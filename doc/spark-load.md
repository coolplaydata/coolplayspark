# Spark 读取数据 源码解析
# 问题描述
在使用spark读取HDFS上的数据时，经常使用load的方式（没有hive的情况下）
```scala
spark.read.schema(schema).load(data_path)
```
以前比较常见的是textFile读HDFS的方式，不同于此，load的方式可以直接形成DataFrame，使用上更方便一些。遇到的一个问题是在读取的目录下非常多的碎片文件时，1.load地方为什么单独形成了一个job？2.文件过多时直接OOM程序停止。
在网上能搜到的比较类似的问题：
>[spark-read-parquet-takes-too-much-time](https://stackoverflow.com/questions/47985913/spark-read-parquet-takes-too-much-time)

出于好奇，让我们看看load的源码 (2.3.1)

# 源码追踪
## load方法
首先点进load方法，发现是spark sql的包(org.apache.spark.sql)
```scala
  /**
   * Loads input in as a `DataFrame`, for data sources that support multiple paths.
   * Only works if the source is a HadoopFsRelationProvider.
   *
   * @since 1.6.0
   */
  @scala.annotation.varargs
  def load(paths: String*): DataFrame = {
    sparkSession.baseRelationToDataFrame(
      DataSource.apply(
        sparkSession,
        paths = paths,
        userSpecifiedSchema = userSpecifiedSchema,
        className = source,
        options = extraOptions.toMap).resolveRelation())
  }
```

入参是目录，可以是多个，返回值是DataFrame。主要是调用了sparkSession.baseRelationToDataFrame
我们看一下这个baseRelationToDataFrame
```scala
  /**
   * Convert a `BaseRelation` created for external data sources into a `DataFrame`.
   *
   * @since 2.0.0
   */
  def baseRelationToDataFrame(baseRelation: BaseRelation): DataFrame = {
    Dataset.ofRows(self, LogicalRelation(baseRelation))
  }

```

这个方法是使用一个BaseRelation来得到DataFrame。在load方法里，我们传入的就是一个BaseRelation的子类。那我们看看究竟传入了什么样的BaseRelation。

## DataSource类
在load函数里，通过DataSource伴生类调用apply方法获取一个DataSource实例，然后使用resolveRelation()方法返回的BaseRelation，看来关键在这个DataSource类
对于这个类，网上也有一些他人的分享：
>《[利用 Spark DataSource API 实现Rest数据源](https://www.jianshu.com/p/6441eaa4d064) 》
>《[Spark Data Source API: Extending Our Spark SQL Query Engine](https://mapr.com/blog/spark-data-source-api-extending-our-spark-sql-query-engine/)》

这个类在spark读取数据部分十分重要，有接近一千行代码。
我们这里只看其中一小部分。


首先看一下传入伴生类的参数：
```
        //sparkSession没什么好说的
        sparkSession,
        //传入的hdfs目录
        paths = paths,
        //传入的定义好的Schema
        userSpecifiedSchema = userSpecifiedSchema,
        //数据源使用的类，默认parquet这里用的也是parquet
        className = source,
        //option()的方式传入的都在这里
        options = extraOptions.toMap
```

然后看看调用的那个方法resolveRelation()
>注释翻译：
创建一个已解析的[[BaseRelation]]，可用于从中读取数据或将数据写入

```scala
 /**
   * Create a resolved [[BaseRelation]] that can be used to read data from or write data into this
   * [[DataSource]]
   *
   * @param checkFilesExist Whether to confirm that the files exist when generating the
   *                        non-streaming file based datasource. StructuredStreaming jobs already
   *                        list file existence, and when generating incremental jobs, the batch
   *                        is considered as a non-streaming file based data source. Since we know
   *                        that files already exist, we don't need to check them again.
   */
  def resolveRelation(checkFilesExist: Boolean = true): BaseRelation = {
    val relation = (providingClass.newInstance(), userSpecifiedSchema) match {
      ...
      case (dataSource: SchemaRelationProvider, Some(schema)) => ...
      case (dataSource: RelationProvider, None) => ...
      case (_: SchemaRelationProvider, None) => ...
      case (dataSource: RelationProvider, Some(schema)) => ...
      // This is a non-streaming file based datasource.
      case (format: FileFormat, _) => ...

      case _ => ...
    }

    relation
  }
```

这个方法会match(providingClass.newInstance(), userSpecifiedSchema)来case选择返回哪种relation，第二个很好理解，就是传入的用户自定义的Schema，第一个参数追踪一下：
```scala
    ...
    lazy val providingClass: Class[_] = DataSource.lookupDataSource(className)
    ...
    /** Given a provider name, look up the data source class definition. */
  def lookupDataSource(provider: String): Class[_] = {
    ...
    val provider1 = backwardCompatibilityMap.getOrElse(provider, provider)
    val provider2 = s"$provider1.DefaultSource"
    val loader = Utils.getContextOrSparkClassLoader
    val serviceLoader = ServiceLoader.load(classOf[DataSourceRegister], loader)
    ...
  }
  ...
  /** A map to maintain backward compatibility in case we move data sources around. */
  private val backwardCompatibilityMap: Map[String, String] = {
    val jdbc = classOf[JdbcRelationProvider].getCanonicalName
    val json = classOf[JsonFileFormat].getCanonicalName
    val parquet = classOf[ParquetFileFormat].getCanonicalName
    val csv = classOf[CSVFileFormat].getCanonicalName
    val libsvm = "org.apache.spark.ml.source.libsvm.LibSVMFileFormat"
    val orc = "org.apache.spark.sql.hive.orc.OrcFileFormat"

    Map(
      ...
      "org.apache.spark.sql.parquet" -> parquet,
      "org.apache.spark.sql.parquet.DefaultSource" -> parquet,
      "org.apache.spark.sql.execution.datasources.parquet" -> parquet,
      ...
    )
  }
```

从上面的代码可以看出这个第一个参数应该是ParquetFileFormat，它是特质FileFormat的一个实现类，所以在resolveRelation进入的分支是：case (format: FileFormat, _)

```scala
case (format: FileFormat, _) =>
        val allPaths = caseInsensitiveOptions.get("path") ++ paths
        val hadoopConf = sparkSession.sessionState.newHadoopConf()
        val globbedPaths = allPaths.flatMap { path =>
          val hdfsPath = new Path(path)
          val fs = hdfsPath.getFileSystem(hadoopConf)
          val qualified = hdfsPath.makeQualified(fs.getUri, fs.getWorkingDirectory)
          val globPath = SparkHadoopUtil.get.globPathIfNecessary(qualified)

          if (globPath.isEmpty) {
            throw new AnalysisException(s"Path does not exist: $qualified")
          }
          // Sufficient to check head of the globPath seq for non-glob scenario
          // Don't need to check once again if files exist in streaming mode
          if (checkFilesExist && !fs.exists(globPath.head)) {
            throw new AnalysisException(s"Path does not exist: ${globPath.head}")
          }
          globPath
        }.toArray

        val (dataSchema, partitionSchema) = getOrInferFileFormatSchema(format)

        val fileCatalog = if (sparkSession.sqlContext.conf.manageFilesourcePartitions &&
            catalogTable.isDefined && catalogTable.get.tracksPartitionsInCatalog) {
          val defaultTableSize = sparkSession.sessionState.conf.defaultSizeInBytes
          new CatalogFileIndex(
            sparkSession,
            catalogTable.get,
            catalogTable.get.stats.map(_.sizeInBytes.toLong).getOrElse(defaultTableSize))
        } else {
          new InMemoryFileIndex(sparkSession, globbedPaths, options, Some(partitionSchema))
        }

        HadoopFsRelation(
          fileCatalog,
          partitionSchema = partitionSchema,
          dataSchema = dataSchema.asNullable,
          bucketSpec = bucketSpec,
          format,
          caseInsensitiveOptions)(sparkSession)
```

从allPaths到globbedPaths，调用了hdfsPath相关的包和方法，应该是根据传入的path获取全局的目录(所有子目录)及判断目录是否存在等操作。
接着这一行：
```scala
val (dataSchema, partitionSchema) = getOrInferFileFormatSchema(format)
```
是根据元数据判自行判断出Schema，后面代码很长，由于我在程序里传入schema，所以着一步应该是会跳过的。网上有人做过实验，传入schema和不传入对比，传入读取数据会更快，具体文章找不到了。
接下来看fileCatalog的形成，这里地方就有坑了，在没有元数据的情况下是会
>注释翻译:
一个[[FileIndex]]，通过递归列出所有文件来生成要处理的文件列表
文件存在于`paths`中。

```scala
new InMemoryFileIndex(sparkSession, globbedPaths, options, Some(partitionSchema))
...
**
 * A [[FileIndex]] that generates the list of files to process by recursively listing all the
 * files present in `paths`.
 *
 * @param rootPaths the list of root table paths to scan
 * @param parameters as set of options to control discovery
 * @param partitionSchema an optional partition schema that will be use to provide types for the
 *                        discovered partitions
 */
class InMemoryFileIndex(...){
    ...
}
```
从注释中可以看到，它通过递归列出`paths`中存在的所有文件来生成要处理的文件列表。不禁要想，如果碎片化相当严重，那么这个地方会把所有的文件都列出来，是个多么庞大的实例呀......
问题描述中的第二个问题应该就在这里了。至于第一个问题，为什么在碎片化比较多的情况下多执行了一个job，我并没有找到相关的代码。以后找到再补上。

接着往下看，返回一个HadoopFsRelation，并且把fileCatalog也放到这里类里返回里，在后面的结果中，这个应该是会发到各个executor的，所以当fileCatalog很大很大的时候，不禁会在driver内存有要求，executor的内存也有要求。

关于DataSource的resolveRelation方法就看到这里。

#
现在得到了HadoopFsRelation是BaseRelation的子类，上文说的load函数需要的关键点。那么看到这里还有一个问题，从BaseRelation是怎么到DataFrame的呢？
再回到这个方法：
```scala
  /**
   * Convert a `BaseRelation` created for external data sources into a `DataFrame`.
   *
   * @since 2.0.0
   */
  def baseRelationToDataFrame(baseRelation: BaseRelation): DataFrame = {
    Dataset.ofRows(self, LogicalRelation(baseRelation))
  }
  ...

  package org.apache.spark.sql

  private[sql] object Dataset {
  def apply[T: Encoder](sparkSession: SparkSession, logicalPlan: LogicalPlan): Dataset[T] = {
    new Dataset(sparkSession, logicalPlan, implicitly[Encoder[T]])
  }

  def ofRows(sparkSession: SparkSession, logicalPlan: LogicalPlan): DataFrame = {
    val qe = sparkSession.sessionState.executePlan(logicalPlan)
    qe.assertAnalyzed()
    new Dataset[Row](sparkSession, qe, RowEncoder(qe.analyzed.schema))
  }
}
```
简单来说就是：
> baseRelation -> logicalPlan -> executePlan -> Dataset[Row] (DataFrame)

从baseRelation到logicalPlan是由
org.apache.spark.sql.execution.datasources包下的
LogicalRelation类处理得到的。
注释翻译：
>LogicalRelation类用来把BaseRelation 链接到(link into) 一个逻辑 查询 计划(logical query plan)

>请注意，有时我们需要使用`LogicalRelation`来替换现有的叶节点而不更改输出属性的ID。 `expectedOutputAttributes`参数用于此目的。有关详细信息，请参阅https://issues.apache.org/jira/browse/SPARK-10741。

```scala
package org.apache.spark.sql.execution.datasources
/**
 * Used to link a [[BaseRelation]] in to a logical query plan.
 *
 * Note that sometimes we need to use `LogicalRelation` to replace an existing leaf node without
 * changing the output attributes' IDs.  The `expectedOutputAttributes` parameter is used for
 * this purpose.  See https://issues.apache.org/jira/browse/SPARK-10741 for more details.
 */
case class LogicalRelation(
    relation: BaseRelation,
    expectedOutputAttributes: Option[Seq[Attribute]] = None,
    catalogTable: Option[CatalogTable] = None)
  extends LeafNode with MultiInstanceRelation {
      ...
  }

```

然后executePlan这部分属于spark sql源码的关键部分，可以看看别人的分享：
>[Spark-Sql源码解析之七 Execute: executed Plan](https://blog.csdn.net/wl044090432/article/details/52190716)

# 结束
spark 读取数据这部分就到这里，从load方法开始，从读取HDFS上parquet文件的这种情况，看到Spark读取各种数据源的方式抽象，而DataSource类是在读取数据中的关键类，之后会使用spark sql执行逻辑计划的代码，把读取到的数据返回为DataFrame。
至于开始提到的问题，最简单的方法是加资源，或者像我一下，先用一个前置任务对目录下的碎片文件进行分批整合。