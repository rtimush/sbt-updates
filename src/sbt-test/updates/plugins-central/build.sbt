import sbt.Defaults.sbtPluginExtra

//resolvers += Resolver.url(s"maven-central-plugin-releases",
//                          new URL(s"https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

libraryDependencies += sbtPluginExtra("org.scalameta" % "sbt-scalafmt" % "2.0.0", "1.0", "2.12")

TaskKey[Unit]("check") := {
  import com.timushev.sbt.updates.versions.Version
  val allUpdates = dependencyUpdatesData.value
  allUpdates.keys.find(_.name == "sbt-scalafmt") match {
    case Some(key) =>
      val expected = "2.0.1"
      if (!allUpdates(key).contains(Version(expected)))
        sys.error(s"Version $expected is not found as an update for org.scalameta:sbt-scalafmt:2.0.0")
    case None =>
      sys.error("No updates for org.scalameta:sbt-scalafmt:2.0.0 found")
  }
  ()
}
