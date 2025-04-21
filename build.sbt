name := """peer-assessment"""
organization := "ca.concordia.ginacody"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, JacocoPlugin)

scalaVersion := "2.13.16"

// ignore classes matching these patterns
jacocoExcludes := Seq(
  "views.*",
  "forms.*",
  "models.*",
  "exceptions.*",
  "modules.*",
  "factory.*",
  "authorization.*",
  "controllers.javascript.*",
  "services.dashboard.*",
  "*Routes*",          // generated classes under test
  "*ReverseRoutes*",
  "*Reverse*Controller*",// generated classes under test
  "*javascript*"       // generated classes under test
)

// JaCoCo settings
jacocoReportSettings := JacocoReportSettings()
  .withTitle("Code Coverage Report")
  .withFormats(JacocoReportFormats.HTML, JacocoReportFormats.XML)

// Define Dependencies
libraryDependencies ++= Seq(
  guice,

  // Database Dependencies
  "org.postgresql" % "postgresql" % "42.7.1", // PostgreSQL JDBC Driver
  "org.playframework" %% "play-java-jpa" % "3.0.7", // Play JPA
  "org.hibernate" % "hibernate-core" % "6.6.3.Final", // Hibernate ORM
  "jakarta.persistence" % "jakarta.persistence-api" % "3.2.0", // JPA Annotations
  jdbc,

  // Logging Dependencies
  "org.slf4j" % "slf4j-api" % "2.0.9",
  "ch.qos.logback" % "logback-classic" % "1.4.11",

  // Security
  "org.mindrot" % "jbcrypt" % "0.4",

  // CSV Parsing
  "org.apache.commons" % "commons-csv" % "1.13.0",

  // Reporting
  "org.apache.poi" % "poi-ooxml" % "5.2.3",

  // Testing Dependencies
  "org.mockito" % "mockito-core" % "5.3.1" % Test,
  "org.mockito" % "mockito-junit-jupiter" % "5.3.1" % Test,
  "junit" % "junit" % "4.13.2" % Test
)

// SBT options to avoid warnings
Compile / javacOptions ++= Seq("-source", "17", "-target", "17") //  Java 17 compatibility