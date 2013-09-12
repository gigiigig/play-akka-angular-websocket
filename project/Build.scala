import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "PlayAkkaAngularWebSocket"
  val appVersion = "1.0"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    "org.specs2" %% "specs2" % "1.14" % "test",
    "commons-codec" % "commons-codec" % "1.7",
    "com.typesafe.akka" %% "akka-testkit" % "2.1.0"
  ) 

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
  ).settings()

}
