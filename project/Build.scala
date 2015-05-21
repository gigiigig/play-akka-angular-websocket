import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._
import com.typesafe.sbt.web.SbtWeb.autoImport._
import com.typesafe.sbt.less.Import.LessKeys

object ApplicationBuild extends Build {

  val appName = "PlayAkkaAngularWebSocket"
  val appVersion = "1.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    "org.specs2" %% "specs2" % "2.4.17" % "test",
    "commons-codec" % "commons-codec" % "1.7",
    "com.typesafe.akka" %% "akka-testkit" % "2.3.10"
  ) 

  val main = Project(appName, file("."))
    .enablePlugins(play.PlayScala)
    .settings(
      scalaVersion := "2.11.5",
      scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:reflectiveCalls"),
      version := appVersion,
      includeFilter in (Assets, LessKeys.less) := "*.less",
      resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases",
      libraryDependencies ++= appDependencies
    )

}
