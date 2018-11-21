package com.timushev.sbt.updates

import sbt.Keys._
import sbt._

object UpdatesPluginIvyScala extends AutoPlugin {

  override def trigger = allRequirements

  lazy val dependencyUpdatesIvyScala =
    taskKey[Option[sbt.IvyScala]]("A task that maps to scalaModuleInfo in scala 2.12")

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    dependencyUpdatesIvyScala := scalaModuleInfo.value
  )
}
