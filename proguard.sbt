proguardSettings

ProguardKeys.options in Proguard ++= Seq(
    "-dontwarn",
    "-dontnote",
    "-dontoptimize",
    "-keep class com.timushev.sbt.updates.**",
    "-keepclassmembers class ** { *; }",
    "-repackageclasses 'com.timushev.sbt.updates.libs'"
)

ProguardKeys.libraries in Proguard ++= {
    (dependencyClasspath in Compile).value.files filterNot (dependencyClasspath in Embedded).value.files.toSet
}

ProguardKeys.inputs in Proguard ++= {
    (dependencyClasspath in Embedded).value.files filterNot (_ == scalaInstance.value.libraryJar)
}

SbtUpdatesBuild.publishMinJar := (ProguardKeys.proguard in Proguard).value.head

packagedArtifact in (Compile, packageBin) := ((packagedArtifact in (Compile, packageBin)).value._1, SbtUpdatesBuild.publishMinJar.value)

dependencyClasspath in Compile <++= dependencyClasspath in Embedded

dependencyClasspath in Test <++= dependencyClasspath in Embedded
