package com.timushev.sbt.updates

object Compat {
  type ModuleFilter = sbt.librarymanagement.ModuleFilter
  type DependencyFilter = sbt.librarymanagement.DependencyFilter
  val DependencyFilter = sbt.librarymanagement.DependencyFilter
}
