package com.timushev.sbt.updates.metadata.extractor

import com.timushev.sbt.updates.metadata.extractor.BintrayVersionExtractor._
import com.timushev.sbt.updates.versions.Version

import scala.util.matching.Regex

object BintrayVersionExtractor {
  val Pattern: Regex = "<a onclick=\"navi\\(event\\)\" href=\":([^/]*)/\" rel=\"nofollow\">\\1/</a>".r
}

class BintrayVersionExtractor extends VersionExtractor {

  override def isDefinedAt(data: String): Boolean = {
    data.contains("function navi(e)")
  }

  override def apply(data: String): Seq[Version] = {
    Pattern.findAllMatchIn(data).map(_.group(1)).map(Version.apply).to[Vector]
  }

}
