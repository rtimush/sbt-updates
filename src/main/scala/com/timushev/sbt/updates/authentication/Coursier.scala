package com.timushev.sbt.updates.authentication

import com.timushev.sbt.updates.CompatUtils
import sbt._

object Coursier {

  // lm-coursier isn't on the compile classpath of the sbt 1.x plugin, so its configuration is read reflectively.
  def dependencyUpdatesCsrConfigurationTask: Def.Initialize[Task[Option[AnyRef]]] =
    CompatUtils.optionalTask[AnyRef]("csrConfiguration")

  private[authentication] def get[T](target: AnyRef, member: String): T =
    target.getClass.getMethod(member).invoke(target).asInstanceOf[T]

}
