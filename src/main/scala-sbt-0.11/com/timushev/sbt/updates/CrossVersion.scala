package com.timushev.sbt.updates

import sbt.ModuleID

object CrossVersion {
  def apply(scalaVersion: String, scalaBinaryVersion: String): ModuleID => ModuleID = {
    m =>
      m.copy(name = m.name + "_" + scalaBinaryVersion)
  }
}
