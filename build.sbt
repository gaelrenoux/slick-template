lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "gaelrenoux",
      scalaVersion := "2.12.4",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "slick-template",
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-language:postfixOps"
    ),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.8.0",

      "org.xerial" % "sqlite-jdbc" % "3.21.0.1",

      "com.typesafe.slick" %% "slick" % "3.2.2",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.2.2"
    )
  )
