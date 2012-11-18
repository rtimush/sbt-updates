seq(lsSettings :_*)

(LsKeys.tags in LsKeys.lsync) := Seq("sbt", "dependencies")

(description in LsKeys.lsync) := "Display your project's dependency updates."
