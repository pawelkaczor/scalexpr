import sbt._
import Keys._

object Publish {
  lazy val settings = Seq(
    scmInfo := Some(ScmInfo(url("https://github.com/pawelkaczor/scalexpr"), "scm:git:git@github.com:pawelkaczor/scalexpr.git</")),
    pomExtra :=
      <developers>
        <developer>
          <id>pawelkaczor</id>
          <name>Pawel Kaczor</name>
          <url>https://github.com/pawelkaczor</url>
        </developer>
      </developers>
  )
}
