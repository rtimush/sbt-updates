package com.timushev.sbt.updates.upgrade

import java.io.File

/** Decides which version literals have to be changed to perform a set of upgrades.
  *
  * Build sources are only ever changed when the outcome is certain:
  *   - every dependency declaration is resolved to the string literal holding its version, following references to
  *     `val`s and `def`s, possibly declared in other files or objects;
  *   - a reference is only followed when it resolves to exactly one definition;
  *   - a literal is only changed when it holds the current version of the dependency, as seen by sbt;
  *   - a literal shared by several dependencies (e.g. `val akkaVersion = "..."`) is only changed when all of them are
  *     upgraded to the same version.
  *
  * Anything else is reported as skipped, so it can be updated manually.
  */
object UpgradePlanner {

  private val MaxReferenceDepth = 16

  final private case class Skipped(reason: String, modules: Set[(String, String)])

  def plan(upgrades: Seq[Upgrade], sources: Seq[SourceFile], scala3First: Boolean): UpgradePlan = {
    val build = BuildSources(sources, scala3First)

    // a declaration missed in an unparsable file could share a version with one being upgraded
    if (build.errors.isEmpty) {
      plan(upgrades, build)
    } else {
      UpgradePlan.failed(build.errors)
    }
  }

  private def plan(upgrades: Seq[Upgrade], build: BuildSources): UpgradePlan = {
    // Find where versions are being defined for each dependency
    val declarationsWithLiteral: Seq[(DependencyRef, Either[String, (SourceFile, StringLiteral)])] =
      build.dependencies.map(declaration => declaration -> resolve(build, declaration))

    // Group the _resolved_ declarations by the literal they resolve to.
    // A literal can be shared by several declarations, e.g. `val akkaVersion = "2.6.20"` used by every Akka module.
    val declarationsByLiteral: Seq[((SourceFile, StringLiteral), Seq[DependencyRef])] = declarationsWithLiteral
      .collect { case (declaration, Right(sourceAndLiteral)) => sourceAndLiteral -> declaration }
      .groupBy { case (sourceAndLiteral, _) => sourceAndLiteral }
      .map { case (sourceAndLiteral, pairs) => sourceAndLiteral -> pairs.map { case (_, declaration) => declaration } }
      .toSeq
      // Sort to make output order more user-friendly
      .sortBy { case ((source, literal), _) => (source.file.getPath, literal.start) }

    // A module may have several upgrades: one per current version, or one per project when projects see different latest versions.
    val upgradesByModule: Map[(String, String), Seq[Upgrade]] = upgrades.groupBy(u => (u.organization, u.name))

    // Decide for each literal whether to change it or to skip it. A literal is only
    // changed when every declaration using it has an upgrade from exactly the version it holds, and all of these
    // upgrades agree on the target version.
    val literalDecisions: Seq[Either[Skipped, Edit]] = declarationsByLiteral.flatMap {
      case ((source, literal), declarations) =>
        // the upgrades applicable to each declaration: those of its module, from the version the literal holds
        // (several when projects see different latest versions, which then disagree on the target below)
        val upgradesPerDeclaration: Seq[(DependencyRef, Seq[Upgrade])] = declarations.map { declaration =>
          val moduleUpgrades = upgradesByModule.getOrElse((declaration.organization, declaration.name), Nil)

          declaration -> moduleUpgrades.filter(_.current == literal.content)
        }

        val applicableUpgrades = upgradesPerDeclaration.flatMap { case (_, moduleUpgrades) => moduleUpgrades }

        if (applicableUpgrades.isEmpty) {
          // none of the modules using this literal is being upgraded, nothing to decide
          None
        } else {
          val sharingModules     = declarations.map(d => (d.organization, d.name)).toSet
          val sharingModulesText = sharingModules.map { case (o, n) => s"$o:$n" }.toSeq.sorted.mkString(", ")
          val targetVersions     = applicableUpgrades.map(_.target).distinct
          val sharedLiteral      =
            s"version ${literal.content} at ${location(source, literal.contentStart)} is shared by $sharingModulesText"

          if (upgradesPerDeclaration.exists { case (_, moduleUpgrades) => moduleUpgrades.isEmpty }) {
            // changing it would also change a module that is not being upgraded
            Some(Left(Skipped(s"$sharedLiteral, not all of which can be upgraded", sharingModules)))
          } else if (targetVersions.size > 1) {
            // no single new version satisfies every module or project using it
            Some(
              Left(
                Skipped(
                  s"$sharedLiteral, which have different latest versions: ${targetVersions.mkString(", ")}",
                  sharingModules
                )
              )
            )
          } else {
            Some(Right(Edit(source, literal, applicableUpgrades.distinct)))
          }
        }
    }

    val unresolvedDeclarations: Seq[Skipped] = declarationsWithLiteral.collect {
      case (declaration, Left(reason)) if upgradesByModule.contains((declaration.organization, declaration.name)) =>
        val loc = location(declaration.source, declaration.offset)

        Skipped(
          s"${declaration.organization}:${declaration.name} at $loc: $reason",
            Set(declaration.organization -> declaration.name)
        )
    }

    val edits            = literalDecisions.collect { case Right(edit) => edit }
    val skipped          = unresolvedDeclarations ++ literalDecisions.collect { case Left(skip) => skip }
    val appliedUpgrades  = edits.flatMap(_.upgrades).toSet
    val skippedModules   = skipped.flatMap(_.modules).toSet
    val notFoundUpgrades = upgrades.filterNot { u =>
      appliedUpgrades(u) || skippedModules((u.organization, u.name))
    }

    UpgradePlan(edits, skipped.map(_.reason), notFoundUpgrades)
  }

