scalacOptions in ThisBuild ++= Seq("-deprecation", "-feature")

organization := "pl.newicom.scalexpr"

name := "scalexpr"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scala-lang" % "scala-compiler"  % scalaVersion.value,
  "org.scala-lang" % "scala-reflect"   % scalaVersion.value,
  "com.lihaoyi"   %% "fastparse"       % "2.2.2",
  "org.scalatest" %% "scalatest"       % "3.2.0" % Test
)
