scalacOptions in ThisBuild ++= Seq("-deprecation", "-feature")

organization := "pl.newicom.scalexpr"

name := "scalexpr"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.13.3"

publishMavenStyle := true
homepage := Some(new URL("http://github.com/pawelkaczor/scalexpr"))
licenses := ("Apache2", new URL("http://raw.githubusercontent.com/pawelkaczor/scalexpr/master/LICENSE")) :: Nil
publishTo := Some(if (isSnapshot.value) Opts.resolver.sonatypeSnapshots else Opts.resolver.sonatypeStaging)

sonatypeProfileName := "pl.newicom"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scala-lang" % "scala-compiler"  % scalaVersion.value,
  "org.scala-lang" % "scala-reflect"   % scalaVersion.value,
  "com.lihaoyi"   %% "fastparse"       % "2.2.2",
  "org.scalatest" %% "scalatest"       % "3.2.0" % Test
)
