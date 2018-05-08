import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "difflool",
    libraryDependencies += scalaTest % Test
  )

libraryDependencies += "org.scala-lang.modules" %% "scala-swing" % "2.0.3"
