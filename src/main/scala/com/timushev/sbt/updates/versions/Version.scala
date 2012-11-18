package com.timushev.sbt.updates.versions

import util.parsing.combinator.RegexParsers

sealed trait Version {
  def major: Long
  def minor: Long
  def patch: Long
}

case class ValidVersion(text: String, releasePart: List[Long], preReleasePart: List[String], buildPart: List[String])
  extends Version {
  def major = releasePart.headOption getOrElse 0
  def minor = releasePart.drop(1).headOption getOrElse 1
  def patch = releasePart.drop(2).headOption getOrElse 1
  override def toString = text
}

case class InvalidVersion(text: String) extends Version {
  def major = -1
  def minor = -1
  def patch = -1
}

object ReleaseVersion {
  def unapply(v: Version) = v match {
    case ValidVersion(_, releasePart, Nil, Nil) => Some(releasePart)
    case _ => None
  }
}

object PreReleaseVersion {
  def unapply(v: Version) = v match {
    case ValidVersion(_, releasePart, preReleasePart, Nil) if !preReleasePart.isEmpty => Some(releasePart, preReleasePart)
    case _ => None
  }
}


object PreReleaseBuildVersion {
  def unapply(v: Version) = v match {
    case ValidVersion(_, releasePart, preReleasePart, buildPart) if !preReleasePart.isEmpty && !buildPart.isEmpty => Some(releasePart, preReleasePart, buildPart)
    case _ => None
  }
}

object BuildVersion {
  def unapply(v: Version) = v match {
    case ValidVersion(_, releasePart, Nil, buildPart) if !buildPart.isEmpty => Some(releasePart, buildPart)
    case _ => None
  }
}

object Version {
  def apply(text: String): Version = {
    VersionParser.parse(text)
      .map {case (a, b, c) => new ValidVersion(text, a, b, c)}
      .getOrElse {new InvalidVersion(text)}
  }
  implicit def versionOrdering: Ordering[Version] = new VersionOrdering
}

object VersionParser extends RegexParsers {

  private val token = """[^-+.]+""".r
  private val number = """\d{1,18}(?=[-+.]|$)""".r ^^ {_.toLong}

  private val numericPart: Parser[List[Long]] = number ~ (("." ~> number) *) ^^ {case h ~ t => h :: t}
  private val part: Parser[List[String]] = token ~ (("." ~> token) *) ^^ {case h ~ t => h :: t}

  private val version: Parser[(List[Long], List[String], List[String])] =
    numericPart ~ ((("." | "-") ~> part) ?) ~ (("+" ~> part) ?) ^^ {case a ~ b ~ c => (a, b.getOrElse(Nil), c.getOrElse(Nil))}

  def parse(text: String) = parseAll(version, text)

}

