package com.timushev.sbt.updates

import sbt.Keys._
import sbt._

object Compat {
  type ModuleFilter     = sbt.librarymanagement.ModuleFilter
  type DependencyFilter = sbt.librarymanagement.DependencyFilter
  val DependencyFilter = sbt.librarymanagement.DependencyFilter

  implicit class ModuleIDExt(val module: ModuleID) {
    def withRevision0(revision: String): ModuleID = module.withRevision(revision)
  }

  def createScopedKey[T](settingKey: SettingKey[T], projRef: ProjectRef): ScopedKey[T] = {
    val scope = GlobalScope.copy(project = Select(projRef))
    Scoped.scopedSetting(scope, settingKey.key).scopedKey
  }

  def setSetting[T](data: sbt.internal.util.Settings[sbt.Scope], scopedKey: ScopedKey[T], value: T): sbt.internal.util.Settings[sbt.Scope] = {
    data.set(scopedKey.scope, scopedKey.key, value)
  }

  def toDirectCredentials(c: sbt.Credentials): sbt.DirectCredentials = {
    sbt.Credentials.toDirect(c)
  }
}
