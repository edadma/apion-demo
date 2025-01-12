ThisBuild / licenses += "ISC"  -> url("https://opensource.org/licenses/ISC")
ThisBuild / versionScheme      := Some("semver-spec")
ThisBuild / evictionErrorLevel := Level.Warn
ThisBuild / scalaVersion           := "3.6.2"

publish / skip := true

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
  ),
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
  //  scalaJSLinkerConfig ~= { _.withModuleSplitStyle(ModuleSplitStyle.SmallestModules) },
  scalaJSLinkerConfig ~= { _.withSourceMap(false) },
)

lazy val client = project
  .enablePlugins(ScalaJSPlugin)
//  .dependsOn(shared)
  .settings(commonSettings)
  //  .enablePlugins(ScalablyTypedConverterPlugin)
  .settings(
    name        := "fluxus",
    description := "A minimalist UI framework inspired by component-based design, built with Scala.js",
    libraryDependencies ++= Seq(
      "io.github.cquiroz" %%% "scala-java-time" % "2.6.0",
      "io.github.edadma"  %%% "fluxus"          % "0.0.1",
    ),
    jsEnv                                  := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    scalaJSLinkerConfig ~= { _.withSourceMap(false) },
    publishMavenStyle        := true,
    Test / publishArtifact   := false,
  )

//lazy val server = project
//  .enablePlugins(ScalaJSPlugin)
//  .dependsOn(shared)
//  .settings(commonSettings)
//  .settings(
//    name                            := "examples",
//    scalaJSUseMainModuleInitializer := true,
//    publish / skip                  := true,
//    publishLocal / skip             := true,
//  )
//
//lazy val shared = project
//  .enablePlugins(ScalaJSPlugin)
//  .settings(commonSettings)
