sbt-updates [![Build Status](https://travis-ci.org/rtimush/sbt-updates.svg?branch=master)](https://travis-ci.org/rtimush/sbt-updates) [![Gitter](https://badges.gitter.im/rtimush/sbt-updates.svg)](https://gitter.im/rtimush/sbt-updates?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
==================
Display your SBT project's dependency updates.

Update information is obtained from the maven metadata.
There is also a limited support for Ivy repositories hosted on BinTray.
 
If your project uses `crossScalaVersions` you will be presented only with updates available for all scala versions. 

Requirements
==============
SBT 0.13.9 and later. SBT 1.x is supported since version 0.3.1.

Installation
============
### Stable version
Add the following line to one of these files:
- The project-specific file at `project/sbt-updates.sbt`
- Your global file at `~/.sbt/0.13/plugins/sbt-updates.sbt` (for SBT 0.13.x series)
  or at `~/.sbt/1.0/plugins/sbt-updates.sbt` (for SBT 1.x series)

```
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.0")
```

### Snapshot version
Choose one of versions available on [BinTray](https://bintray.com/rtimush/sbt-plugin-snapshots/sbt-updates/view)
or the [latest](https://bintray.com/rtimush/sbt-plugin-snapshots/sbt-updates/_latestVersion) one.
Add the following lines to one of these files:
- The project-specific file at `project/sbt-updates.sbt`
- Your global file at `~/.sbt/0.13/plugins/sbt-updates.sbt`  (for SBT 0.13 series)
  or at `~/.sbt/1.0/plugins/sbt-updates.sbt` (for SBT 1.x series)

```
resolvers += Resolver.bintrayIvyRepo("rtimush", "sbt-plugin-snapshots")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "x.x.x-y+gzzzzzzz")
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

SBT plugin updates
=============
If `sbt-updates` is installed in your global file you can get updates for SBT plugins by using the `reload plugins` command:
```
> reload plugins
...
> dependencyUpdates
[info] Found 2 dependency updates for project
[info]   com.timushev.sbt:sbt-updates          : 0.3.0  -> 0.3.4 -> 0.4.0
[info]   org.scala-lang:scala-library:provided : 2.10.6          -> 2.12.4
> reload return
```
Only plugins defined in a project are checked, there is currently no way to check updates for global plugins.

You can also check updates for dependencies and SBT plugins with:
```
sbt ";dependencyUpdates; reload plugins; dependencyUpdates"
```

Publishing
==========
`sbt-updates` relies on the repository Maven metadata. If you want to get update notifications
 for artifacts published by other SBT projects, you should ensure that metadata is updated
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
