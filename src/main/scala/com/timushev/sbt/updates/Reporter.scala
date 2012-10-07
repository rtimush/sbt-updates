package com.timushev.sbt.updates

import sbt._
import sbt.std.TaskStreams
import semverfi.SemVersion

object Reporter {

  import UpdatesFinder._

  def displayDependencyUpdates(project: ModuleID, dependencies: Seq[ModuleID], resolvers: Seq[Resolver], scalaFullVersion: String, scalaBinaryVersion: String, out: TaskStreams[_]) {
    val crossDependencies = dependencies.map(CrossVersion(scalaFullVersion, scalaBinaryVersion))
    val loaders = resolvers collect MetadataLoader.factory
    val updates = crossDependencies map findUpdates(loaders) map (_.apply())
    val result = (dependencies zip updates toMap)
      .filterNot(_._2.isEmpty)
      .map((formatInfo _).tupled)
    if (result.isEmpty) out.log.info("No dependency updates found for %s" format (project.name))
    else {
      val columns = result.transpose.map(_.map(_.length).max)
      val header = "Found %s dependency updates for %s\n" format(result.size, project.name)
      val body = result map {
        row =>
          "  %s : %s -> %s" format (row zip columns map (pad _).tupled: _*)
      } mkString "\n"
      out.log.info(header + body)
    }
  }

  def formatInfo(module: ModuleID, updates: Set[SemVersion]) =
    Seq(module.organization + ":" + module.name, module.revision, updates.max.toString)

  def pad(s: String, w: Int) = s.padTo(w, ' ')

}
