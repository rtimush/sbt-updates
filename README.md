sbt-updates-plugin
==================
Display your project's dependency updates.

Requirements
==============
SBT 0.11.2, 0.11.3 or 0.12

Installation
============
The plugin is in it's early stage, so only snapshot version is available. Add the following lines to your `project/plugins.sbt` or `~/.sbt/plugins/build.sbt` file:
```
resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-snapshots"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.0-SNAPSHOT")
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
