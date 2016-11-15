sbtPlugin := true

resolvers += Resolver.bintrayIvyRepo("sbt", "sbt-plugin-releases")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.10")

TaskKey[Unit]("check") := {
  import com.timushev.sbt.updates.versions.Version
  val allUpdates = dependencyUpdatesData.value
  allUpdates.keys.find(_.name == "sbt-updates") match {
    case Some(key) =>
      val expected = "0.2.0"
      if (!allUpdates(key).contains(Version(expected))) {
        sys.error(s"Version $expected is not found as an update for com.timushev.sbt:sbt-updates:0.1.10")
      }
    case None =>
      sys.error("No updates for com.timushev.sbt:sbt-updates:0.1.10 found")
  }
  ()
}
