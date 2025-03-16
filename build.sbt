name := """peer-assessment"""
organization := "ca.concordia.ginacody"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  guice,
  // PostgreSQL JDBC Driver (for raw SQL)
  "org.postgresql" % "postgresql" % "42.7.1",

  // Play JPA for ORM support
  "org.playframework" %% "play-java-jpa" % "3.0.7",

  // Hibernate ORM (JPA Implementation)
  "org.hibernate" % "hibernate-core" % "6.6.3.Final",

  // Jakarta Persistence API (for JPA annotations)
  "jakarta.persistence" % "jakarta.persistence-api" % "3.2.0",

  jdbc,

  "org.slf4j" % "slf4j-api" % "2.0.9",

  "ch.qos.logback" % "logback-classic" % "1.4.11",

  "org.mindrot" % "jbcrypt" % "0.4",


)