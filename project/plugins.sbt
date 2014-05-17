resolvers ++= Seq(
  "less is" at "http://repo.lessis.me",
  "coda" at "http://repo.codahale.com",
  Resolver.sbtPluginRepo("releases")
)

//addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.8.1")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-proguard" % "0.2.2")
