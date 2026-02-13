package com.timushev.sbt.updates.metadata.extractor

import com.timushev.sbt.updates.metadata.extractor.HtmlVersionExtractor._
import com.timushev.sbt.updates.versions.Version

import scala.util.matching.Regex

object HtmlVersionExtractor {
  val Pattern: Regex = "<a[^>]+href=\":?([^/]*)/\"[^>]*>\\1/?</a>".r
}

class HtmlVersionExtractor extends VersionExtractor {
  override def isDefinedAt(data: String): Boolean =
    Pattern.findAllIn(data).nonEmpty

  override def apply(data: String): Seq[Version] =
    Pattern.findAllMatchIn(data).map(_.group(1)).map(Version.apply).toVector
}
