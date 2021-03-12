resolvers += Resolver.bintrayIvyRepo("rallyhealth", "sbt-plugins")

addSbtPlugin("org.scalameta"       % "sbt-scalafmt"       % "2.4.2")
addSbtPlugin("com.typesafe.sbt"    % "sbt-git"            % "1.0.0")
addSbtPlugin("com.rallyhealth.sbt" % "sbt-git-versioning" % "1.4.0")
addSbtPlugin("com.eed3si9n"        % "sbt-projectmatrix"  % "0.7.0")
addSbtPlugin("org.xerial.sbt"      % "sbt-sonatype"       % "3.9.7")
addSbtPlugin("com.github.sbt"      % "sbt-pgp"            % "2.1.2")
