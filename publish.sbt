publishMavenStyle := false

def sbtPluginRepo(name: String) = {
  val scalasbt = "http://scalasbt.artifactoryonline.com/scalasbt/"
	(s"sbt-plugin-$name-publish", scalasbt + s"sbt-plugin-$name")
}

publishTo := {
  val snapshot = isSnapshot.value
  val (repoName, repoUrl) = if (snapshot) sbtPluginRepo("snapshots")
               else sbtPluginRepo("releases")
  Some(Resolver.url(repoName, url(repoUrl))(Resolver.ivyStylePatterns))
}
