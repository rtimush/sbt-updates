package com.timushev.sbt.updates

import sbt.ModuleID

object Compat {

  type ModuleFilter = sbt.librarymanagement.ModuleFilter
  type DependencyFilter = sbt.librarymanagement.DependencyFilter
  val DependencyFilter = sbt.librarymanagement.DependencyFilter

  implicit class ModuleIDExt(val module: ModuleID) {
    def withRevision0(revision: String): ModuleID = module.withRevision(revision)
  }

}
