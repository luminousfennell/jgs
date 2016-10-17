lazy val setScalaVersion = scalaVersion := "2.11.7"

lazy val commonSettings = Seq(

    setScalaVersion,
    organization := "de.unifreiburg.cs.proglang",
    unmanagedBase := file("lib"),
    
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % "3.4",
      "org.apache.commons" % "commons-collections4" % "4.1"
    )
)

lazy val jgsCheckDeps = Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  // "org.json4s" %% "json4s-native" % "3.3.0",
  "org.json4s" %% "json4s-jackson" % "3.3.0",
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % "2.7.4",
  "com.github.scopt" %% "scopt" % "3.4.0",
  "com.googlecode.kiama" %% "kiama" % "1.8.0"
)

lazy val GradualConstraints = 
  (project in file(".")).
    dependsOn(InstrumentationSupport).
    settings(commonSettings:_*).
    settings(
      libraryDependencies ++= jgsCheckDeps
    )

lazy val Tests =
  (project in file("Tests")).
    dependsOn(GradualConstraints).
    settings(commonSettings:_*).
    settings(
      libraryDependencies ++=
        jgsCheckDeps ++
        Seq(
          "com.novocode" % "junit-interface" % "0.11" % "test",
          "org.scalactic" %% "scalactic" % "2.2.6",
          "org.scalatest" %% "scalatest" % "2.2.6" % "test"
        )
    )

lazy val InstrumentationSupport = 
  (project in file("InstrumentationSupport")).
    settings(commonSettings:_*).
    settings(
      libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
      artifactName := {
        (sv : ScalaVersion , mod : ModuleID , artifact : Artifact) => "gradualconstraints_" + Artifact.artifactName(sv, mod, artifact)
      }

    )


lazy val JGSSupport = project.settings(setScalaVersion)
