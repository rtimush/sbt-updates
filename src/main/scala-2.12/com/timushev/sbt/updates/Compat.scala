package com.timushev.sbt.updates

import sbt.Keys._
import sbt._

object Compat {
  def setSetting[T](
      data: sbt.internal.util.Settings[sbt.Scope],
      scopedKey: ScopedKey[T],
      value: T
  ): sbt.internal.util.Settings[sbt.Scope] =
    data.set(scopedKey.scope, scopedKey.key, value)

  def toDirectCredentials(c: sbt.Credentials): sbt.DirectCredentials =
    sbt.Credentials.toDirect(c)

  def uncached[T](task: => T): T = task
}
