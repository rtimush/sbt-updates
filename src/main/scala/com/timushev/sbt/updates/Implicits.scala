package com.timushev.sbt.updates

import sbt._
import sbt.librarymanagement.ModuleFilter

trait Implicits {
  implicit val moduleFilterRemoveValue: Remove.Value[ModuleFilter, ModuleFilter] =
    new Remove.Value[ModuleFilter, ModuleFilter] {
      override def removeValue(a: ModuleFilter, b: ModuleFilter): ModuleFilter = a - b
    }
}
