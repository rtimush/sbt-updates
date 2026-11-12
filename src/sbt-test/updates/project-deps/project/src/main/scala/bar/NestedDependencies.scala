package bar

import sbt._
import sbt.Keys._

object NestedDependencies {
  val deps = libraryDependencies += "org.typelevel" %% "cats-kernel" % "2.9.0"
}
