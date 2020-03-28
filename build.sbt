name := "john-hopkins-service"

version := "0.1"

scalaVersion in ThisBuild := "2.13.1"


lazy val `johns-hopkins` = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(model, endpoints, service, database, endpoints)


lazy val service = (project in file("service"))
  .settings(compilerSettings)
  .enablePlugins(dockerPlugins: _*)
  .settings(versioningSettings)
  .settings(dockerSettings)
  .settings(libraryDependencies ++= Dependencies.Module.service)
  .dependsOn(database, endpoints)

lazy val database = (project in file("database"))
  .settings(compilerSettings)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++= Dependencies.Module.database)
  .dependsOn(model)

lazy val endpoints = (project in file("endpoints"))
  .settings(compilerSettings)
  .settings(noPublishSettings)
  .settings(libraryDependencies ++= Dependencies.Module.endpoints)
  .dependsOn(model)

lazy val model = (project in file("model"))
  .settings(noPublishSettings)
  .settings(compilerSettings)



lazy val compilerSettings = {
  scalacOptions in ThisProject ++= Seq(
    "-deprecation",
    "-feature",
    "-language:postfixOps",              //Allow postfix operator notation, such as `1 to 10 toList'
    "-language:implicitConversions",
    "-language:higherKinds",
    "-Ywarn-dead-code",                  // Warn when dead code is identified.
    "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
    "-Ywarn-macros:after",               // Warn unused macros after compilation
    "-Ywarn-numeric-widen",              // Warn when numerics are widened.
    "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals",              // Warn if a local definition is unused.
    "-Ywarn-unused:params",              // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates",            // Warn if a private member is unused.
    "-Ywarn-value-discard",               // Warn when non-Unit expression results are unused.
    "-Ywarn-unused:imports",
    "-Xfatal-warnings"
  )
}

lazy val dockerSettings = Seq(
  version in Docker := version.value,
  maintainer in Docker := "Vasilis Nicolaou",
  dockerBaseImage := "openjdk:8u191-alpine3.9",
  dockerExposedPorts := Seq(8080),
  maintainer := "vaslabsco@gmail.com",
  packageName in Docker := "covid19-stats",
  version in Docker := version.value,
  dockerUsername := Some("vaslabs"),
  dockerUpdateLatest := true,
  javaOptions in Universal ++= Seq(
    "-J-Xmx256m",
    "-J-Xms128m"
  )
)

lazy val noPublishSettings = Seq(
  publish / skip := true
)

lazy val dockerPlugins = Seq(DockerPlugin, AshScriptPlugin, JavaAppPackaging, UniversalPlugin)

lazy val versioningSettings = Seq(
  dynverSeparator in ThisBuild := "-",
  dynverVTagPrefix in ThisBuild := false
)