
name := "comic-cloud-oauth2"

organization := "io.comiccloud"

version := "0.1"

scalaVersion := "2.12.5"

val akkaVersion = "2.5.13"
val akkaHttpVersion = "10.1.3"
val log4j2Version = "2.9.0"
val mysqlDriverVersion = "8.0.12"
val slickVersion = "3.2.3"
val scalatestFullVersion = "3.0.3"
val scalaMockVersion = "3.6.0"
val persistInmemVersion = "2.5.1.1"


javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++=Seq(
  "com.typesafe.akka"        %%  "akka-actor"                   % akkaVersion,
  "com.typesafe.akka"        %%  "akka-stream"                  % akkaVersion,
  "com.typesafe.akka"        %%  "akka-remote"                  % akkaVersion,
  "com.typesafe.akka"        %%  "akka-cluster"                 % akkaVersion,
  "com.typesafe.akka"        %%  "akka-cluster-tools"           % akkaVersion,
  "com.typesafe.akka"        %%  "akka-cluster-sharding"        % akkaVersion,
  "com.typesafe.akka"        %%  "akka-http"                    % akkaHttpVersion,
  "com.typesafe.akka"        %%  "akka-http-spray-json"         % akkaHttpVersion,
  "com.typesafe.akka"        %%  "akka-slf4j"                   % akkaVersion,
  "org.apache.logging.log4j" %   "log4j-slf4j-impl"             % log4j2Version,
  "org.apache.logging.log4j" %   "log4j-api"                    % log4j2Version,
  "org.apache.logging.log4j" %   "log4j-core"                   % log4j2Version,

  // akka persistence
  "com.typesafe.akka"        %% "akka-persistence"              % akkaVersion,
  "com.github.dnvriend"      %% "akka-persistence-inmemory"     % persistInmemVersion,

  "mysql"                    %   "mysql-connector-java"         % mysqlDriverVersion   % Runtime,
  "com.zaxxer"               %   "HikariCP"                     % "2.7.8",
  "com.typesafe.slick"       %%  "slick"                        % slickVersion,
  "com.typesafe.slick"       %%  "slick-hikaricp"               % slickVersion,
  
  "com.twitter"              %%  "chill-akka"                   % "0.9.2",
  "org.apache.commons"       %   "commons-lang3"                % "3.0",
  
  "com.typesafe.akka"        %%  "akka-http-testkit"            % akkaHttpVersion      % Test,
  "com.typesafe.akka"        %%  "akka-stream-testkit"          % akkaVersion          % Test,
  "org.specs2"               %%  "specs2-core"                  % "4.3.4"              % Test,
  "org.specs2"               %%  "specs2-mock"                  % "4.3.4"              % Test,
  "org.scalatest"            %%  "scalatest"                    % scalatestFullVersion % Test,
  "org.scalamock"            %%  "scalamock-scalatest-support"  % scalaMockVersion     % Test
)

enablePlugins(DockerPlugin)

dockerfile in docker := {
  // The assembly task generates a fat JAR file
  val artifact: File = assembly.value
  val artifactTargetPath = s"/src/${artifact.name}"

  new Dockerfile {
    from("java")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
    expose(9000)
  }
}

imageNames in docker := Seq(
  // Sets the latest tag
  ImageName(s"${organization.value}/${name.value}:latest"),

  // Sets a name with a tag that contains the project version
  ImageName(
    namespace = Some(organization.value),
    repository = name.value,
    tag = Some("v" + version.value)
  )
)
