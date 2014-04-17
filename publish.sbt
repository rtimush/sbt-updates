publishMavenStyle := false

publishTo := {
  val scalasbt = "http://scalasbt.artifactoryonline.com/scalasbt/"
  val snapshot = isSnapshot.value
  val (name, url) =
    if (snapshot)
      ("sbt-plugin-snapshots-publish", scalasbt + "sbt-plugin-snapshots")
    else
      ("sbt-plugin-releases-publish", scalasbt + "sbt-plugin-releases")
  Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
}
