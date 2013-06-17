sbt-updates
==================
Display your SBT project's dependency updates.

Requirements
==============
SBT 0.12

Note: use version 0.1.0 for SBT 0.11.* series

Installation
============
### Stable version
Add the following linesto your `project/plugins.sbt` or `~/.sbt/plugins/build.sbt` file:
```
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.1")
```

### Snapshot version
Add the following lines to your `project/plugins.sbt` or `~/.sbt/plugins/build.sbt` file:
```
resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.2-SNAPSHOT")
```

Tasks
=====
* `dependency-updates`: show a list of project dependencies that can be updated.

Example:
```
> dependency-updates
[info] Found 3 dependency updates for test-project
[info]   ch.qos.logback:logback-classic : 0.8    -> 0.8.1 -> 0.9.30     -> 1.0.7
[info]   org.scala-lang:scala-library   : 2.9.1  -> 2.9.2 -> 2.10.0-RC1
[info]   org.slf4j:slf4j-api            : 1.6.4  -> 1.6.6 -> 1.7.2
```
