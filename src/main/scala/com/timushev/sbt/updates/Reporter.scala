package com.timushev.sbt.updates

import com.timushev.sbt.updates.Compat._
import com.timushev.sbt.updates.metadata.MetadataLoaderFactory
import com.timushev.sbt.updates.versions.Version
import sbt._
import sbt.std.TaskStreams

import scala.collection.immutable.SortedSet
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Reporter {

  import com.timushev.sbt.updates.UpdatesFinder._

  def dependencyUpdatesData(project: ModuleID,
                            dependencies: Seq[ModuleID],
                            dependencyPositions: Map[ModuleID, SourcePosition],
                            resolvers: Seq[Resolver],
                            credentials: Seq[Credentials],
                            scalaVersions: Seq[String],
                            excluded: ModuleFilter,
                            included: ModuleFilter,
                            allowPreRelease: Boolean,
                            out: TaskStreams[_]): Map[ModuleID, SortedSet[Version]] = {
    val buildDependencies = excludeDependenciesFromPlugins(dependencies, dependencyPositions)
    val loaders = resolvers.collect(MetadataLoaderFactory.loader(out.log, credentials))
    val updatesFuture = Future
      .sequence(scalaVersions.map { scalaVersion =>
        val crossVersion = CrossVersion(scalaVersion, CrossVersion.binaryScalaVersion(scalaVersion))
        val crossDependencies = dependencies.map(crossVersion)
        Future.sequence(crossDependencies.map(findUpdates(loaders, allowPreRelease)))
      })
      .map { crossUpdates =>
        crossUpdates.transpose.map { updates =>
          updates.reduce(_.intersect(_))
        }
      }
    val updates = Await.result(updatesFuture, 1.hour)
    buildDependencies
      .zip(updates)
      .toMap
      .transform(include(included))
      .transform(exclude(excluded))
      .filterNot(_._2.isEmpty)
  }

  def gatherDependencyUpdates(dependencyUpdates: Map[ModuleID, SortedSet[Version]]): Seq[String] = {
    if (dependencyUpdates.isEmpty) Seq.empty
    else {
      val table = dependencyUpdates
        .map {
          case (m, vs) =>
            val c = Version(m.revision)
            Seq(
              Some(formatModule(m)),
              Some(m.revision),
              patchUpdate(c, vs).map(_.toString),
              minorUpdate(c, vs).map(_.toString),
              majorUpdate(c, vs).map(_.toString)
            )
        }
        .toSeq
        .sortBy(_.head)
      val widths = table.transpose.map { c =>
        c.foldLeft(0) { (m, c) =>
          m.max(c.map(_.length).getOrElse(0))
        }
      }
      val separator = Seq("", " : ", " -> ", " -> ", " -> ")
      for (row <- table) yield {
        separator
          .zip(row)
          .zip(widths)
          .map {
            case (_, 0)            => ""
            case ((s, Some(v)), w) => s + pad(v, w)
            case ((s, None), w)    => " " * (s.length + w)
          }
          .mkString("")
      }
    }
  }

  def dependencyUpdatesReport(project: ModuleID, dependencyUpdates: Map[ModuleID, SortedSet[Version]]): String = {
    val updates = gatherDependencyUpdates(dependencyUpdates)
    if (updates.isEmpty) "No dependency updates found for %s".format(project.name)
    else {
      val info = StringBuilder.newBuilder
      info.append(
        "Found %s dependency update%s for %s".format(updates.size, if (updates.size > 1) "s" else "", project.name))
      updates.foreach { u =>
        info.append("\n  ")
        info.append(u)
      }
      info.toString()
    }
  }

  def displayDependencyUpdates(project: ModuleID,
                               dependencyUpdates: Map[ModuleID, SortedSet[Version]],
                               failBuild: Boolean,
                               out: TaskStreams[_]): Unit = {
    out.log.info(dependencyUpdatesReport(project, dependencyUpdates))
    if (failBuild && dependencyUpdates.nonEmpty) sys.error("Dependency updates found")
  }

  def writeDependencyUpdatesReport(project: ModuleID,
                                   dependencyUpdates: Map[ModuleID, SortedSet[Version]],
                                   file: File,
                                   out: TaskStreams[_]): File = {
    IO.write(file, dependencyUpdatesReport(project, dependencyUpdates) + "\n")
    out.log.info("Dependency update report written to %s".format(file))
    file
  }

  def writeDependencyUpdatesReports(projectUpdates: Seq[(ModuleID, Map[sbt.ModuleID, SortedSet[Version]])],
                                    file: File,
                                    out: TaskStreams[_]): File = {
    IO.delete(file)
    projectUpdates.foreach {
      case (project, dependencyUpdates) =>
        IO.write(file, dependencyUpdatesReport(project, dependencyUpdates) + "\n", append = true)
    }
    out.log.info("Dependency update reports written to %s".format(file))
    file
  }

  def formatModule(module: ModuleID) =
    module.organization + ":" + module.name + module.configurations.map(":" + _).getOrElse("")

  def patchUpdate(c: Version, updates: SortedSet[Version]): Option[Version] =
    updates.filter { v =>
      v.major == c.major && v.minor == c.minor
    }.lastOption

  def minorUpdate(c: Version, updates: SortedSet[Version]): Option[Version] =
    updates.filter { v =>
      v.major == c.major && v.minor > c.minor
    }.lastOption

  def majorUpdate(c: Version, updates: SortedSet[Version]): Option[Version] =
    updates.filter { v =>
      v.major > c.major
    }.lastOption

  def pad(s: String, w: Int): String = s.padTo(w, ' ')

  def include(included: ModuleFilter)(module: ModuleID, versions: SortedSet[Version]): SortedSet[Version] = {
    versions.filter { version =>
      included.apply(module.withRevision0(version.toString))
    }
  }

  def exclude(excluded: ModuleFilter)(module: ModuleID, versions: SortedSet[Version]): SortedSet[Version] = {
    versions.filterNot { version =>
      excluded.apply(module.withRevision0(version.toString))
    }
  }

  def excludeDependenciesFromPlugins(dependencies: Seq[ModuleID],
                                     dependencyPositions: Map[ModuleID, SourcePosition]): Seq[ModuleID] = {
    dependencies.filter { moduleId =>
      dependencyPositions.get(moduleId) match {
        case Some(fp: FilePosition) if fp.path.startsWith("(sbt.Classpaths") => true
        case Some(fp: FilePosition) if fp.path.startsWith("(")               => false
        case _                                                               => true
      }
    }
  }

}
