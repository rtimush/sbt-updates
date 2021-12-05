package com.timushev.sbt.updates

import sbt._
import sbt.Keys._

object CompatUtils {

  def optionalTask[T](label: String): Def.Initialize[Task[Option[T]]] =
    Def.taskDyn {
      buildStructure.value.settings.map(_.key.key).find(_.label == label) match {
        case Some(key) =>
          Scoped
            .scopedTask(ThisScope, key.asInstanceOf[AttributeKey[Task[T]]])
            .map(Some.apply)

        case None =>
          Def.task(None)
      }
    }

}
