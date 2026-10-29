package com.timushev.sbt.updates.upgrade

import java.io.File

import scala.meta._

/** All dependency references and version definitions for the build sources of a project
  *
  * @param dependencies
  *   dependency references (e.g. `"org" %% "name" % version`)
  * @param definitions
  *   version definitions (e.g. val fooVersion = "1.2.3")
  * @param errors
  *   any error encountered during parsing
  */
final case class BuildSources(
    dependencies: Seq[DependencyRef],
    definitions: Seq[Definition],
    errors: Seq[String]
)

object BuildSources {

  private val dependencyOps = Set("%", "%%", "%%%")

  /** Finds dependency declarations and version definitions in build sources, using the Scalameta parser. */
  def apply(sources: Seq[SourceFile], scala3First: Boolean): BuildSources = {
    val analysed = sources.map(source => analyse(source, dialectsFor(source.isSbt, scala3First)))

    BuildSources(
      analysed.flatMap(_.dependencies),
      analysed.flatMap(_.definitions),
      analysed.flatMap(_.errors)
    )
  }

  private def analyse(source: SourceFile, dialects: Seq[Dialect]): BuildSources = {
    val input  = Input.VirtualFile(source.path, source.text)
    val parses = dialects.view.map(dialect => dialect(input).parse[Source])

    parses.collectFirst { case Parsed.Success(tree) => tree } match {
      case Some(tree) =>
        collect(source, tree)

      case None =>
        // if couldn't parse any dialect, get the error for the first dialect,
        // which is the one matching the build
        val error = parses.head match {
          case e: Parsed.Error => s"${source.path}:${e.pos.startLine + 1}: ${e.message}"
          case _               => source.path
        }

        BuildSources(Nil, Nil, Seq(error))
    }
  }

  /** The dialects to try, first the Scala version of the build itself
    *
    * If dialect selection becomes an issue on SBT 1.x builds with `-Xsource:3`, then `dialects.Scala212Source3` could
    * be used
    */
  private def dialectsFor(isSbt: Boolean, scala3First: Boolean): Seq[Dialect] = {
    val (scala2, scala3) = if (isSbt) {
      Seq(dialects.Sbt1) -> Seq(dialects.Scala3.withAllowToplevelTerms(true))
    } else {
      Seq(dialects.Scala212) -> Seq(dialects.Scala3)
    }

    if (scala3First) {
      scala3 ++ scala2
    } else {
      scala2 ++ scala3
    }
  }

  private def collect(source: SourceFile, tree: Tree): BuildSources = {
    val dependencies = Vector.newBuilder[DependencyRef]
    val definitions  = Vector.newBuilder[Definition]

    def visit(t: Tree, scope: List[String]): Unit = {
      t match {
        // `"org" %% "name" % version`
        case Dependency(org, name, version) =>
          dependencies += DependencyRef(source, org.value, name.value, versionExpr(version), scope, org.pos.start)

        // `val fooVersion = "1.2.3"`
        case d: Defn.Val =>
          d.pats match {
            case List(Pat.Var(name)) =>
              definitions += Definition(source, name.value, scope, versionExpr(d.rhs), name.pos.start)

            case _ =>
          }

        // `def fooVersion = "1.2.3"`
        case d: Defn.Def if d.paramClauseGroups.isEmpty =>
          definitions += Definition(source, d.name.value, scope, versionExpr(d.body), d.name.pos.start)

        case _ => // ignore any other tree/case
      }

      val inner = t match {
        case p: Pkg         => scope ++ path(p.ref).getOrElse(Nil)
        case o: Defn.Object => scope :+ o.name.value
        case c: Defn.Class  => scope :+ c.name.value
        case c: Defn.Trait  => scope :+ c.name.value
        case p: Pkg.Object  => scope :+ p.name.value
        case _              => scope
      }

      t.children.foreach(visit(_, inner))
    }

    visit(tree, Nil)

    BuildSources(dependencies.result(), definitions.result(), Nil)
  }

  /** Matches `"org" % "name" % version`, as well as `%%` and `%%%` between the organization and the name. */
  private object Dependency {
    def unapply(t: Tree): Option[(Lit.String, Lit.String, Term)] =
      t match {
        case outer: Term.ApplyInfix if outer.op.value == "%" =>
          (outer.lhs, outer.argClause.values) match {
            case (inner: Term.ApplyInfix, List(version)) if dependencyOps(inner.op.value) =>
              (inner.lhs, inner.argClause.values) match {
                case (org: Lit.String, List(name: Lit.String)) => Some((org, name, version))
                case _                                         => None
              }

            case _ =>
              None
          }

        case _ => None
      }
  }

  private def versionExpr(term: Term): VersionExpr =
    term match {
      case lit: Lit.String =>
        literal(lit).fold[VersionExpr](VersionExpr.Unsupported)(VersionExpr.Literal(_))

      case _ =>
        path(term).fold[VersionExpr](VersionExpr.Unsupported)(VersionExpr.Reference(_))
    }

  /** A literal whose source text is exactly its value between quotes (with no escapes) */
  private def literal(lit: Lit.String): Option[StringLiteral] = {
    val raw   = lit.pos.text
    val quote = if (raw.startsWith("\"\"\"")) 3 else 1

    // Reject literals with escape sequences (e.g. "1.2.19\t")
    // to avoid overwriting how the original author input
    if (raw.length >= 2 * quote && raw.substring(quote, raw.length - quote) == lit.value) {
      Some(StringLiteral(lit.value, lit.pos.start, lit.pos.start + quote, lit.pos.end - quote))
    } else {
      None
    }
  }

  /** Path for a term (eg. `a`, `a.b.c`) */
  private def path(term: Term): Option[List[String]] =
    term match {
      case Term.Name(name)                 => Some(List(name))
      case Term.Select(qual, Term.Name(n)) => path(qual).map(_ :+ n)
      case _                               => None
    }
}
