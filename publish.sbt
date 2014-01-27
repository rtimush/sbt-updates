publishMavenStyle := false

publishTo <<= (version) { version: String =>
  val scalasbt = "http://scalasbt.artifactoryonline.com/scalasbt/"
  val (name, url) =
    if (version.contains("-SNAPSHOT"))
      ("sbt-plugin-snapshots-publish", scalasbt + "sbt-plugin-snapshots")
    else
      ("sbt-plugin-releases-publish", scalasbt + "sbt-plugin-releases")
  Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
}
