ThisBuild / versionScheme := Some("early-semver")
ThisBuild / scalacOptions ++= Seq("-deprecation", "-feature")

organization := "pl.newicom.scalexpr"

name := "scalexpr"

scalaVersion := "2.13.6"

publishMavenStyle := true
homepage := Some(new URL("http://github.com/pawelkaczor/scalexpr"))
licenses := ("Apache2", new URL("http://raw.githubusercontent.com/pawelkaczor/scalexpr/master/LICENSE")) :: Nil
publishTo := sonatypePublishToBundle.value

sonatypeProfileName := "pl.newicom"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler"  % scalaVersion.value,
  "org.scala-lang" % "scala-reflect"   % scalaVersion.value,
  "com.lihaoyi"   %% "fastparse"       % "2.2.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
  "org.scalatest" %% "scalatest"       % "3.2.0" % Test
)

Publish.settings
