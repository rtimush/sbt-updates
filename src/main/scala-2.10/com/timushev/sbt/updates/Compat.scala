package com.timushev.sbt.updates

import sbt.ModuleID

object Compat {

  implicit class ModuleIDExt(val module: ModuleID) {
    def withRevision0(revision: String): ModuleID = module.copy(revision = revision)
  }

}
