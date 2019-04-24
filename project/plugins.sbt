resolvers += Resolver.bintrayIvyRepo("rallyhealth", "sbt-plugins")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value

addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.15")
addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.4")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
addSbtPlugin("com.rallyhealth.sbt" % "sbt-git-versioning" % "1.2.1")

