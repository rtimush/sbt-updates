sbt-updates [![Build Status](https://travis-ci.org/rtimush/sbt-updates.svg?branch=master)](https://travis-ci.org/rtimush/sbt-updates)
==================
Display your SBT project's dependency updates.

Requirements
==============
SBT 0.13.5 and later

Note: use version 0.1.0 for SBT 0.11.x, version 0.1.6 for SBT 0.12.x, version 0.1.7 for SBT 0.13.0-0.13.2.

Installation
============
### Stable version
Add the following line to one of these files:
- The project-specific file at `project/sbt-updates.sbt`
- Your global file at `~/.sbt/0.13/plugins/sbt-updates.sbt`

```
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.10")
```

### Snapshot version
Choose one of versions available on [BinTray](https://bintray.com/rtimush/sbt-plugin-snapshots/sbt-updates/view)
or the [latest](https://bintray.com/rtimush/sbt-plugin-snapshots/sbt-updates/_latestVersion) one.
Add the following lines to one of these files:
- The project-specific file at `project/sbt-updates.sbt`
- Your global file at `~/.sbt/0.13/plugins/sbt-updates.sbt`

```
resolvers += Resolver.url("rtimush/sbt-plugin-snapshots", new URL("https://dl.bintray.com/rtimush/sbt-plugin-snapshots/"))(Resolver.ivyStylePatterns)
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.9-6-g5a7705c")
```

Note, that snapshots are not updated automatically.

Tasks
=====
* `dependencyUpdates`: show a list of project dependencies that can be updated,
* `dependencyUpdatesReport`: writes a list of project dependencies to a file.

Settings
========
* `dependencyUpdatesReportFile`: report file location, `target/dependency-updates.txt` by default.
* `dependencyUpdatesExclusions`: filter matching dependencies that should be excluded from update reporting.
* `dependencyUpdatesFailBuild`: `dependencyUpdates` task will fail a build if updates found.
* `dependencyAllowPreRelease`: when enabled, pre-release dependencies will be reported as well.

Exclusions
==========
You can exclude some modules from update checking:
```
dependencyUpdatesExclusions := moduleFilter(organization = "org.scala-lang")
```

Example
=======
```
> dependencyUpdates
[info] Found 3 dependency updates for test-project
[info]   ch.qos.logback:logback-classic : 0.8   -> 0.8.1 -> 0.9.30 -> 1.0.13
[info]   org.scala-lang:scala-library   : 2.9.1 -> 2.9.3 -> 2.10.3
[info]   org.slf4j:slf4j-api            : 1.6.4 -> 1.6.6 -> 1.7.5
```
