sbt-updates
==================
Display your sbt project's dependency updates.

Update information is obtained from the Maven metadata of your resolvers and, where a repository serves them, from its
directory listings, which also cover Ivy repositories.

If your project uses `crossScalaVersions` you will be presented only with updates available for all scala versions.

Compatibility
=============
sbt-updates runs inside sbt, so every release is built separately for each sbt series it supports, with the Scala
version of that series. The Scala version of your own project doesn't matter.

### Which version to use

| Your sbt         | sbt-updates | Plugin built with     |
|------------------|-------------|-----------------------|
| 2.x              | 0.7.1       | Scala 3.8.4, Java 17+ |
| 1.9 or later     | 0.7.1       | Scala 2.12, Java 8+   |
| 1.0 – 1.8        | 0.6.4 ¹     | Scala 2.12, Java 8+   |
| 0.13.9 – 0.13.18 | 0.6.4       | Scala 2.10, Java 6+   |
| 0.13.8           | 0.1.10      | Scala 2.10, Java 6+   |
| 0.13.7           | 0.1.8       | Scala 2.10, Java 6+   |
| 0.13.0 – 0.13.6  | 0.1.7       | Scala 2.10, Java 6+   |
| 0.12.4           | 0.1.6       | Scala 2.9.2, Java 5+  |
| 0.12.1 – 0.12.3  | 0.1.1       | Scala 2.9.2, Java 5+  |
| 0.12.0           | 0.1.0       | Scala 2.9.2, Java 5+  |
| 0.11.2, 0.11.3   | 0.1.0       | Scala 2.9.1, Java 5+  |

### All releases

Each cell is the oldest sbt version the release supports: the sbt it was compiled against, or an older one it was
tested with in CI (then shown as `tested (built on)`). A dash means the release wasn't built for that sbt series.
The Java versions are what the plugin's bytecode needs; sbt itself may need a newer one.

| sbt-updates    | Released          | sbt 0.11<br>Scala 2.9.1, Java 5+ | sbt 0.12<br>Scala 2.9.2, Java 5+ | sbt 0.13<br>Scala 2.10, Java 6+ | sbt 1.x<br>Scala 2.12, Java 8+ | sbt 2.x<br>Scala 3.8.4, Java 17+ |
|----------------|-------------------|----------------------------------|----------------------------------|---------------------------------|--------------------------------|----------------------------------|
| 0.7.0 – 0.7.1  | 2026-06 – 2026-10 | –                                | –                                | –                               | 1.9.0 ¹                        | 2.0.0                            |
| 0.4.0 – 0.6.4  | 2019-02 – 2022-10 | –                                | –                                | 0.13.9 (built on 0.13.16)       | 1.0.0 (built on 1.1.5)         | –                                |
| 0.3.4          | 2018-01           | –                                | –                                | 0.13.9 (built on 0.13.16)       | 1.0.0 (built on 1.1.0)         | –                                |
| 0.3.3          | 2017-10           | –                                | –                                | 0.13.15 (built on 0.13.16)      | 1.0.2                          | –                                |
| 0.3.2          | 2017-09           | –                                | –                                | 0.13.16                         | 1.0.2                          | –                                |
| 0.3.1          | 2017-06           | –                                | –                                | 0.13.15                         | 1.0.0-RC3 ²                    | –                                |
| 0.3.0          | 2016-11           | –                                | –                                | 0.13.13                         | –                              | –                                |
| 0.2.0          | 2016-08           | –                                | –                                | 0.13.12                         | –                              | –                                |
| 0.1.9 – 0.1.10 | 2015-07 – 2015-10 | –                                | –                                | 0.13.8                          | –                              | –                                |
| 0.1.8          | 2015-02           | –                                | –                                | 0.13.7                          | –                              | –                                |
| 0.1.7          | 2015-02           | –                                | –                                | 0.13.0                          | –                              | –                                |
| 0.1.2 – 0.1.6  | 2013-07 – 2014-05 | –                                | 0.12.4                           | 0.13.0 ³                        | –                              | –                                |
| 0.1.1          | 2013-06           | –                                | 0.12.1                           | –                               | –                              | –                                |
| 0.1.0          | 2012-11           | 0.11.2, 0.11.3                   | 0.12.0                           | –                               | –                              | –                                |

