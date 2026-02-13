package com.timushev.sbt.updates

import sbt.Keys._
import sbt._
import sbt.util.Uncached

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

  def setSetting[T](data: Def.Settings, scopedKey: ScopedKey[T], value: T): Def.Settings = {
    data.set(scopedKey, value)
  }

  def toDirectCredentials(c: sbt.Credentials) = {
    import sbt.internal.librarymanagement.ivy.IvyCredentials
    IvyCredentials.toDirect(c)
  }

  inline def uncached[T](value: T): T = Uncached(value)
}
