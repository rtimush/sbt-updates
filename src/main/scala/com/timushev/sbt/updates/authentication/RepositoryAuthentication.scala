package com.timushev.sbt.updates.authentication

import com.timushev.sbt.updates.authentication.Coursier.CoursierConfiguration
import sbt._

import scala.language.reflectiveCalls
import scala.util.control.Exception.allCatch

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
    c match {
      case d: DirectCredentials =>
        Some(
          RepositoryAuthentication(
            repositoryId = None,
            realm = Some(d.realm),
            host = Some(d.host),
            user = d.userName,
            password = d.passwd,
            headers = Nil
          )
        )
      case _ => None
    }

  def fromCoursier(c: CoursierConfiguration): Seq[RepositoryAuthentication] =
    c.authenticationByRepositoryId.flatMap { case (repositoryId, auth) =>
      allCatch.opt {
        RepositoryAuthentication(
          repositoryId = Some(repositoryId),
          realm = auth.realmOpt,
          host = None,
          user = auth.user,
          password = auth.password,
          headers = auth.headers
        )
      }
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
