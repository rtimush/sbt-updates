package com.timushev.sbt.updates.versions

import scala.util.matching.Regex

sealed trait Version {
  def major: Long
  def minor: Long
  def patch: Long
  def text: String
}

case class ValidVersion(text: String, releasePart: List[Long], preReleasePart: List[String], buildPart: List[String])
    extends Version {
  def major: Long = releasePart.headOption.getOrElse(0)
  def minor: Long = releasePart.drop(1).headOption.getOrElse(0)
  def patch: Long = releasePart.drop(2).headOption.getOrElse(0)
}

case class InvalidVersion(text: String) extends Version {
  def major: Long = -1
  def minor: Long = -1
  def patch: Long = -1
}

object ReleaseVersion {
  val releaseKeyword: Regex                   = "(?i)final|release".r
  def unapply(v: Version): Option[List[Long]] =
    v match {
      case ValidVersion(_, releasePart, Nil, Nil)                     => Some(releasePart)
      case ValidVersion(_, releasePart, releaseKeyword() :: Nil, Nil) => Some(releasePart)
      case _                                                          => None
    }
}

object PreReleaseVersion {
  def unapply(v: Version): Option[(List[Long], List[String])] =
    v match {
      case ValidVersion(_, releasePart, preReleasePart, Nil) if preReleasePart.nonEmpty =>
        Some(releasePart, preReleasePart)
      case _ => None
    }
}

object PreReleaseBuildVersion {
  def unapply(v: Version): Option[(List[Long], List[String], List[String])] =
    v match {
      case ValidVersion(_, releasePart, preReleasePart, buildPart) if preReleasePart.nonEmpty && buildPart.nonEmpty =>
        Some(releasePart, preReleasePart, buildPart)
      case _ => None
    }
}

object SnapshotVersion {
  def unapply(v: Version): Option[(List[Long], List[String], List[String])] =
    v match {
      case ValidVersion(_, releasePart, preReleasePart, buildPart)
          if preReleasePart.lastOption == Some("SNAPSHOT") || buildPart.lastOption == Some("SNAPSHOT") =>
        Some(releasePart, preReleasePart, buildPart)
      case _ => None
    }
}

object BuildVersion {
  def unapply(v: Version): Option[(List[Long], List[String])] =
    v match {
      case ValidVersion(_, releasePart, Nil, buildPart) if buildPart.nonEmpty => Some(releasePart, buildPart)
      case _                                                                  => None
    }
}

object Version {
  def apply(text: String): Version =
    VersionParser.parse(text) match {
      case Some((release, preRelease, build)) => ValidVersion(text, release, preRelease, build)
      case None                               => InvalidVersion(text)
    }

  implicit def versionOrdering: Ordering[Version] = new VersionOrdering
}

/** Splits `[v]1.2.3[.+][(.|-)pre.release][+build.meta]`; numbers have at most 18 digits, leftover text is invalid. */
object VersionParser {
  private val MaxDigits = 18

  def parse(text: String): Option[(List[Long], List[String], List[String])] = {
    val input = new Input(text.trim)
    input.release().flatMap { release =>
      val preRelease = input.part('.', '-')
      val build      = input.part('+')
      if (input.atEnd) Some((release, preRelease, build)) else None
    }
  }

  final private class Input(s: String) {
    private var i = 0

    def atEnd: Boolean = i == s.length

    private def peek(c: Char): Boolean = i < s.length && s.charAt(i) == c

    private def isSeparator(c: Char): Boolean = c == '.' || c == '-' || c == '+'

    // Digits up to a separator or the end; anything else, like `3m`, is a token rather than a number.
    private def number(): Option[Long] = {
      var j = i
      while (j < s.length && s.charAt(j) >= '0' && s.charAt(j) <= '9') j += 1
      val digits = j - i
      if (digits == 0 || digits > MaxDigits || (j < s.length && !isSeparator(s.charAt(j)))) None
      else {
        val n = s.substring(i, j).toLong
        i = j
        Some(n)
      }
    }

    private def token(): Option[String] = {
      var j = i
      while (j < s.length && !isSeparator(s.charAt(j))) j += 1
      if (j == i) None
      else {
        val t = s.substring(i, j)
        i = j
        Some(t)
      }
    }

    /** `[v]number(.number)*` where a final `.+` stands for the highest number. */
    def release(): Option[List[Long]] = {
      if (peek('v')) i += 1
      number().map { first =>
        val numbers = List.newBuilder[Long]
        numbers += first
        var more = true
        while (more && peek('.')) {
          val dot = i
          i += 1
          if (peek('+')) {
            i += 1
            numbers += Long.MaxValue
            more = false
          } else
            number() match {
              case Some(n) => numbers += n
              case None    => i = dot; more = false
            }
        }
        numbers.result()
      }
    }

    /** `separator token((.|-)token)*`, consuming nothing when the separator isn't followed by a token. */
    def part(separators: Char*): List[String] =
      if (!separators.exists(peek)) Nil
      else {
        val start = i
        i += 1
        token() match {
          case None        => i = start; Nil
          case Some(first) =>
            val tokens = List.newBuilder[String]
            tokens += first
            var more = true
            while (more && (peek('.') || peek('-'))) {
              val separator = i
              i += 1
              token() match {
                case Some(t) => tokens += t
                case None    => i = separator; more = false
              }
            }
            tokens.result()
        }
      }
  }
}
