name := "LogFileGenerator"

version := "0.1"

scalaVersion := "3.2.0"

val logbackVersion        = "1.4.1"
val sfl4sVersion          = "2.0.1"
val typesafeConfigVersion = "1.4.2"
val apacheCommonIOVersion = "2.11.0"
val scalacticVersion      = "3.2.12"
val generexVersion        = "1.0.2"

resolvers += Resolver.jcenterRepo

lazy val root = (project in file("."))
  .settings(
    name             := "LogFileGenerator",
    idePackagePrefix := Some("vvakic2.uic.cs441")
  )

libraryDependencies ++= Seq(
  "ch.qos.logback"    % "logback-core"          % logbackVersion,
  "ch.qos.logback"    % "logback-classic"       % logbackVersion,
  "org.slf4j"         % "slf4j-api"             % sfl4sVersion,
  "com.typesafe"      % "config"                % typesafeConfigVersion,
  "commons-io"        % "commons-io"            % apacheCommonIOVersion,
  "org.scalactic"    %% "scalactic"             % scalacticVersion,
  "org.scalatest"    %% "scalatest"             % scalacticVersion % Test,
  "org.scalatest"    %% "scalatest-featurespec" % scalacticVersion % Test,
  "com.github.mifmif" % "generex"               % generexVersion
)

// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-common
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.3.4"
// https://mvnrepository.com/artifact/org.apache.hadoop/hadoop-mapreduce-client-core
libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-core"      % "3.3.4"
libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % "3.3.4"
libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-shuffle"   % "3.3.4"
libraryDependencies += "org.apache.hadoop" % "hadoop-mapreduce-client-common"    % "3.3.4"
libraryDependencies += "org.scalatestplus" %% "mockito-4-6"  % "3.2.14.0" % "test"
libraryDependencies += "org.mockito"        % "mockito-core" % "2.7.19"   % Test

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x                             => MergeStrategy.first
}
