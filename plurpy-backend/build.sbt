ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

libraryDependencies += "dev.zio" %% "zio" % "2.0.3"
libraryDependencies += "dev.zio" %% "zio-streams" % "2.0.3"

lazy val root = (project in file("."))
  .settings(
    name := "plurpy-backend",
    idePackagePrefix := Some("org.tomasmo.plurpy")
  )