¹ The sbt 1.x plugins of 0.7.0 and 0.7.1 are still compiled against sbt 1.1.5, but they are published only in the
Maven-compatible layout that sbt 1.9 introduced. Older sbt versions look for the legacy file name and fail with:
```
[error] sbt.librarymanagement.ResolveException: Error downloading com.timushev.sbt:sbt-updates;sbtVersion=1.0;scalaVersion=2.12:0.7.0
[error]   not found: https://repo1.maven.org/maven2/com/timushev/sbt/sbt-updates_2.12_1.0/0.7.0/sbt-updates-0.7.0.pom
```
Use 0.6.4 with sbt 1.0 – 1.8.

² 0.3.1 was also published for sbt 1.0.0-M5 and 1.0.0-M6.

³ 0.1.2 was built on sbt 0.13.0-RC3.

### Tested in CI

| sbt-updates         | sbt versions                                                     | JDKs                                        |
|---------------------|------------------------------------------------------------------|---------------------------------------------|
| 0.7.0 – 0.7.1       | 1.11.7, 2.0.0                                                    | sbt 1.x: 8, 11, 14, 17; sbt 2.x: 17         |
| 0.5.2 – 0.6.4       | 0.13.9, 0.13.16, 1.0.0, and the release's own sbt (1.4.7 – 1.7.1) | sbt 0.13: 8; sbt 1.x: 8, 11, 14             |
| 0.5.1               | 0.13.9, 0.13.16, 1.0.0, 1.3.10                                   | sbt 0.13: 8; sbt 1.x: 8, 11, 14             |
| 0.4.0 – 0.5.0       | 0.13.9, 0.13.16, 1.0.0, 1.1.5                                    | 8                                           |
| 0.3.4               | 0.13.9, 0.13.16, 1.0.0, 1.1.0                                    | sbt 0.13: 6; sbt 1.x: 8                     |
| 0.3.3               | 0.13.15, 0.13.16, 1.0.2                                          | sbt 0.13: 6, 7, 8; sbt 1.x: 8               |
| 0.3.2               | 0.13.16, 1.0.2                                                   | sbt 0.13: 6, 7, 8; sbt 1.x: 8               |
| 0.3.1               | 0.13.15, 1.0.0-M5, 1.0.0-M6                                      | sbt 0.13: 6, 7, 8; sbt 1.x: 8               |
| 0.1.9 – 0.3.0       | the release's own sbt (0.13.8 – 0.13.13)                         | 7, 8                                        |
| 0.1.0 – 0.1.8       | not tested in CI                                                 |                                             |

### Notes
* **Repository credentials on Java 9 and later.** 0.1.10 – 0.3.1 encode credentials with JAXB, which Java 9 and later
  don't provide by default, so resolvers with credentials fail there. 0.3.2 and later fall back to `java.util.Base64`.
