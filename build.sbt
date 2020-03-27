name := "john-hopkins-service"

version := "0.1"

scalaVersion in ThisBuild := "2.13.1"


lazy val `johns-hopkins` = (project in file("."))
  .aggregate(model, endpoints)


lazy val database = (project in file("database"))
  .settings(libraryDependencies ++= Dependencies.Module.database)
  .dependsOn(model)

lazy val endpoints = (project in file("endpoints"))
  .settings(libraryDependencies ++= Dependencies.Module.endpoints)
  .dependsOn(model)

lazy val model = (project in file("model"))
