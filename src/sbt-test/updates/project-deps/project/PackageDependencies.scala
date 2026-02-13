package foo

import sbt._
import sbt.Keys._

object PackageDependencies {
  val deps = libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0"
}
