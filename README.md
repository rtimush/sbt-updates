sbt-updates
==================
Display your sbt project's dependency updates.

Update information is obtained from the maven metadata.
There is also a limited support for Ivy repositories hosted on BinTray.

If your project uses `crossScalaVersions` you will be presented only with updates available for all scala versions.

Requirements
==============
sbt 0.13.9 and later.
sbt 1.x is supported since version 0.3.1.
sbt 2.x is supported since version 0.7.0.

Installation
============
### Stable version
Create a `~/.sbt/1.0/plugins/sbt-updates.sbt` file (for sbt 1.x series), or `~/.sbt/0.13/plugins/sbt-updates.sbt` (for sbt 0.13.x series) with the following content:

```
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "x.x.x")
```

The latest version is [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.timushev.sbt/sbt-updates/badge.svg?subject=sbt-updates)](https://maven-badges.herokuapp.com/maven-central/com.timushev.sbt/sbt-updates/)

### Snapshot version
Choose one of versions available on [Sonatype](https://oss.sonatype.org/content/repositories/snapshots/com/timushev/sbt/sbt-updates_2.12_1.0/). Then create a `~/.sbt/1.0/plugins/sbt-updates.sbt` file (for sbt 1.x series), or `~/.sbt/0.13/plugins/sbt-updates.sbt` (for sbt 0.13.x series) with the following content:

```
resolvers += Resolver.sonatypeRepo("snapshots")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "x.x.x-y+gzzzzzzz-SNAPSHOT")
```

Note, that snapshots are not updated automatically.

Tasks
=====
* `dependencyUpdates`: show a list of project dependencies that can be updated,
* `dependencyUpdatesReport`: writes a list of project dependencies to a file,
* `dependencyUpdatesUpgrade`: upgrades project dependencies to their latest release versions by changing the build sources (see [Upgrading dependencies](#upgrading-dependencies)).

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

Upgrading dependencies
======================
`dependencyUpdatesUpgrade` changes the build sources so that the dependencies of the current project and of the projects
it aggregates are upgraded to their latest release versions. Pre-release and snapshot versions are never selected, and
`dependencyUpdatesFilter` is respected, so you can, for example, limit upgrades to some modules or versions.
```
> dependencyUpdatesUpgrade
[info] Upgraded ch.qos.reload4j:reload4j 1.2.19 -> 1.2.27 (build.sbt:17)
[info] Upgraded org.slf4j:slf4j-api 1.7.30 -> 2.0.20 (build.sbt:2)
[info] Upgraded com.google.code.gson:gson 2.8.0 -> 2.14.0 (project/Dependencies.scala:7)
[warn] Skipped org.example:example at build.sbt:21: the version is not a string literal or a reference
[info] Upgraded 3 dependencies in 2 files, reload the build to apply the changes
> reload
```
Versions are changed where they are declared, which can be:
* inline: `"ch.qos.reload4j" % "reload4j" % "1.2.19"`,
* a `val` or `def` in the same file: `"org.slf4j" % "slf4j-api" % slf4jVersion`,
* a `val` or `def` in another file, e.g. in `project/Dependencies.scala`: `"com.google.code.gson" % "gson" % Dependencies.gsonVersion`,
* a chain of references, e.g. `val logging = Versions.reload4j`.

Build sources are parsed with [Scalameta](https://scalameta.org) rather than matched with regular expressions, so
declarations split across lines or containing comments are handled, while commented-out code and string contents are
ignored. Both Scala 2 and Scala 3 syntax (including significant indentation) are supported. If a build source cannot be
parsed, nothing is changed. A version is only changed when the outcome is certain; otherwise the dependency is skipped
with a warning, and must be upgraded manually:
* the version is computed (e.g. `"1." + minor` or `s"1.$minor"`),
* a reference is ambiguous, e.g. there are several definitions with the same name in different files,
* the declared version is not the one sbt resolves, e.g. because of `dependencyOverrides`,
* a version is shared by several dependencies (e.g. `val akkaVersion = "..."`), and they cannot all be upgraded to
  the same version.

The changes are made in the `.sbt` files of the build and of its projects, and in the `project/` directory. Review them
with your version control system before committing. Plugins can be upgraded the same way, by running
`reload plugins` first.

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
