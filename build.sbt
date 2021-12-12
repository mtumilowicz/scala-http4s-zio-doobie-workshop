addCommandAlias("build", "prepare; test")
addCommandAlias("prepare", "fix; fmt")
addCommandAlias("check", "fixCheck; fmtCheck")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias(
  "fixCheck",
  "compile:scalafix --check; test:scalafix --check"
)
addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
addCommandAlias(
  "fmtCheck",
  "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck"
)

inThisBuild(
  List(
    organization := "mtum.corp",
    developers := List(
      Developer(
        "mtumilowicz",
        "Michal Tumilowicz",
        "michal.tumilowicz01@gmail.com",
        url("https://github.com/mtumilowicz")
      )
    ),
    licenses := Seq(
      "MIT" -> url(
        s"https://github.com/mtumilowicz/scala-http4s-zio-fs2-workshop/blob/master/LICENSE"
      )
    ),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalaVersion := "2.13.6",
    scalafixDependencies ++= Dependencies.ScalaFix
  )
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerSpotifyClientPlugin)
  .settings(
    dockerBaseImage := "openjdk:11-jre-slim-buster",
    dockerExposedPorts in Docker := Seq(8080),
    dockerUsername in Docker := Some("mtumilowicz"),
    libraryDependencies ++= Dependencies.App,
    name := "scala-http4s-zio-fs2-workshop",
    scalacOptions in ThisBuild := Options.scalacOptions(scalaVersion.value, isSnapshot.value),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
