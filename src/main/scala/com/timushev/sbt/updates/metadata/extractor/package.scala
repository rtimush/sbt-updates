package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.versions.Version

package object extractor {
  type VersionExtractor = PartialFunction[String, Seq[Version]]
}
