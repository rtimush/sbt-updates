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

}
