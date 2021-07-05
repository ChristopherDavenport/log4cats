import sbtcrossproject.{crossProject, CrossType}
import sbtghactions.UseRef

val Scala213 = "2.13.6"
val Scala212 = "2.12.13"
val Scala3   = "3.0.0"

enablePlugins(SonatypeCiReleasePlugin)

ThisBuild / organization := "org.typelevel"
ThisBuild / baseVersion := "1.3"
ThisBuild / crossScalaVersions := Seq(Scala213, Scala212, Scala3)
ThisBuild / scalaVersion := Scala213
ThisBuild / publishFullName := "Christopher Davenport"
ThisBuild / publishGithubUser := "christopherdavenport"

ThisBuild / githubWorkflowJavaVersions := Seq("adopt@1.8", "adopt@1.11")

ThisBuild / versionIntroduced := Map(
  "2.12" -> "1.2.0",
  "2.13" -> "1.2.0",
  "3.0.0" -> "1.3.1",
)

val MicrositesCond = s"matrix.scala == '$Scala212'"

ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("test"), name = Some("Test")),
  WorkflowStep.Sbt(List("mimaReportBinaryIssues"), name = Some("Binary Compatibility Check"))
)

def micrositeWorkflowSteps(cond: Option[String] = None): List[WorkflowStep] = List(
  WorkflowStep.Use(
    UseRef.Public("ruby", "setup-ruby", "v1"),
    params = Map("ruby-version" -> "2.6"),
    cond = cond
  ),
  WorkflowStep.Run(List("gem update --system"), cond = cond),
  WorkflowStep.Run(List("gem install sass"), cond = cond),
  WorkflowStep.Run(List("gem install jekyll -v 4"), cond = cond)
)

ThisBuild / githubWorkflowAddedJobs ++= Seq(
  WorkflowJob(
    "scalafmt",
    "Scalafmt",
    githubWorkflowJobSetup.value.toList ::: List(
      WorkflowStep.Sbt(List("scalafmtCheckAll"), name = Some("Scalafmt"))
    ),
    // Awaiting release of https://github.com/scalameta/scalafmt/pull/2324/files
    scalas = crossScalaVersions.value.toList.filter(_.startsWith("2."))
  ),
  WorkflowJob(
    "microsite",
    "Microsite",
    githubWorkflowJobSetup.value.toList ::: (micrositeWorkflowSteps(None) :+ WorkflowStep
      .Sbt(List("docs/makeMicrosite"), name = Some("Build the microsite"))),
    scalas = List(Scala212)
  )
)

ThisBuild / githubWorkflowTargetBranches := List("*", "series/*")
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("release"),
  )
) ++ micrositeWorkflowSteps(Some(MicrositesCond)).toSeq :+ WorkflowStep.Sbt(
  List("docs/publishMicrosite"),
  cond = Some(MicrositesCond)
)

val catsV = "2.6.1"
val catsEffectV = "2.5.1"
val slf4jV = "1.7.30"
val munitCatsEffectV = "1.0.3"
val logbackClassicV = "1.2.3"
val enclosureV      = "0.1.0"

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val log4cats = project
  .in(file("."))
  .aggregate(
    coreJVM,
    coreJS,
    testingJVM,
    testingJS,
    noopJVM,
    noopJS,
    slf4j,
    docs
  )
  .enablePlugins(NoPublishPlugin)
  .settings(commonSettings, releaseSettings)

lazy val docs = project
  .settings(commonSettings, micrositeSettings)
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(MdocPlugin)
  .enablePlugins(NoPublishPlugin)
  .settings(mdocIn := sourceDirectory.value / "main" / "mdoc")
  .dependsOn(slf4j)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .settings(commonSettings, releaseSettings)
  .settings(
    name := "log4cats-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % catsV,
      "com.lorandszakacs" %%% "enclosure" % enclosureV,
    )
  )
lazy val coreJVM = core.jvm
lazy val coreJS = core
  .settings(dottyJsSettings(ThisBuild / crossScalaVersions))
  .jsSettings(
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
  )
  .js

