package com.timushev.sbt.updates.authentication

import com.timushev.sbt.updates.Compat
import sbt._

import scala.util.control.Exception.nonFatalCatch

case class RepositoryAuthentication(
    repositoryId: Option[String],
    realm: Option[String],
    host: Option[String],
    user: String,
    password: String,
    headers: Seq[(String, String)]
) {
  def describe: String =
    (if (user.nonEmpty || password.nonEmpty) s"$user:***** " else "") +
      headers.map { case (k, _) => s"$k:*****" }.mkString("(", ", ", ")")
}

object RepositoryAuthentication {

  def fromCredentials(c: Credentials): Option[RepositoryAuthentication] =
    nonFatalCatch.opt(Compat.toDirectCredentials(c)).map { d =>
      RepositoryAuthentication(
        repositoryId = None,
        realm = Some(d.realm),
        host = Some(d.host),
        user = d.userName,
        password = d.passwd,
        headers = Nil
      )
    }

  /** Reads `authenticationByRepositoryId` of an lm-coursier `CoursierConfiguration`; fails if its shape changed. */
  def fromCoursier(configuration: AnyRef): Seq[RepositoryAuthentication] =
    Coursier.get[Seq[(String, AnyRef)]](configuration, "authenticationByRepositoryId").map {
      case (repositoryId, auth) =>
        RepositoryAuthentication(
          repositoryId = Some(repositoryId),
          realm = Coursier.get[Option[String]](auth, "realmOpt"),
          host = None,
          user = Coursier.get[String](auth, "user"),
          password = Coursier.get[String](auth, "password"),
          headers = Coursier.get[Seq[(String, String)]](auth, "headers")
        )
    }

  def find(
      host: String,
      repositoryId: String,
      authentications: Seq[RepositoryAuthentication]
  ): Option[RepositoryAuthentication] =
    authentications
      .find(a => a.repositoryId == Some(repositoryId))
      .orElse(authentications.find(a => a.host == Some(host)))

}
