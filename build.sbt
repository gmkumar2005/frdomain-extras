name := "frdomain-extras"

// global settings for this build
ThisBuild / version := "0.0.1"
ThisBuild / organization := "frdomain"
ThisBuild / scalaVersion := Versions.scalaVersion
ThisBuild / evictionErrorLevel := Level.Warn


lazy val mtl = (project in file("./mtl"))
  //  .settings(Common.settings: _*)
  .settings(libraryDependencies ++= Dependencies.catsMtlDependencies)


  .settings(
    run / fork := true,
    Compile / mainClass := Some("frdomain.ch6.domain.io.app.Main"),
    addCommandAlias("mtl", "mtl/run")
  )


lazy val root = (project in file(".")).
  aggregate(mtl)

