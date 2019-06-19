package com.timushev.sbt.updates.metadata

import com.timushev.sbt.updates.versions.Version

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompoundMetadataLoader(loaders: Seq[MetadataLoader]) extends MetadataLoader {
  override def getVersions(module: sbt.ModuleID): Future[Seq[Version]] =
    loaders.foldLeft(Future.failed[Seq[Version]](new NoSuchElementException)) { (result, loader) =>
      result.recoverWith {
        case _ => loader.getVersions(module)
      }
    }
}
