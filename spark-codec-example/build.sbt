import AssemblyKeys._

assemblySettings

name := "spark-codec-example"

version := "1.0"

organization := "com.open"

scalaVersion := "2.11.8"

//publishMavenStyle := true

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

// additional spark Libraries
val sparkVersion = "2.1.0"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided"
)
// https://mvnrepository.com/artifact/com.databricks/spark-csv
libraryDependencies += "com.databricks" %% "spark-csv" % "1.4.0"

val hanlpVersion = "portable-1.6.3"
libraryDependencies += "com.hankcs" % "hanlp" % hanlpVersion

// additional scala libraries
libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.11.8" % "provided",
  "org.scala-lang" % "scala-reflect" % "2.11.8" % "provided",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4" % "provided",
  "org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.4" % "provided"
)

// additional log libraries
libraryDependencies ++= Seq(
  "log4j" % "log4j" % "1.2.14" % "provided",
  "org.slf4j" % "slf4j-api" % "1.7.25" % "provided",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % "provided"
)

libraryDependencies ++= Seq(
  "com.github.scopt" % "scopt_2.10" % "3.2.0",
  "junit" % "junit" % "4.11" % "test"
)

// 引用glint参数服务器,详见tools/glint,http://rjagerman.github.io/glint/
//libraryDependencies += "ch.ethz.inf.da" %% "glint" % "0.1-SNAPSHOT"
//val angelVersion = "1.5.1"
//libraryDependencies += "com.tencent.angel" % "spark-on-angel-core" % angelVersion
//libraryDependencies += "com.tencent.angel" % "spark-on-angel-mllib" % angelVersion

// https://mvnrepository.com/artifact/ml.dmlc/xgboost4j-spark
// lambdaMart算法
libraryDependencies += "ml.dmlc" % "xgboost4j-spark" % "0.72"

// 时间工具类
libraryDependencies += "joda-time" % "joda-time" % "2.9.9"
// https://mvnrepository.com/artifact/com.hadoop.gplcompression/hadoop-lzo
//libraryDependencies += "com.hadoop.gplcompression" % "hadoop-lzo" % "0.4.19"
//libraryDependencies += "com.hadoop.gplcompression" % "hadoop-lzo" % "0.4.21-SNAPSHOT" % "provided"

libraryDependencies += "com.esotericsoftware.kryo" % "kryo" % "2.16"
libraryDependencies += "commons-lang" % "commons-lang" % "2.4"
libraryDependencies += "com.google.guava" % "guava" % "18.0"
//libraryDependencies += "junit" % "junit" % "4.11" % "test"
//libraryDependencies += "com.alibaba" % "fastjson" % "1.2.47"

// 添加本地maven仓库搜索
resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
resolvers += Resolver.mavenLocal

resolvers ++= Seq(
  "ali central" at "http://maven.aliyun.com/nexus/content/repositories/central/",
  "mvn" at "https://mvnrepository.com/",
  "typesafe2" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.sonatypeRepo("public")
)

//resolvers += "Flyway" at "https://flywaydb.org/repo"
//addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.0")

dependencyOverrides += "log4j" % "log4j" % "1.2.14"
dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.16"
dependencyOverrides += "org.objenesis" % "objenesis" % "1.2"

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

mergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("org", "slf4j", xs @ _*) => MergeStrategy.first
  case x => MergeStrategy.first
}
