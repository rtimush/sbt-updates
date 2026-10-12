package com.timushev.sbt.updates

import com.timushev.sbt.updates.LookupSession.executionContext
import com.timushev.sbt.updates.authentication.RepositoryAuthentication
import com.timushev.sbt.updates.metadata.MetadataLoaderFactory
import com.timushev.sbt.updates.model.{Csv, ReportType, SbtOutput}
import com.timushev.sbt.updates.versions.Version
import sbt._
import sbt.librarymanagement.ModuleFilter

import scala.collection.immutable.SortedSet
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.control.NonFatal
import scala.util.matching.Regex

object Reporter {
  import com.timushev.sbt.updates.UpdatesFinder._

  // Projects are checked in parallel, and sbt would otherwise interleave the lines of their reports.
  private val outputLock = new Object

  def dependencyUpdatesData(
      project: ModuleID,
      dependencies: Seq[ModuleID],
      dependenciesOverrides: Iterable[ModuleID],
      dependencyPositions: Map[ModuleID, Set[SourcePosition]],
      resolvers: Seq[Resolver],
      credentials: Seq[Credentials],
      csrConfiguration: Option[AnyRef],
      scalaVersions: Seq[String],
      excluded: ModuleFilter,
      included: ModuleFilter,
      allowPreRelease: Boolean,
      buildRoot: File,
      session: LookupSession,
      log: Logger
  ): Map[ModuleID, SortedSet[Version]] = {
    val buildDependencies = excludeDependenciesFromPlugins(dependencies, dependencyPositions, buildRoot)
    val authentications   =
      credentials.flatMap(RepositoryAuthentication.fromCredentials) ++
        csrConfiguration.toSeq.flatMap(coursierAuthentications(_, log))
    val loaders       = resolvers.collect(MetadataLoaderFactory.loader(session, log, authentications))
    val updatesFuture = Future
      .sequence {
        scalaVersions
          .map(finalDependencies(_, buildDependencies, dependenciesOverrides))
          .map(_.map(findUpdates(loaders, allowPreRelease)))
          .map(Future.sequence(_))
      }
    val crossUpdates = Await.result(updatesFuture, 1.hour)
    reportFailures(project, crossUpdates.flatten.flatMap(_.failures), log)
    buildDependencies
      .zip(crossUpdates.transpose.map(_.map(_.versions).reduce(_.intersect(_))))
      .toMap
      .transform(include(included))
      .transform(exclude(excluded))
      .filterNot(_._2.isEmpty)
  }

  def reportFailures(project: ModuleID, failures: Seq[LookupFailure], log: Logger): Unit =
    if (failures.nonEmpty) outputLock.synchronized {
      failures.foreach { f =>
        log.debug(s"${project.name}: could not check ${f.module} in repository ${f.repository}: ${f.cause}")
      }
      failures.groupBy(_.repository).toSeq.sortBy(_._1).foreach { case (repository, failed) =>
        val modules = failed.map(f => (f.module.organization, f.module.name)).distinct.size
        val cause   = failed.head.cause
        log.warn(
          s"${project.name}: could not check $modules module${if (modules == 1) "" else "s"} in repository $repository " +
            s"(${Option(cause.getMessage).getOrElse(cause.getClass.getName)}); updates may be missing"
        )
      }
    }

  // crossScalaVersions := Nil is how builds keep a project out of `+`; its dependencies still need checking.
  def scalaVersionsToCheck(scalaVersion: String, crossScalaVersions: Seq[String]): Seq[String] =
    if (crossScalaVersions.isEmpty) Seq(scalaVersion) else crossScalaVersions

  // lm-coursier is read reflectively; a changed shape must cost only its credentials, not fail the task.
  private[updates] def coursierAuthentications(configuration: AnyRef, log: Logger): Seq[RepositoryAuthentication] =
    try RepositoryAuthentication.fromCoursier(configuration)
    catch {
      case NonFatal(e) =>
        log.debug(s"Could not read repository credentials from the Coursier configuration: $e")
        Nil
    }

  def overrideDependencies(dependencies: Seq[ModuleID], overrides: Iterable[ModuleID]): Seq[ModuleID] = {
    def key(id: ModuleID) = (id.organization, id.name)
    val overridden        = overrides.map(id => (key(id), id.revision)).toMap
    dependencies.map { dep =>
      overridden.get(key(dep)) match {
        case Some(rev) => dep.withRevision(rev)
        case None      => dep
      }
    }
  }

  def finalDependencies(
      scalaVersion: String,
      dependencies: Seq[ModuleID],
      overrides: Iterable[ModuleID]
  ): Seq[ModuleID] = {
    val crossVersion      = CrossVersion(scalaVersion, CrossVersion.binaryScalaVersion(scalaVersion))
    val crossDependencies = dependencies.map(crossVersion)
    val crossOverrides    = overrides.map(crossVersion)
    overrideDependencies(crossDependencies, crossOverrides)
  }

