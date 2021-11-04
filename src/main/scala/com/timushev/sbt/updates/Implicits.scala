package com.timushev.sbt.updates

import sbt._
import sbt.librarymanagement.ModuleFilter

trait Implicits {
  implicit val moduleFilterRemoveValue: Remove.Value[ModuleFilter, ModuleFilter] =
    (a: ModuleFilter, b: ModuleFilter) => a - b
}
