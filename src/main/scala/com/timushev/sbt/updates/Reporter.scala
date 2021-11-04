package com.timushev.sbt.updates

import com.timushev.sbt.updates.metadata.MetadataLoaderFactory
import com.timushev.sbt.updates.versions.Version
import sbt._
import sbt.librarymanagement.ModuleFilter
import sbt.std.TaskStreams

import scala.collection.immutable.SortedSet
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.matching.Regex

object Reporter {
  import com.timushev.sbt.updates.UpdatesFinder._

  def dependencyUpdatesData(
      dependencies: Seq[ModuleID],
      dependenciesOverrides: Iterable[ModuleID],
      dependencyPositions: Map[ModuleID, Set[SourcePosition]],
      resolvers: Seq[Resolver],
      credentials: Seq[Credentials],
      scalaVersions: Seq[String],
      excluded: ModuleFilter,
      included: ModuleFilter,
      allowPreRelease: Boolean,
      buildRoot: File,
      out: TaskStreams[_]
  ): Map[ModuleID, SortedSet[Version]] = {
    val buildDependencies = excludeDependenciesFromPlugins(dependencies, dependencyPositions, buildRoot)
    val loaders           = resolvers.collect(MetadataLoaderFactory.loader(out.log, credentials))
    val updatesFuture = Future
      .sequence {
        scalaVersions
          .map(finalDependencies(_, buildDependencies, dependenciesOverrides))
          .map(_.map(findUpdates(loaders, allowPreRelease)))
          .map(Future.sequence(_))
      }
      .map(crossUpdates => crossUpdates.transpose.map(updates => updates.reduce(_.intersect(_))))
    val updates = Await.result(updatesFuture, 1.hour)
    buildDependencies
      .zip(updates)
      .toMap
      .transform(include(included))
      .transform(exclude(excluded))
      .filterNot(_._2.isEmpty)
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
      val table = dependencyUpdates
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

  def dependencyUpdatesReport(project: ModuleID, dependencyUpdates: Map[ModuleID, SortedSet[Version]]): String = {
    val updates = gatherDependencyUpdates(dependencyUpdates)
    if (updates.isEmpty) "No dependency updates found for %s".format(project.name)
    else {
      val info = StringBuilder.newBuilder
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

  def displayDependencyUpdates(
      project: ModuleID,
      dependencyUpdates: Map[ModuleID, SortedSet[Version]],
      failBuild: Boolean,
      out: TaskStreams[_]
  ): Unit = {
    out.log.info(dependencyUpdatesReport(project, dependencyUpdates))
    if (failBuild && dependencyUpdates.nonEmpty) sys.error("Dependency updates found")
  }

  def writeDependencyUpdatesReport(
      project: ModuleID,
      dependencyUpdates: Map[ModuleID, SortedSet[Version]],
      file: File,
      out: TaskStreams[_]
  ): File = {
    IO.write(file, dependencyUpdatesReport(project, dependencyUpdates) + "\n")
    out.log.info("Dependency update report written to %s".format(file))
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

  val FileNamePattern: Regex = "^\\([^\\)]+\\) (.*)$".r
  def extractFileName(path: String): Option[String] =
    path match {
      case FileNamePattern(fileName) => Some(fileName)
      case _                         => None
    }

  def fileExists(buildRoot: File, file: String): Boolean =
    (buildRoot / "project" / file).exists()
}
