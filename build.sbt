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
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scala-lang" % "scala-compiler"  % scalaVersion.value,
  "org.scala-lang" % "scala-reflect"   % scalaVersion.value,
  "com.lihaoyi"   %% "fastparse"       % "2.2.2",
  "org.scalatest" %% "scalatest"       % "3.2.0" % Test
)

Publish.settings
