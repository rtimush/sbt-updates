publishMavenStyle := false

publishTo := {
  def scalasbt(name: String) =
    (s"sbt-plugin-$name-publish", s"http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-$name")
  val snapshot = isSnapshot.value
  val (name, url) = scalasbt(if (snapshot) "snapshots" else "releases")
  Some(Resolver.url(name, new URL(url))(Resolver.ivyStylePatterns))
}
