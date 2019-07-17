import sbt._
import sbt.Keys._

object RootDependencies {
  val deps = libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.0"
}
