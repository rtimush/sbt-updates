ProguardPlugin.proguardSettings

proguardLibraryJars <++= (dependencyClasspath in Compile, dependencyClasspath in Embedded) map {
    (ccp, rcp) => ccp.files filterNot rcp.files.toSet
}

proguardInJars <++= (dependencyClasspath in Embedded) map (_.files)

proguardLibraryJars <++= (scalaInstance) map { (si) => Seq(si.libraryJar) }

proguardInJars := Seq()

proguardDefaultArgs := Seq()

proguardOptions ++= Seq(
    "-dontwarn",
    "-dontnote",
    "-dontoptimize",
    "-keep class com.timushev.sbt.updates.**",
    "-keepclassmembers class * { ** MODULE$; }",
    "-repackageclasses 'com.timushev.sbt.updates.libs'"
)

SbtUpdatesBuild.publishMinJar <<= (proguard, minJarPath) map { (_, jar) => jar }

packagedArtifact in (Compile, packageBin) <<= (packagedArtifact in (Compile, packageBin), SbtUpdatesBuild.publishMinJar) map {
    case ((art, _), jar) => (art, jar)
}

dependencyClasspath in Compile <++= dependencyClasspath in Embedded

dependencyClasspath in Test <++= dependencyClasspath in Embedded
