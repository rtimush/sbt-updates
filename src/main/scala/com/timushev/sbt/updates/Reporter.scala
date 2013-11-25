package com.timushev.sbt.updates

import sbt._
import sbt.std.TaskStreams
import versions.Version
import scala.collection.immutable.SortedSet

object Reporter {

  import UpdatesFinder._

  def dependencyUpdatesData(project: ModuleID,
                            dependencies: Seq[ModuleID],
                            resolvers: Seq[Resolver],
                            scalaFullVersion: String,
                            scalaBinaryVersion: String): Map[ModuleID, SortedSet[Version]] = {
    val crossDependencies = dependencies.map(CrossVersion(scalaFullVersion, scalaBinaryVersion))
    val loaders = resolvers collect MetadataLoaderFactory.loader
    val updates = crossDependencies map findUpdates(loaders) map (_.run)
    (dependencies zip updates toMap).filterNot(_._2.isEmpty).toMap
  }

  def gatherDependencyUpdates(dependencyUpdates: Map[ModuleID, SortedSet[Version]]): Seq[String] = {
    if (dependencyUpdates.isEmpty) Seq.empty
    else {
      val table = dependencyUpdates.map {
        case (m, vs) =>
          val c = Version(m.revision)
          Seq(
            Some(formatModule(m)),
            Some(m.revision),
            patchUpdate(c, vs).map(_.toString),
            minorUpdate(c, vs).map(_.toString),
            majorUpdate(c, vs).map(_.toString)
          )
      }.toSeq.sortBy(_.head)
      val widths = table.transpose.map {
        c => c.foldLeft(0) {
          _ max _.map(_.length).getOrElse(0)
        }
      }
      val separator = Seq("", " : ", " -> ", " -> ", " -> ")
      for (row <- table) yield {
        (separator zip row zip widths) map {
          case (_, 0) => ""
          case ((s, Some(v)), w) => s + pad(v, w)
          case ((s, None), w) => " " * (s.length + w)
        } mkString ""
      }
    }
  }

  def dependencyUpdatesReport(project: ModuleID, dependencyUpdates: Map[ModuleID, SortedSet[Version]]): String = {
    val updates = gatherDependencyUpdates(dependencyUpdates)
    if (updates.isEmpty) "No dependency updates found for %s" format (project.name)
    else {
      val info = StringBuilder.newBuilder
      info.append("Found %s dependency update%s for %s" format(updates.size, if (updates.size > 1) "s" else "", project.name))
      updates.foreach {
        u =>
          info.append("\n  ")
          info.append(u)
      }
      info.toString()
    }
  }

  def displayDependencyUpdates(project: ModuleID, dependencyUpdates: Map[ModuleID, SortedSet[Version]], out: TaskStreams[_]): Unit = {
    out.log.info(dependencyUpdatesReport(project, dependencyUpdates))
  }

  def formatModule(module: ModuleID) =
    module.organization + ":" + module.name + module.configurations.map(":" + _).getOrElse("")

  def patchUpdate(c: Version, updates: SortedSet[Version]) =
    updates.filter {
      v => v.major == c.major && v.minor == c.minor
    }.lastOption

  def minorUpdate(c: Version, updates: SortedSet[Version]) =
    updates.filter {
      v => v.major == c.major && v.minor > c.minor
    }.lastOption

  def majorUpdate(c: Version, updates: SortedSet[Version]) =
    updates.filter {
      v => v.major > c.major
    }.lastOption

  def pad(s: String, w: Int) = s.padTo(w, ' ')

}
