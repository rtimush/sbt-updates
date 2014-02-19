proguardSettings

ProguardKeys.options in Proguard ++= Seq(
    "-dontwarn",
    "-dontnote",
    "-dontoptimize",
    "-keep class com.timushev.sbt.updates.**",
    "-keepclassmembers class ** { *; }",
    "-repackageclasses 'com.timushev.sbt.updates.libs'"
)

ProguardKeys.libraries in Proguard <++= (dependencyClasspath in Compile, dependencyClasspath in Embedded) map {
    (ccp, rcp) => ccp.files filterNot rcp.files.toSet
}

ProguardKeys.inputs in Proguard <++= (dependencyClasspath in Embedded, scalaInstance) map {
    (dcp, si) => dcp.files filterNot (_ == si.libraryJar)
}

SbtUpdatesBuild.publishMinJar <<= (ProguardKeys.proguard in Proguard) map (_.head)

packagedArtifact in (Compile, packageBin) <<= (packagedArtifact in (Compile, packageBin), SbtUpdatesBuild.publishMinJar) map {
    case ((art, _), jar) => (art, jar)
}

dependencyClasspath in Compile <++= dependencyClasspath in Embedded

dependencyClasspath in Test <++= dependencyClasspath in Embedded