  /** Resolves a [[DependencyRef]] from the list of [[BuildSources]]
    *
    * @param build
    *   build sources
    * @param dep
    *   reference to resolve
    * @return
    *   either the SourceFile where the reference is defined, and its value, or the resolution error
    */
  private def resolve(build: BuildSources, dep: DependencyRef): Either[String, (SourceFile, StringLiteral)] =
    resolve(build, dep.version, dep.source, dep.scope, MaxReferenceDepth)

  private def resolve(
      build: BuildSources,
      expr: VersionExpr,
      source: SourceFile,
      scope: List[String],
      depth: Int
  ): Either[String, (SourceFile, StringLiteral)] =
    expr match {
      case VersionExpr.Literal(literal)           => Right(source -> literal)
      case VersionExpr.Unsupported                => Left("the version is not a string literal or a reference")
      case VersionExpr.Reference(_) if depth == 0 => Left("too many nested references")
      case VersionExpr.Reference(path)            =>
        candidates(build, path, source, scope) match {
          case Seq(d) => resolve(build, d.rhs, d.source, d.scope, depth - 1)
          case Seq()  => Left(s"no definition of `${path.mkString(".")}` found")
          case ds     =>
            Left(
              s"`${path.mkString(".")}` is ambiguous, it may refer to any of " +
                ds.map(d => location(d.source, d.offset)).mkString(", ")
            )
        }
    }

  /** The definitions a reference to a version may resolve to; the caller only follows it when there is exactly one.
    *
    *   - A qualified reference, e.g. `Dependencies.Versions.foo`, matches definitions named `foo` whose enclosing
    *     packages and objects end with `Dependencies.Versions`, in any file.
    *   - An unqualified reference, e.g. `foo`, matches the innermost definition visible from the reference in the same
    *     file; when there is none, it matches definitions in other files, as it may be imported from any of them.
    *
    * @param build
    *   all definitions found in the build sources
    * @param path
    *   the reference, split on dots, e.g. `List("Dependencies", "Versions", "foo")`
    * @param source
    *   the file containing the reference
    * @param scope
    *   the packages and objects enclosing the reference, outermost first
    * @return
    *   the matching definitions; empty when there is no match, several when the reference is ambiguous
    */
  private def candidates(
      build: BuildSources,
      path: List[String],
      source: SourceFile,
      scope: List[String]
  ): Seq[Definition] = {
    val name      = path.last
    val qualifier = path.init
    val named     = build.definitions.filter(_.name == name)

    if (qualifier.nonEmpty) {
      named.filter(_.scope.endsWith(qualifier))
    } else {
      // an unqualified name is either visible lexically, or is imported from another file
      val visible = named.filter(d => d.source == source && scope.startsWith(d.scope))

      if (visible.nonEmpty) {
        visible.filter(_.scope.length == visible.map(_.scope.length).max)
      } else {
        named.filterNot(_.source == source)
      }
    }
  }

  private def location(source: SourceFile, offset: Int): String =
    s"${source.path}:${source.lineOf(offset)}"
}