lazy val testing = crossProject(JSPlatform, JVMPlatform)
  .settings(commonSettings, releaseSettings)
  .dependsOn(core)
  .settings(
    name := "log4cats-testing",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % catsEffectV
    )
  )
lazy val testingJVM = testing.jvm
lazy val testingJS = testing.js

lazy val noop = crossProject(JSPlatform, JVMPlatform)
  .settings(commonSettings, releaseSettings)
  .dependsOn(core)
  .settings(
    name := "log4cats-noop"
  )
lazy val noopJVM = noop.jvm
lazy val noopJS = noop.js

lazy val slf4j = project
  .settings(commonSettings, releaseSettings)
  .dependsOn(coreJVM)
  .settings(
    name := "log4cats-slf4j",
    libraryDependencies ++= Seq(
      "org.slf4j"                       % "slf4j-api"       % slf4jV,
      "org.typelevel" %%% "cats-effect" % catsEffectV,
      "ch.qos.logback"                  % "logback-classic" % logbackClassicV % Test
    ),
    libraryDependencies ++= {
      if (isDotty.value) Seq.empty
      else Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)
    }
  )

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "munit-cats-effect-2" % munitCatsEffectV % Test,
  ),
  testFrameworks += new TestFramework("munit.Framework"),
  // major scala version folders do not work properly for shared sources between JS/JVM
  // see: https://github.com/sbt/sbt/issues/5895#issuecomment-739510744
  Compile / unmanagedSourceDirectories ++= {
    val major = if (isDotty.value) "-3" else "-2"
    List(CrossType.Pure, CrossType.Full).flatMap(
      _.sharedSrcDir(baseDirectory.value, "main").toList.map(f => file(f.getPath + major))
    )
  },
  Test / unmanagedSourceDirectories ++= {
    val major = if (isDotty.value) "-3" else "-2"
    List(CrossType.Pure, CrossType.Full).flatMap(
      _.sharedSrcDir(baseDirectory.value, "test").toList.map(f => file(f.getPath + major))
    )
  }
)

lazy val releaseSettings = {
  Seq(
    Test / publishArtifact := false,
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/typelevel/log4cats"),
        "git@github.com:typelevel/log4cats.git"
      )
    ),
    homepage := Some(url("https://github.com/typelevel/log4cats")),
    licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
    startYear := Some(2018),
    developers := List(
      Developer("christopherdavenport", "Christopher Davenport", "chris@christopherdavenport.tech", new java.net.URL("https://christopherdavenport.github.io/")),
      Developer("lorandszakacs", "Loránd Szakács", "lorandszakacs@users.noreply.github.com", new java.net.URL("https://github.com/lorandszakacs")),
    ),
  )
}

lazy val micrositeSettings = Seq(
  micrositeName := "log4cats",
  micrositeDescription := "Functional Logging",
  micrositeAuthor := "Christopher Davenport",
  micrositeGithubOwner := "typelevel",
  micrositeGithubRepo := "log4cats",
  micrositeBaseUrl := "/log4cats",
  micrositeDocumentationUrl := "https://typelevel.github.io/log4cats",
  micrositeFooterText := None,
  micrositeHighlightTheme := "atom-one-light",
  micrositePalette := Map(
    "brand-primary" -> "#3e5b95",
    "brand-secondary" -> "#294066",
    "brand-tertiary" -> "#2d5799",
    "gray-dark" -> "#49494B",
    "gray" -> "#7B7B7E",
    "gray-light" -> "#E5E5E6",
    "gray-lighter" -> "#F4F3F4",
    "white-color" -> "#FFFFFF"
  ),
  scalacOptions --= Seq(
    "-Xfatal-warnings",
    "-Ywarn-unused-import",
    "-Ywarn-numeric-widen",
    "-Ywarn-dead-code",
    "-Ywarn-unused:imports",
    "-Xlint:-missing-interpolator,_"
  ),
  micrositePushSiteWith := GitHub4s,
  micrositeGithubToken := sys.env.get("GITHUB_TOKEN")
)
