import sbt._
import sbt.Keys._

import com.typesafe.tools.mima.plugin.MimaPlugin.mimaDefaultSettings
import com.typesafe.tools.mima.plugin.MimaKeys.{
  binaryIssueFilters, previousArtifacts
}
import com.typesafe.tools.mima.core._, ProblemFilters._, Problem.ClassVersion

object Publish {
  @inline def env(n: String): String = sys.env.get(n).getOrElse(n)

  private val repoName = env("PUBLISH_REPO_NAME")
  private val repoUrl = env("PUBLISH_REPO_URL")

  val previousVersion = "0.11.0"

  val missingMethodInOld: ProblemFilter = {
    case mmp @ MissingMethodProblem(_) if (
      mmp.affectedVersion == ClassVersion.Old) => false

    case MissingMethodProblem(old) => !old.isAccessible
    case _ => true
  }

  val mimaSettings = mimaDefaultSettings ++ Seq(
    previousArtifacts := {
      if (scalaVersion.value startsWith "2.12.") Set.empty
      else if (crossPaths.value) {
        Set(organization.value % s"${moduleName.value}_${scalaBinaryVersion.value}" % previousVersion)
      } else {
        Set(organization.value % moduleName.value % previousVersion)
      }
    },
    binaryIssueFilters ++= Seq(missingMethodInOld))

  val siteUrl = "http://reactivemongo.org"

  lazy val settings = Seq(
    publishMavenStyle := true,
    publishArtifact in Test := false,
    publishTo := Some(repoUrl).map(repoName at _),
    credentials += Credentials(repoName, env("PUBLISH_REPO_ID"),
      env("PUBLISH_USER"), env("PUBLISH_PASS")),
    pomIncludeRepository := { _ => false },
    autoAPIMappings := true,
    apiURL := Some(url(s"$siteUrl/release/${Release.major.value}/api/")),
    licenses := {
      Seq("Apache 2.0" ->
        url("http://www.apache.org/licenses/LICENSE-2.0"))
    },
    homepage := Some(url(siteUrl)),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/ReactiveMongo/ReactiveMongo"),
        "scm:git://github.com/ReactiveMongo/ReactiveMongo.git")),
    developers := List(
      Developer(
        id = "sgodbillon",
        name = "Stephane Godbillon",
        email = "",
        url = url("http://stephane.godbillon.com")),
      Developer(
        id = "cchantep",
        name = "Cédric Chantepie",
        email = "",
        url = url("http://github.com/cchantep/"))))
}
