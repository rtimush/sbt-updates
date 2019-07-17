package foo

import sbt._
import sbt.Keys._

object PackageDependencies {
  val deps = libraryDependencies += "org.specs2" %% "specs2" % "3.1"
}
