package com.timushev.sbt.updates.versions

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

sealed trait Version {
  def major: Long
  def minor: Long
  def patch: Long
}

case class ValidVersion(text: String, releasePart: List[Long], preReleasePart: List[String], buildPart: List[String])
    extends Version {
  def major: Long = releasePart.headOption.getOrElse(0)
  def minor: Long = releasePart.drop(1).headOption.getOrElse(1)
  def patch: Long = releasePart.drop(2).headOption.getOrElse(1)
  override def toString: String = text
}

case class InvalidVersion(text: String) extends Version {
  def major: Long = -1
  def minor: Long = -1
  def patch: Long = -1
}

object ReleaseVersion {
  val releaseKeyword: Regex = "(?i)final|release".r
  def unapply(v: Version): Option[List[Long]] = v match {
    case ValidVersion(_, releasePart, Nil, Nil)                     => Some(releasePart)
    case ValidVersion(_, releasePart, releaseKeyword() :: Nil, Nil) => Some(releasePart)
    case _                                                          => None
  }
}

object PreReleaseVersion {
  def unapply(v: Version): Option[(List[Long], List[String])] = v match {
    case ValidVersion(_, releasePart, preReleasePart, Nil) if preReleasePart.nonEmpty =>
      Some(releasePart, preReleasePart)
    case _ => None
  }
}

object PreReleaseBuildVersion {
  def unapply(v: Version): Option[(List[Long], List[String], List[String])] = v match {
    case ValidVersion(_, releasePart, preReleasePart, buildPart) if preReleasePart.nonEmpty && buildPart.nonEmpty =>
      Some(releasePart, preReleasePart, buildPart)
    case _ => None
  }
}

object SnapshotVersion {
  def unapply(v: Version): Option[(List[Long], List[String], List[String])] = v match {
    case ValidVersion(_, releasePart, preReleasePart, buildPart)
        if preReleasePart.lastOption == Some("SNAPSHOT") || buildPart.lastOption == Some("SNAPSHOT") =>
      Some(releasePart, preReleasePart, buildPart)
    case _ => None
  }
}

object BuildVersion {
  def unapply(v: Version): Option[(List[Long], List[String])] = v match {
    case ValidVersion(_, releasePart, Nil, buildPart) if buildPart.nonEmpty => Some(releasePart, buildPart)
    case _                                                                  => None
  }
}

object Version {
  def apply(text: String): Version = synchronized {
    VersionParser
      .parse(text)
      .map { case (a, b, c) => ValidVersion(text, a, b, c) }
      .getOrElse {
        InvalidVersion(text)
      }
  }

  implicit def versionOrdering: Ordering[Version] = new VersionOrdering
}

object VersionParser extends RegexParsers {

  private val token = """[^-+.]+""".r
  private val number = """\d{1,18}(?=[-+.]|$)""".r ^^ (_.toLong)
  private val plusAsPatchValue = """\+""".r ^^ (_ => Long.MaxValue)

  private val numericPart: Parser[List[Long]] = number ~ ("." ~> number) ~ ("." ~> (number | plusAsPatchValue)).* ^^ {
    case h ~ m ~ t => h :: m :: t
  }
  private val part: Parser[List[String]] = token ~ (("." | "-") ~> token).* ^^ { case h ~ t => h :: t }

  private val version: Parser[(List[Long], List[String], List[String])] =
    numericPart ~ (("." | "-") ~> part).? ~ ("+" ~> part).? ^^ {
      case a ~ b ~ c => (a, b.getOrElse(Nil), c.getOrElse(Nil))
    }

  def parse(text: String): VersionParser.ParseResult[(List[Long], List[String], List[String])] = parseAll(version, text)

}
