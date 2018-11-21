package com.timushev.sbt.updates

object IvyScala {
  def apply(): sbt.IvyScala = {
    new sbt.IvyScala(scalaFullVersion = "2.10.7",
      scalaBinaryVersion = "2.10",
      configurations = Vector.empty,
      checkExplicit = false,
      filterImplicit = false,
      overrideScalaVersion = false)
  }
}
