lazy val commonSettings = commonSmlBuildSettings ++ ossPublishSettings ++ Seq(
  organization := "com.softwaremill.testing",
  scalaVersion := "2.12.10"
)

lazy val rootProject = (project in file("."))
  .settings(commonSettings: _*)
  .settings(publishArtifact := false, name := "testing-doobie-programs")
  .aggregate(core)

lazy val core: Project = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      "org.tpolecat" %% "doobie-core" % "0.8.8",
      "org.tpolecat" %% "doobie-postgres" % "0.8.8",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.opentable.components" % "otj-pg-embedded" % "0.13.3" % Test,
      "org.scalatest" %% "scalatest" % "3.1.0" % Test,
    )
  )

