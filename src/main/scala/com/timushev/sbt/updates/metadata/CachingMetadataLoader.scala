package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.versions.Version
import sbt.ModuleID

import scala.collection.mutable
import scala.concurrent.Future

class CachingMetadataLoader(
    loader: MetadataLoader,
    cache: mutable.Map[(MetadataLoader, ModuleID), Future[Seq[Version]]]
) extends MetadataLoader {

  override def getVersions(module: ModuleID): Future[Seq[Version]] = {
    cache.synchronized {
      cache.getOrElseUpdate((loader, module), loader.getVersions(module))
    }
  }

}
