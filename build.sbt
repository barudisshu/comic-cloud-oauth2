import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm
import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings

val akkaVersion          = "2.5.24"
val akkaHttpVersion      = "10.1.3"
val log4j2Version        = "2.9.0"
val mysqlDriverVersion   = "8.0.12"
val slickVersion         = "3.3.2"
val scalatestFullVersion = "3.0.8"
val scalaMockVersion     = "3.6.0"
val kryoVersion          = "0.9.2"

lazy val `comic-cloud-oauth2` = project
  .in(file("."))
  .settings(multiJvmSettings: _*)
  .settings(
    organization := "com.comiccloud",
    scalaVersion := "2.12.8",
    scalacOptions in Compile ++=
      Seq(
        "-deprecation",
        "-feature",
        "-unchecked",
        "-Xlog-reflective-calls",
        "-Xlint",
        "-language:implicitConversions"
      ),
    javacOptions in Compile ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    javaOptions in run ++= Seq("-Xms128m",
                               "-Xmx1024m",
                               "-XX:MaxDirectMemorySize=512m",
                               "-Djava.library.path=./target/native"),
    libraryDependencies ++= Seq(
      // -- akka --
      "com.typesafe.akka"        %% "akka-actor"              % akkaVersion,
      "com.typesafe.akka"        %% "akka-remote"             % akkaVersion,
      "com.typesafe.akka"        %% "akka-cluster"            % akkaVersion,
      "com.typesafe.akka"        %% "akka-cluster-metrics"    % akkaVersion,
      "com.typesafe.akka"        %% "akka-cluster-tools"      % akkaVersion,
      "com.typesafe.akka"        %% "akka-cluster-sharding"   % akkaVersion,
      "com.typesafe.akka"        %% "akka-distributed-data"   % akkaVersion,
      "com.typesafe.akka"        %% "akka-http"               % akkaHttpVersion,
      "com.typesafe.akka"        %% "akka-http-spray-json"    % akkaHttpVersion,
      "com.typesafe.akka"        %% "akka-multi-node-testkit" % akkaVersion,
      "com.typesafe.akka"        %% "akka-slf4j"              % akkaVersion,
      "org.apache.logging.log4j" % "log4j-slf4j-impl"         % log4j2Version,
      "org.apache.logging.log4j" % "log4j-api"                % log4j2Version,
      "org.apache.logging.log4j" % "log4j-core"               % log4j2Version,
      // -- kryo --
      "com.twitter" %% "chill-akka" % kryoVersion,
      // -- jdbc --
      "mysql"              % "mysql-connector-java" % mysqlDriverVersion % Runtime,
      "com.zaxxer"         % "HikariCP"             % "3.4.0",
      "com.typesafe.slick" %% "slick"               % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp"      % slickVersion,
      // -- test --
      "com.typesafe.akka" %% "akka-http-testkit"           % akkaHttpVersion      % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"         % akkaVersion          % Test,
      "org.specs2"        %% "specs2-core"                 % "4.3.4"              % Test,
      "org.specs2"        %% "specs2-mock"                 % "4.3.4"              % Test,
      "org.scalatest"     %% "scalatest"                   % scalatestFullVersion % Test,
      "org.scalamock"     %% "scalamock-scalatest-support" % scalaMockVersion     % Test
    ),
    fork in run := true,
    mainClass in (Compile, run) := Some("com.commiccloud.Main"),
    // disable parallel tests
    parallelExecution in Test := false,
    licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0"))),
    version in Docker := "latest",
    dockerExposedPorts in Docker := Seq(1600),
    dockerRepository := Some("comic"),
    dockerBaseImage := "java",
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
  )
  .enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)
  .configs(MultiJvm)
