resolvers ++= Seq(
  "less is" at "http://repo.lessis.me",
  "coda" at "http://repo.codahale.com")

addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.7.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.2")
