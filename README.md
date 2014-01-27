sbt-updates
==================
Display your SBT project's dependency updates.

Requirements
==============
SBT 0.12, 0.13.x

Note: use version 0.1.0 for SBT 0.11.* series

Installation
============
### Stable version
Add the following lines to your `project/sbt-updates.sbt` or `~/.sbt/plugins/sbt-updates.sbt` file:
```
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.3")
```

### Snapshot version
Add the following lines to your `project/sbt-updates.sbt` or `~/.sbt/plugins/sbt-updates.sbt` file:
```
resolvers += Resolver.url("sbt-plugin-snapshots", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.4-SNAPSHOT")
```

Tasks
=====
* `dependency-updates`: show a list of project dependencies that can be updated,
* `dependency-updates-report`: writes a list of project dependencies to a file.

Note: for SBT 0.13.x use camelCase task names `dependencyUpdates` and `dependencyUpdatesReport`.

Settings
========
* `dependency-updates-report-file`: report file location, `target/dependency-updates.txt` by default.

Example:
```
> dependency-updates
[info] Found 3 dependency updates for test-project
[info]   ch.qos.logback:logback-classic : 0.8   -> 0.8.1 -> 0.9.30 -> 1.0.13
[info]   org.scala-lang:scala-library   : 2.9.1 -> 2.9.3 -> 2.10.3
[info]   org.slf4j:slf4j-api            : 1.6.4 -> 1.6.6 -> 1.7.5
```
