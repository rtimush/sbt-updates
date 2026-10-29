package com.timushev.sbt.updates

import sbt.Keys._
import sbt._
import sbt.util.Uncached

object Compat {

  /** Whether build sources are written in Scala 3, i.e. the plugin runs in sbt 2. */
  val scala3Sources: Boolean = true

  def setSetting[T](data: Def.Settings, scopedKey: ScopedKey[T], value: T): Def.Settings = {
    data.set(scopedKey, value)
  }

  def toDirectCredentials(c: sbt.Credentials) = {
    import sbt.internal.librarymanagement.ivy.IvyCredentials
    IvyCredentials.toDirect(c)
  }

  inline def uncached[T](value: T): T = Uncached(value)
}