  def gatherDependencyUpdates(dependencyUpdates: Map[ModuleID, SortedSet[Version]]): Seq[String] =
    if (dependencyUpdates.isEmpty) Seq.empty
    else {
      val table     = latestSemVerRevisions(dependencyUpdates)
      val widths    = table.transpose.map(c => c.foldLeft(0)((m, c) => m.max(c.map(_.length).getOrElse(0))))
      val separator = Seq("", " : ", " -> ", " -> ", " -> ")
      for (row <- table)
        yield separator
          .zip(row)
          .zip(widths)
          .map {
            case (_, 0)            => ""
            case ((s, Some(v)), w) => s + pad(v, w)
            case ((s, None), w)    => " " * (s.length + w)
          }
          .mkString("")
    }

  private def latestSemVerRevisions(dependencyUpdates: Map[ModuleID, SortedSet[Version]]) =
    dependencyUpdates
      .map { case (m, vs) =>
        val c = Version(m.revision)
        Seq(
          Some(formatModule(m)),
          Some(m.revision),
          patchUpdate(c, vs).map(_.text),
          minorUpdate(c, vs).map(_.text),
          majorUpdate(c, vs).map(_.text)
        )
      }
      .toSeq
      .sortBy(_.head)

  def dependencyUpdatesReport(project: ModuleID, dependencyUpdates: Map[ModuleID, SortedSet[Version]]): String = {
    val updates = gatherDependencyUpdates(dependencyUpdates)
    if (updates.isEmpty) "No dependency updates found for %s".format(project.name)
    else {
      val info = new StringBuilder
      info.append(
        "Found %s dependency update%s for %s".format(updates.size, if (updates.size > 1) "s" else "", project.name)
      )
      updates.foreach { u =>
        info.append("\n  ")
        info.append(u)
      }
      info.toString()
    }
  }

  def dependencyUpdatesCsvReport(dependencyUpdates: Map[ModuleID, SortedSet[Version]]): String =
    latestSemVerRevisions(dependencyUpdates)
      .map(_.filterNot(_.isEmpty).map(_.getOrElse("")).mkString(";"))
      .mkString("\n")

  def displayDependencyUpdates(
      project: ModuleID,
      dependencyUpdates: Map[ModuleID, SortedSet[Version]],
      failBuild: Boolean,
      log: Logger
  ): Unit = {
    outputLock.synchronized(log.info(dependencyUpdatesReport(project, dependencyUpdates)))
    if (failBuild && dependencyUpdates.nonEmpty) sys.error("Dependency updates found")
  }

  def writeDependencyUpdatesReport(
      project: ModuleID,
      dependencyUpdates: Map[ModuleID, SortedSet[Version]],
      file: File,
      `type`: ReportType,
      log: Logger
  ): File = {
    val reportContent = `type` match {
      case SbtOutput => dependencyUpdatesReport(project, dependencyUpdates) + "\n"
      case Csv       => dependencyUpdatesCsvReport(dependencyUpdates)
    }
    IO.write(file, reportContent)
    log.info("Dependency update report written to %s".format(file))
    file
  }

  def formatModule(module: ModuleID) =
    module.organization + ":" + module.name + module.configurations.map(":" + _).getOrElse("")

  def patchUpdate(c: Version, updates: SortedSet[Version]): Option[Version] =
    updates.filter(v => v.major == c.major && v.minor == c.minor).lastOption

  def minorUpdate(c: Version, updates: SortedSet[Version]): Option[Version] =
    updates.filter(v => v.major == c.major && v.minor > c.minor).lastOption

  def majorUpdate(c: Version, updates: SortedSet[Version]): Option[Version] =
    updates.filter(v => v.major > c.major).lastOption

  def pad(s: String, w: Int): String = s.padTo(w, ' ')

  def include(included: ModuleFilter)(module: ModuleID, versions: SortedSet[Version]): SortedSet[Version] =
    versions.filter(version => included.apply(module.withRevision(version.text)))

  def exclude(excluded: ModuleFilter)(module: ModuleID, versions: SortedSet[Version]): SortedSet[Version] =
    versions.filterNot(version => excluded.apply(module.withRevision(version.text)))

  def excludeDependenciesFromPlugins(
      dependencies: Seq[ModuleID],
      dependencyPositions: Map[ModuleID, Set[SourcePosition]],
      buildRoot: File
  ): Seq[ModuleID] =
    dependencies.filter {
      case moduleId
          if moduleId.organization == "org.scala-lang" &&
            moduleId.name == "scala-library" =>
        true
      case moduleId =>
        dependencyPositions.get(moduleId).fold(true) {
          _.exists {
            case fp: FilePosition if fp.path.startsWith("(sbt.Classpaths") =>
              false
            case fp: FilePosition if fp.path.startsWith("(") =>
              extractFileName(fp.path).exists(fileExists(buildRoot, _))
            case _ =>
              true
          }
        }
    }

  val FileNamePattern: Regex                        = "^\\([^\\)]+\\) (.*)$".r
  def extractFileName(path: String): Option[String] =
    path match {
      case FileNamePattern(fileName) => Some(fileName)
      case _                         => None
    }

  // sbt records only the file name, and build sources can also live under project/src, e.g. project/src/main/scala.
  def fileExists(buildRoot: File, file: String): Boolean = {
    val project = buildRoot / "project"
    (project / file).exists() || containsFile(project / "src", file)
  }

  private def containsFile(directory: File, name: String): Boolean =
    Option(directory.listFiles()).exists(_.exists(f => if (f.isDirectory) containsFile(f, name) else f.getName == name))
}
