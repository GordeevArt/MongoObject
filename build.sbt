import PlayKeys._

name := "mongoObject"

version := "1.0"

scalaVersion  := "2.11.4"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23" % "provided"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)