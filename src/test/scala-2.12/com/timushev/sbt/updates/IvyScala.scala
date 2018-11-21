package com.timushev.sbt.updates
import sbt.librarymanagement.ScalaModuleInfo

object IvyScala {
  def apply(): sbt.IvyScala = {
    ScalaModuleInfo(scalaFullVersion = "2.12.7",
                    scalaBinaryVersion = "2.12",
                    configurations = Vector.empty,
                    checkExplicit = false,
                    filterImplicit = false,
                    overrideScalaVersion = false)
  }
}
