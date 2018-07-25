* [00Spark简介](./doc/00SparkIntroduction/README.md)
  * Spark简介
  * Spark应用场景和案例
* [01Spark架构](./doc/01SparkArchitecture/README.md)
  * spark submit
    * standalone
    * cluster(on yarn)
  * spark DAG
    * schdule
    * stage 
* [02Spark核心概念](./doc/02SparkCore/README.md)
  * RDDs
  * Accumulators
  * Boardcasts Vars
  * Wide&Narrow Dependency
* [03SparkSQL模块](./doc/03SparkSQL/README.md)
  * SparkSQL模块概述
    * SQL语言
    * 数据结构：DataFrame和DataSet
  * 基础概念
    * SparkSession
    * ...
  * 数据源
    * 数据读取与保存
    * Parquet格式文件
    * ORC格式文件
    * JSON数据集
    * Hive数据表
    * JDBC数据源
    * [spark 读取数据](./doc/03SparkSQL/spark-load.md)
  * 性能优化
  * 分布式SQL引擎
  * Pyspark和Pandas交互
  * 版本变更历史
* [04SparkStreaming模块](./doc/04SparkStreaming/README.md)
  * structured streaming
    * 01Structured Streaming概述
    * 02编程模型
    * 03基于Dataset和DataFrame的API
    * 04连续处理
    * 05其他
  * spark streaming
    * 基础概念
      * 初始化StreamingContext
      * 输入DStreams和Receivers
      * 基于DStreams的转换操作
      * 基于DStreams的输出操作
      * DataFrame和SQL操作
      * MLLib操作
      * 缓存Cache
      * 检查点Checkpoint
      * Accumulators, Broadcast Variables, and Checkpoints
      * 应用部署
      * 应用监控
    * 性能调优
    * 容错语义
* [05SparkMLLib模块](./doc/05SparkMLLib/README.md)
  * 基于DataFrame的ml模块
    * 01基础统计算法
    * 02Pipline管道操作
    * 03特征工程
    * 04分类与回归算法
    * 05聚类算法
    * 06协同过滤算法
    * 07频繁模式挖掘
    * 08模型调优
    * 09优化算法
  * 基于RDD的mllib模块
    * 01数据类型
    * 02基础统计算法
    * 03分类与回归算法
    * 04协同过滤算法
    * 05聚类算法
    * 06降维算法
    * 07特征工程
    * 08频繁模式挖掘
    * 09模型评估算法
    * 10预测模型标记语言模型导出
    * 优化算法
* [06SparkGraphX](./doc/06SparkGraphX/README.md)
  * 01Graph简介
  * 02Graph核心RDD
    01顶点RDD
    02边RDD
  * 03Graph算子
    * 01图算子列表
    * 02属性算子
    * 03结构算子
    * 04连接算子
    * 05邻近聚合算子
    * 06缓存算子
  * 04PregelAPI
  * 05Graph构建
  * 06Graph优化
  * 07Graph算法
    * PageRank算法
    * 联通组件算法
    * 标签传播算法
