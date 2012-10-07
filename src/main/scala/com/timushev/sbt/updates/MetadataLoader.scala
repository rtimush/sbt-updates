package com.timushev.sbt.updates

import semverfi.{Version, SemVersion}
import sbt.{Resolver, MavenRepository, ModuleID}
import dispatch._

object MetadataLoader {
  val factory: PartialFunction[Resolver, MetadataLoader] = {
    case repo: MavenRepository => new MavenMetadataLoader(repo)
  }
}

trait MetadataLoader {
  def getVersions(module: ModuleID): Promise[Seq[SemVersion]]
}

class MavenMetadataLoader(repo: MavenRepository) extends MetadataLoader {

  def getVersions(module: ModuleID): Promise[Seq[SemVersion]] =
    Http(metadataUrl(module) OK as.xml.Elem) map extractVersions

  def metadataUrl(module: ModuleID) =
    artifactUrl(module) / "maven-metadata.xml"

  def artifactUrl(module: ModuleID) =
    (module.organization.split('.') :+ module.name foldLeft url(repo.root))(_ / _)

  def extractVersions(metadata: xml.Elem): Seq[SemVersion] =
    metadata \ "versioning" \ "versions" \ "version" map (_.text) map Version.apply

}
