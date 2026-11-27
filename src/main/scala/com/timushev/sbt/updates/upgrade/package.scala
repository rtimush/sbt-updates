package com.timushev.sbt.updates

import java.io.File

package object upgrade {

  /** A string literal
    *
    * @param content
    *   string literal value
    * @param start
    *   where does the string literal start, considering quotes (ie. start < contentStart < contentEnd)
    * @param contentStart
    *   where does the content between the quotes start
    * @param contentEnd
    *   where does the content between the quotes end
    */
  final case class StringLiteral(content: String, start: Int, contentStart: Int, contentEnd: Int)

  /** How a version is expressed in a build source. */
  sealed trait VersionExpr
  object VersionExpr {

    /** A plain string literal, e.g. `"1.2.3"`. */
    final case class Literal(literal: StringLiteral) extends VersionExpr

    /** A reference to a definition, e.g. `fooVersion` or `Dependencies.Versions.foo`. */
    final case class Reference(path: List[String]) extends VersionExpr

    /** Anything else (string concatenation, method calls, interpolation, ...), which is never edited. */
    case object Unsupported extends VersionExpr
  }

  /** A dependency declaration, e.g. `"org" %% "name" % version`. */
  final case class DependencyRef(
      source: SourceFile,
      organization: String,
      name: String,
      version: VersionExpr,
      scope: List[String],
      offset: Int
  )

  /** A parameterless `val` or `def`, e.g. `val fooVersion = "1.2.3"`. */
  final case class Definition(
      source: SourceFile,
      name: String,
      scope: List[String],
      rhs: VersionExpr,
      offset: Int
  )

  /** The outcome of planning.
    *
    * When some build sources cannot be parsed,`errors` is non-empty and nothing is edited.
    */
  final case class UpgradePlan(
      edits: Seq[Edit],
      skipped: Seq[String],
      notFound: Seq[Upgrade],
      errors: Seq[String] = Seq.empty
  ) {

    /** Reports the upgrade plan to the logger
      */
    def report(log: sbt.util.Logger): Unit =
      if (errors.nonEmpty) {
        errors.foreach(error => log.error(s"Could not parse $error"))
        log.error("No build sources were changed")
      } else {
        edits.foreach { e =>
          e.upgrades.foreach { u =>
            log.info(s"Upgraded ${u.module} ${u.current} -> ${u.target} (${e.source.path}:${e.line})")
          }
        }

        skipped.foreach(reason => log.warn(s"Skipped $reason"))

        notFound.foreach {
          case u if u.organization == "org.scala-lang" =>
            log.info(s"Skipped ${u.module}: its version is defined by scalaVersion")
          case u =>
            log.warn(s"Skipped ${u.module} ${u.current} -> ${u.target}: no declaration found in the build sources")
        }
      }
  }

  object UpgradePlan {
    def failed(errors: Seq[String]): UpgradePlan = UpgradePlan(Nil, Nil, Nil, errors)
  }

  /** A requested upgrade of a module from its current version to the target one. */
  final case class Upgrade(organization: String, name: String, current: String, target: String) {
    def module: String = s"$organization:$name"
  }

  /** A replacement of a version literal in a build source. */
  final case class Edit(source: SourceFile, literal: StringLiteral, upgrades: Seq[Upgrade]) {
    def file: File          = source.file
    def line: Int           = source.lineOf(literal.contentStart)
    def replacement: String = upgrades.head.target
  }

}
