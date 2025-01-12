ThisBuild / licenses += "ISC"  -> url("https://opensource.org/licenses/ISC")
ThisBuild / versionScheme      := Some("semver-spec")
ThisBuild / evictionErrorLevel := Level.Warn

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
  .dependsOn(shared)
  .settings(commonSettings)
  //  .enablePlugins(ScalablyTypedConverterPlugin)
  .settings(
    name        := "fluxus",
    description := "A minimalist UI framework inspired by component-based design, built with Scala.js",
    libraryDependencies ++= Seq(
      "org.scalatest"    %%% "scalatest"                   % "3.2.19" % "test",
      "com.lihaoyi"      %%% "pprint"                      % "0.9.0"  % "test",
      "org.scala-js"     %%% "scalajs-dom"                 % "2.8.0",
      "io.github.edadma" %%% "logger"                      % "0.0.6",
      "dev.zio"          %%% "zio-json"                    % "0.7.3",
      "com.raquo"        %%% "airstream"                   % "16.0.0",
      "org.scala-js"     %%% "scala-js-macrotask-executor" % "1.1.1",
    ),
    jsEnv                                  := new org.scalajs.jsenv.nodejs.NodeJSEnv(),
    scalaJSUseMainModuleInitializer        := true,
    Test / scalaJSUseMainModuleInitializer := true,
    Test / scalaJSUseTestModuleInitializer := false,
    //    Test / scalaJSUseMainModuleInitializer := false,
    //    Test / scalaJSUseTestModuleInitializer := true,
    Test / parallelExecution := false,
    publishMavenStyle        := true,
    Test / publishArtifact   := false,
  )

lazy val server = project
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(shared)
  .settings(commonSettings)
  .settings(
    name                            := "examples",
    scalaJSUseMainModuleInitializer := true,
    publish / skip                  := true,
    publishLocal / skip             := true,
  )

lazy val shared = project
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings)
