publishMavenStyle := false

bintrayRepository := (if (isSnapshot.value) "sbt-plugin-snapshots" else "sbt-plugins")

bintrayOrganization in bintray := None

bintrayReleaseOnPublish := isSnapshot.value

licenses += (("BSD 3-Clause", url("https://github.com/rtimush/sbt-updates/blob/master/LICENSE")))
