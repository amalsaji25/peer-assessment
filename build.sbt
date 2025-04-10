name := """peer-assessment"""
organization := "ca.concordia.ginacody"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, JacocoPlugin)

scalaVersion := "2.13.16"

// JaCoCo settings
jacocoReportSettings := JacocoReportSettings(
  title = "JaCoCo Coverage Report",
  formats = Seq(JacocoReportFormats.HTML, JacocoReportFormats.XML)
)

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
  "org.apache.poi" % "poi-ooxml" % "5.2.3" exclude("org.apache.logging.log4j", "log4j-api"),

  // Testing Dependencies
  "org.mockito" % "mockito-core" % "5.3.1" % Test,
  "org.mockito" % "mockito-junit-jupiter" % "5.3.1" % Test,
  "junit" % "junit" % "4.13.2" % Test
)

// SBT options to avoid warnings
Compile / javacOptions ++= Seq("-source", "17", "-target", "17") //  Java 17 compatibility