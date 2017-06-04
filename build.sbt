sbtPlugin := true

name := "sbt-updates"
organization := "com.timushev.sbt"

scalacOptions := Seq("-deprecation", "-unchecked", "-feature")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

scriptedSettings
scriptedLaunchOpts += s"-Dsbt.updates.version=${version.value}"

crossSbtVersions := Seq("0.13", "1.0.0-M5", "1.0.0-M6")

enablePlugins(GitVersioning)
git.useGitDescribe := true
git.gitTagToVersionNumber := {
  case VersionNumber(Seq(x, y, z), Seq(), Seq()) => Some(s"$x.$y.$z")
  case VersionNumber(Seq(x, y, z), Seq(since, commit), Seq()) => Some(s"$x.$y.${z + 1}-$since+$commit")
  case _ => None
}

// https://github.com/sbt/sbt/issues/3245
ScriptedPlugin.scripted := Def.inputTask {
  val args = ScriptedPlugin.asInstanceOf[{
    def scriptedParser(f: File): complete.Parser[Seq[String]]
  }].scriptedParser(sbtTestDirectory.value).parsed
  val prereq: Unit = scriptedDependencies.value
  try {
    if((sbtVersion in pluginCrossBuild).value == "1.0.0-M6") {
      ScriptedPlugin.scriptedTests.value.asInstanceOf[{
        def run(
                 x1: File,
                 x2: Boolean,
                 x3: Array[String],
                 x4: File,
                 x5: Array[String],
                 x6: java.util.List[File]
               ): Unit
      }].run(
        sbtTestDirectory.value,
        scriptedBufferLog.value,
        args.toArray,
        sbtLauncher.value,
        scriptedLaunchOpts.value.toArray,
        new java.util.ArrayList()
      )
    } else {
      ScriptedPlugin.scriptedTests.value.asInstanceOf[{
        def run(
                 x1: File,
                 x2: Boolean,
                 x3: Array[String],
                 x4: File,
                 x5: Array[String]
               ): Unit
      }].run(
        sbtTestDirectory.value,
        scriptedBufferLog.value,
        args.toArray,
        sbtLauncher.value,
        scriptedLaunchOpts.value.toArray
      )
    }
  } catch { case e: java.lang.reflect.InvocationTargetException => throw e.getCause }
}.evaluated