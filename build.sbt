ThisBuild / licenses += "ISC"  -> url("https://opensource.org/licenses/ISC")
ThisBuild / versionScheme      := Some("semver-spec")
ThisBuild / evictionErrorLevel := Level.Warn

publish / skip := true

lazy val apion_demo = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
//  .enablePlugins(ScalablyTypedConverterPlugin)
  .settings(
    name         := "apion-demo",
    version      := "0.0.1",
    scalaVersion := "3.6.2",
    organization := "io.github.edadma",
    libraryDependencies ++= Seq(
      "io.github.edadma" %%% "apion" % "0.0.2",
    ),
//    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % "2.6.0",
//    libraryDependencies += "com.lihaoyi" %%% "pprint" % "0.9.0" % "test",
    jsEnv                                  := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
    Test / scalaJSUseMainModuleInitializer := true,
    Test / scalaJSUseTestModuleInitializer := false,
//    Test / scalaJSUseMainModuleInitializer := false,
//    Test / scalaJSUseTestModuleInitializer := true,
    scalaJSUseMainModuleInitializer := true,
    publishMavenStyle               := true,
    Test / publishArtifact          := false,
    licenses += "ISC"               -> url("https://opensource.org/licenses/ISC"),
  )
