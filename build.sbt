val settings = Seq(
  organization := "io.scalaland",
  version := "0.1.3",
  scalaVersion := "2.12.2",
  scalacOptions ++= Seq(
    "-target:jvm-1.8",
    "-encoding",
    "UTF-8",
    "-unchecked",
    "-deprecation",
    "-explaintypes",
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-inaccessible",
    "-Ywarn-infer-any",
    "-Ywarn-nullary-override",
    "-Ywarn-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint:adapted-args",
    "-Xlint:by-name-right-associative",
    "-Xlint:constant",
    "-Xlint:delayedinit-select",
    "-Xlint:doc-detached",
    "-Xlint:inaccessible",
    "-Xlint:infer-any",
    "-Xlint:missing-interpolator",
    "-Xlint:nullary-override",
    "-Xlint:nullary-unit",
    "-Xlint:option-implicit",
    "-Xlint:package-object-classes",
    "-Xlint:poly-implicit-overload",
    "-Xlint:private-shadow",
    "-Xlint:stars-align",
    "-Xlint:type-parameter-shadow",
    "-Xlint:unsound-match"
  ),
  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
)

val versions = new {
  val shapelessVersion = "2.3.2"
  val scalatestVersion = "3.0.3"
  val scalafmt = "1.0.0-RC1"
}

val dependencies = Seq(
  libraryDependencies += "com.chuusai" %%% "shapeless" % versions.shapelessVersion,
  libraryDependencies += "org.scalatest" %%% "scalatest" % versions.scalatestVersion % "test"
)

lazy val root = project
  .in(file("."))
  .settings(settings: _*)
  .settings(publishSettings: _*)
  .settings(noPublishSettings: _*)
  .settings(commands += Command.args("scalafmt", "Run scalafmt cli.") {
    case (state, args) =>
      val Right(scalafmt) =
        org.scalafmt.bootstrap.ScalafmtBootstrap.fromVersion(versions.scalafmt)
      scalafmt.main("--non-interactive" +: args.toArray)
      state
  })
  .aggregate(chimneyJVM, chimneyJS, protosJVM, protosJS)
  .dependsOn(chimneyJVM, chimneyJS)

lazy val chimney = crossProject
  .crossType(CrossType.Pure)
  .settings(
    moduleName := "chimney",
    name := "chimney",
    description := "Scala library for boilerplate free data rewriting"
  )
  .settings(settings: _*)
  .settings(publishSettings: _*)
  .settings(dependencies: _*)
  .dependsOn(protos % "test->compile")

lazy val chimneyJVM = chimney.jvm
lazy val chimneyJS = chimney.js

lazy val protos = crossProject
  .crossType(CrossType.Pure)
  .settings(
    name := "chimney-protos",
    libraryDependencies += "com.trueaccord.scalapb" %%% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion,
    PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value),
    PB.protoSources in Compile := Seq(file("protos/src/main/protobuf")),
    coverageExcludedPackages := "<empty>;(.*)"
  )
  .settings(settings: _*)
  .settings(noPublishSettings: _*)

lazy val protosJVM = protos.jvm
lazy val protosJS = protos.js

lazy val docs = project
  .enablePlugins(MicrositesPlugin)
  .settings(moduleName := "chimney-docs")
  .settings(settings)
  .settings(noPublishSettings)
  .settings(docSettings)
  .dependsOn(chimneyJVM)

lazy val publishSettings = Seq(
  homepage := Some(url("https://scalaland.io")),
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  scmInfo := Some(
    ScmInfo(url("https://github.com/scalalandio/chimney"), "scm:git:git@github.com:scalalandio/chimney.git")
  ),
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  pomExtra := (
    <developers>
      <developer>
        <id>krzemin</id>
        <name>Piotr Krzemiński</name>
        <url>http://github.com/krzemin</url>
      </developer>
      <developer>
        <id>MateuszKubuszok</id>
        <name>Mateusz Kubuszok</name>
        <url>http://github.com/MateuszKubuszok</url>
      </developer>
    </developers>
  )
)

lazy val noPublishSettings =
  Seq(publish := (), publishLocal := (), publishArtifact := false)

lazy val docSettings = Seq(
  micrositeName := "Chimney",
  micrositeDescription := "Scala library for boilerplate-free data transformations",
  micrositeAuthor := "Piotr Krzemiński, Mateusz Kubuszok",
  micrositeHighlightTheme := "atom-one-light",
  micrositeHomepage := "http://github.com/scalaland/chimney",
  micrositeBaseUrl := "/chimney",
//  micrositeDocumentationUrl := "api",
  micrositeGithubOwner := "scalalandio",
//  micrositeExtraMdFiles := Map(file("CONTRIBUTING.md") -> "contributing.md"),
  micrositeGithubRepo := "chimney",
  micrositePalette := Map(
    "brand-primary" -> "#88695B",
    "brand-secondary" -> "#533E29",
    "brand-tertiary" -> "#493722",
    "gray-dark" -> "#49494B",
    "gray" -> "#7B7B7E",
    "gray-light" -> "#E5E5E6",
    "gray-lighter" -> "#F4F3F4",
    "white-color" -> "#FFFFFF"
//"brand-primary" -> "#5B5988",
//"brand-secondary" -> "#292E53",
//"brand-tertiary" -> "#222749",
//"gray-dark" -> "#49494B",
//"gray" -> "#7B7B7E",
//"gray-light" -> "#E5E5E6",
//"gray-lighter" -> "#F4F3F4",
//"white-color" -> "#FFFFFF"
  ),
//  autoAPIMappings := true,
//  unidocProjectFilter in (ScalaUnidoc, unidoc) :=
//    inProjects(docsSourcesAndProjects(scalaVersion.value)._2:_*),
//  docsMappingsAPIDir := "api",
//  addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), docsMappingsAPIDir),
  ghpagesNoJekyll := false,
  fork in tut := true,
//  fork in (ScalaUnidoc, unidoc) := true,
//  scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
//    "-Xfatal-warnings",
//    "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
//    "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath,
//    "-diagrams"
//  ),
  git.remoteRepo := "git@github.com:scalaland/chimney.git",
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md" | "*.svg",
  includeFilter in Jekyll := (includeFilter in makeSite).value
)
