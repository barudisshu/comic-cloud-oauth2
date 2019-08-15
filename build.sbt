name := "comic-cloud-oauth2"
maintainer := "galudisu@gmail.com"

organization := "io.comiccloud"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion          = "2.5.23"
val akkaHttpVersion      = "10.1.3"
val log4j2Version        = "2.9.0"
val mysqlDriverVersion   = "8.0.12"
val slickVersion         = "3.2.3"
val scalatestFullVersion = "3.0.3"
val scalaMockVersion     = "3.6.0"
val persistInmemVersion  = "2.5.1.1"
val phantomVersion       = "2.39.0"
val phantomUtilVersion   = "0.50.0"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= Seq(
  // -- akka --
  "com.typesafe.akka"        %% "akka-actor"            % akkaVersion,
  "com.typesafe.akka"        %% "akka-stream"           % akkaVersion,
  "com.typesafe.akka"        %% "akka-remote"           % akkaVersion,
  "com.typesafe.akka"        %% "akka-cluster"          % akkaVersion,
  "com.typesafe.akka"        %% "akka-cluster-tools"    % akkaVersion,
  "com.typesafe.akka"        %% "akka-cluster-sharding" % akkaVersion,
  "com.typesafe.akka"        %% "akka-http"             % akkaHttpVersion,
  "com.typesafe.akka"        %% "akka-http-spray-json"  % akkaHttpVersion,
  "com.typesafe.akka"        %% "akka-slf4j"            % akkaVersion,
  "org.apache.logging.log4j" % "log4j-slf4j-impl"       % log4j2Version,
  "org.apache.logging.log4j" % "log4j-api"              % log4j2Version,
  "org.apache.logging.log4j" % "log4j-core"             % log4j2Version,
  // -- phantom --
  "com.outworkers"     %% "phantom-dsl"     % phantomVersion,
  "com.outworkers"     %% "phantom-streams" % phantomVersion,
  "com.outworkers"     %% "util-testing"    % phantomUtilVersion % Test,
  "com.twitter"        %% "chill-akka"      % "0.9.2",
  "org.apache.commons" % "commons-lang3"    % "3.0",
  "org.scala-lang"     % "scala-reflect"    % scalaVersion.value,
  // -- test --
  "com.typesafe.akka" %% "akka-http-testkit"           % akkaHttpVersion      % Test,
  "com.typesafe.akka" %% "akka-stream-testkit"         % akkaVersion          % Test,
  "org.specs2"        %% "specs2-core"                 % "4.3.4"              % Test,
  "org.specs2"        %% "specs2-mock"                 % "4.3.4"              % Test,
  "org.scalatest"     %% "scalatest"                   % scalatestFullVersion % Test,
  "org.scalamock"     %% "scalamock-scalatest-support" % scalaMockVersion     % Test
)

enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)

coverageEnabled := true

mainClass in (Compile, packageBin) := Some("io.comiccloud.Main")

version in Docker := "latest"
dockerExposedPorts in Docker := Seq(1600)
dockerRepository := Some("comic")
dockerBaseImage := "java"

dockerfile in docker := {
  val jarFile: File = sbt.Keys.`package`.in(Compile, packageBin).value
  val classpath     = (managedClasspath in Compile).value
  val mainclass     = mainClass.in(Compile, packageBin).value.getOrElse(sys.error("Expected exactly one main class"))
  val jarTarget     = s"/app/${jarFile.getName}"
  // Make a colon separated classpath with the JAR file
  val classpathString = classpath.files
    .map("/app/" + _.getName)
    .mkString(":") + ":" + jarTarget
  new Dockerfile {
    // Base image
    from("openjdk:8-jre")
    // Add all files on the classpath
    add(classpath.files, "/app/")
    // Add the JAR file
    add(jarFile, jarTarget)
    // On launch run Java with the classpath and the main class
    entryPoint("java", "-cp", classpathString, mainclass)
  }
}
