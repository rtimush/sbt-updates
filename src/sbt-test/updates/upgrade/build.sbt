// libraryDependencies += "ch.qos.reload4j" % "reload4j" % "1.2.19"
val slf4jVersion = "1.7.30"

// holds the same value as slf4jVersion, but is not used by a dependency: must not be changed
val unrelatedVersion = "1.7.30"

lazy val core = project
  .settings(
    libraryDependencies += "org.apache.commons" % "commons-text" % // a multi-line declaration
      "1.9"
  )

// shared by projects on different Scala versions, which see different latest releases
lazy val crossSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel"          %% "spire"                      % "0.16.2", // 2.12 releases stop at 0.17.0
    "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.0"   // never released for 2.12
  )
)

lazy val scala212 = project.settings(crossSettings, scalaVersion := "2.12.20")
lazy val scala213 = project.settings(crossSettings, scalaVersion := "2.13.16")

lazy val root = (project in file("."))
  .aggregate(core, scala212, scala213)
  .settings(
    libraryDependencies ++= Seq(
      "ch.qos.reload4j"      % "reload4j"  % "1.2.19",
      "org.slf4j"            % "slf4j-api" % slf4jVersion,
      "com.google.code.gson" % "gson"      % Dependencies.gsonVersion
    )
  )
  .settings(Check.settings)
