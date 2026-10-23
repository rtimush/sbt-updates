package com.timushev.sbt.updates.upgrade

import com.timushev.sbt.updates.Compat
import com.timushev.sbt.updates.versions.{ReleaseVersion, Version}
import sbt._
import sbt.util.Logger

import scala.collection.immutable.SortedSet

object Upgrader {

  /** What one project knows about its dependencies.
    *
    * @param id
    *   the project id, used in messages
    * @param dependencies
    *   all its declared dependencies, including those already at their latest version
    * @param updates
    *   the newer versions of its outdated dependencies, as computed by `dependencyUpdatesData`
    */
  final case class ProjectUpdates(id: String, dependencies: Seq[ModuleID], updates: Map[ModuleID, SortedSet[Version]])

  /** Upgrades the dependencies to their latest release versions by changing the build sources in place. */
  def upgrade(
      projects: Seq[ProjectUpdates],
      buildRoot: File,
      projectDirectories: Seq[File],
      log: Logger
  ): Seq[File] = {
    val (upgrades, conflicts) = latestReleases(projects)
    conflicts.foreach(conflict => log.warn(s"Skipped $conflict"))

    if (upgrades.isEmpty) {
      log.info("No dependency upgrades found")
      Seq.empty[File]
    } else {
      val sources = buildSources(buildRoot, projectDirectories)

      val plan = UpgradePlanner.plan(upgrades, sources, scala3First = Compat.scala3Sources)
      plan.report(log)

      val changed = apply(plan.edits)
      if (changed.nonEmpty) {
        val count = plan.edits.map(_.upgrades.size).sum
        log.info(
          s"Upgraded $count ${if (count == 1) "dependency" else "dependencies"} in ${changed.size} " +
            s"${if (changed.size == 1) "file" else "files"}, reload the build to apply the changes"
        )
      }

      changed
    }
  }

  /** The latest release version of every outdated dependency, ignoring pre-releases and snapshots.
    *
    * A declaration may be shared by several projects (e.g. through common settings), which can see different newer
    * versions because of their Scala versions, resolvers or filters. So the target is the latest release that is newer
    * for every project declaring the dependency; a project declaring it with no newer version allows none.
    *
    * @return
    *   the upgrades, and a message for every dependency the projects declaring it disagree on
    */
  private[upgrade] def latestReleases(projects: Seq[ProjectUpdates]): (Seq[Upgrade], Seq[String]) = {
    def key(module: ModuleID) = (module.organization, module.name, module.revision)

    def releases(versions: SortedSet[Version]): Set[Version] = versions.collect { case v @ ReleaseVersion(_) =>
      v
    }.toSet

    val outdated = projects.flatMap(_.updates.keys.map(key)).distinct.sorted

    val results = outdated.flatMap { case k @ (organization, name, current) =>
      val perProject = projects.collect {
        case p if p.dependencies.exists(key(_) == k) || p.updates.keys.exists(key(_) == k) =>
          val newer = p.updates.collect { case (m, versions) if key(m) == k => releases(versions) }
          p.id -> newer.reduceOption(_ intersect _).getOrElse(Set.empty[Version])
      }
      val common = perProject.map(_._2).reduce(_ intersect _)

      if (common.nonEmpty) {
        Some(Left(Upgrade(organization, name, current, common.max.text)))
      } else if (perProject.forall(_._2.isEmpty)) {
        // only pre-releases are newer: there is nothing to upgrade to, which is not a disagreement
        None
      } else {
        val latest = perProject.map { case (id, vs) => s"$id: ${if (vs.isEmpty) "none" else vs.max.text}" }
        Some(
          Right(
            s"$organization:$name $current: no newer release is available to every project using it " +
              s"(${latest.mkString(", ")})"
          )
        )
      }
    }

    (results.collect { case Left(upgrade) => upgrade }, results.collect { case Right(conflict) => conflict })
  }

  /** The files where dependencies can be declared. */
  private def buildSources(buildRoot: File, projectDirectories: Seq[File]): Seq[SourceFile] = {
    val projectDir = buildRoot / "project"
    val sbtFiles   = (projectDir +: buildRoot +: projectDirectories).distinct.flatMap(dir => listFiles(dir, ".sbt"))
    val metaFiles  = scalaFiles(projectDir)

    val files = (sbtFiles ++ metaFiles).map(_.getCanonicalFile).distinct.sortBy(_.getPath)

    files.map(file =>
      SourceFile(
        file,
        IO.read(file),
        IO.relativize(buildRoot.getCanonicalFile, file).getOrElse(file.getPath)
      )
    )
  }

  private def listFiles(dir: File, extension: String): Seq[File] =
    Option(dir.listFiles()).toSeq.flatten.filter(f => f.isFile && f.getName.endsWith(extension))

  /** Sources of the meta-build, excluding build outputs and the meta-meta-build
    * @param dir
    *   location of `project` folder
    */
  private def scalaFiles(dir: File): Seq[File] =
    Option(dir.listFiles()).toSeq.flatten.flatMap { f =>
      if (f.isDirectory) {
        if (f.getName == "target" || f.getName == "project" || f.getName.startsWith(".")) {
          Seq.empty[File]
        } else {
          scalaFiles(f)
        }
      } else if (f.getName.endsWith(".scala")) {
        Seq(f)
      } else {
        Seq.empty[File]
      }
    }

  private def apply(edits: Seq[Edit]): Seq[File] =
    edits.groupBy(_.source).toSeq.map { case (source, fileEdits) =>
      // Sort by place to edit in ascending order, and then
      // process right-to-left to avoid previous edits moving the place to edit of following ones
      val updated = fileEdits.sortBy(e => e.literal.contentStart).foldRight(source.text) { (e, text) =>
        text.substring(0, e.literal.contentStart) + e.replacement + text.substring(e.literal.contentEnd)
      }

      IO.write(source.file, updated)
      source.file
    }
}