* **Where releases are published.** 0.1.0 – 0.5.1 were published to Bintray and are now served from the
  [sbt plugin repository](https://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/com.timushev.sbt/sbt-updates/);
  0.5.2 and later are on Maven Central. sbt resolves plugins from both by default.
* **Scala version of the sbt 2.x plugin.** Every sbt 2.0.x release runs on Scala 3.8.4, and the plugin must be compiled
  with the same Scala: Scala 3.3 can't read sbt 2's compiled API, and sbt 2.0.x can't read a plugin compiled with
  Scala 3.9. sbt resolves a single sbt 2.x artifact (`sbt-updates_sbt2_3`) per release, so the plugin can't be
  cross-built for several Scala 3 versions.

Installation
============
### Stable version
Pick the version for your sbt from [Compatibility](#compatibility), then create a `~/.sbt/1.0/plugins/sbt-updates.sbt`
file (for sbt 1.x), or `~/.sbt/2/plugins/sbt-updates.sbt` (for sbt 2.x) with the following content:

```
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "x.x.x")
```

The latest release for each sbt series (sbt 1.0 – 1.8 need 0.6.4, see [Compatibility](#compatibility)). Maven Central
lists a release first; Scaladex can take a while to catch up.

| sbt | Maven Central | Scaladex |
|-----|---------------|----------|
| 2.x | [![sbt-updates for sbt 2.x on Maven Central](https://img.shields.io/maven-central/v/com.timushev.sbt/sbt-updates_sbt2_3?label=sbt-updates)](https://central.sonatype.com/artifact/com.timushev.sbt/sbt-updates_sbt2_3) | [![sbt-updates for sbt 2.x on Scaladex](https://index.scala-lang.org/rtimush/sbt-updates/sbt-updates/latest-by-scala-version.svg?platform=sbt2)](https://index.scala-lang.org/rtimush/sbt-updates/artifacts/sbt-updates?stableOnly=true&binary-version=_sbt2_3) |
| 1.x | [![sbt-updates for sbt 1.x on Maven Central](https://img.shields.io/maven-central/v/com.timushev.sbt/sbt-updates_2.12_1.0?label=sbt-updates)](https://central.sonatype.com/artifact/com.timushev.sbt/sbt-updates_2.12_1.0) | [![sbt-updates for sbt 1.x on Scaladex](https://index.scala-lang.org/rtimush/sbt-updates/sbt-updates/latest-by-scala-version.svg?platform=sbt1)](https://index.scala-lang.org/rtimush/sbt-updates/artifacts/sbt-updates?stableOnly=true&binary-version=_2.12_1.0) |

### Snapshot version
Choose one of the versions listed in the [snapshot repository](https://central.sonatype.com/repository/maven-snapshots/com/timushev/sbt/sbt-updates_2.12_1.0/maven-metadata.xml). Then create the same file with the following content:

```
resolvers += "Central Snapshots" at "https://central.sonatype.com/repository/maven-snapshots/"
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "x.x.x-n-hash-SNAPSHOT")
```

Note, that snapshots are not updated automatically.

Tasks
=====
* `dependencyUpdates`: show a list of project dependencies that can be updated,
* `dependencyUpdatesReport`: writes a list of project dependencies to a file.

Settings
========
* `dependencyUpdatesReportFile`: report file location, `target/dependency-updates.txt` by default.
* `dependencyUpdatesFilter`: filter matching dependencies that should be included to update reporting.
* `dependencyUpdatesFailBuild`: `dependencyUpdates` task will fail a build if updates found.
* `dependencyAllowPreRelease`: when enabled, pre-release dependencies will be reported as well.

#### Deprecated Settings
* `dependencyUpdatesExclusions`: filter matching dependencies that should be excluded from update reporting.

Exclusions
==========
You can exclude some modules from update checking:
```
dependencyUpdatesFilter -= moduleFilter(organization = "org.scala-lang")
```

sbt plugin updates
=============
If `sbt-updates` is installed in your global file you can get updates for sbt plugins by using the `reload plugins` command:
```
> reload plugins
...
> dependencyUpdates
[info] Found 2 dependency updates for project
[info]   com.timushev.sbt:sbt-updates          : 0.3.0  -> 0.3.4 -> 0.4.3
[info]   org.scala-lang:scala-library:provided : 2.10.6          -> 2.12.4
> reload return
```
Only plugins defined in a project are checked, there is currently no way to check updates for global plugins.

You can also check updates for dependencies and sbt plugins with:
```
sbt ";dependencyUpdates; reload plugins; dependencyUpdates"
```

Usage as project plugin
=======================
It is preferred to use sbt-updates as a global plugin. Nevertheless, there might be cases when you want to use sbt-updates
as a project plugin. In that case, add the plugin definition to `project/sbt-updates.sbt`. You can then use dependencyUpdates
target to find updates for your project. But this way you won't be able to check sbt plugin updates. In order to check both
dependency updates and sbt plugin updates, add the plugin to both project and meta project i.e `project/sbt-updates.sbt`
 and `project/project/sbt-updates.sbt` and run:
```
sbt ";dependencyUpdates; reload plugins; dependencyUpdates; reload return"
```

Publishing
==========
`sbt-updates` relies on the repository Maven metadata. If you want to get update notifications
 for artifacts published by other sbt projects, you should ensure that metadata is updated
 correctly. One possible way to achieve this is to use [sbt-aether-deploy](https://github.com/arktekk/sbt-aether-deploy).

Example
=======

In order from left, the result shows current version, patch update version, minor update version and major update version.

```
> dependencyUpdates
[info] Found 3 dependency updates for test-project
[info]   ch.qos.logback:logback-classic : 0.8   -> 0.8.1 -> 0.9.30 -> 1.0.13
[info]   org.scala-lang:scala-library   : 2.9.1 -> 2.9.3 -> 2.10.3
[info]   org.slf4j:slf4j-api            : 1.6.4 -> 1.6.6 -> 1.7.5
```
