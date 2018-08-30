安装好如下软件：
* anaconda2
* findspark
* pyspark

spark1.x的版本以下的只支持python2.x

spark是分为local,standalone,yarn-client,yarn-cluster等运行模式的.既然想用jupyter,自然是想要交互式的,那么如何以不同的模式来交互呢?

### local模式
```py
import findspark
findspark.init()
from pyspark import SparkContext
sc = SparkContext("local", "First App")
```

### standalone
```py
# 需要传入地址和端口
import findspark
findspark.init()
from pyspark import SparkContext
sc = SparkContext("spark://192.168.5.129:7077", "First App")
```

### yarn-client
```py
import findspark
findspark.init()
from pyspark import SparkContext
sc = SparkContext("yarn-client", "First App")
```

示例：
```py

import findspark
findspark.init()

from pyspark.sql import HiveContext
from pyspark.sql import SparkSession

# sc = SparkContext("yarn-client", "RegisterFinderTest")
spark = SparkSession.builder \
                    # 应用名称
                    .appName("RegisterFinderTest") \
                    # 使用yarn提交作业
                    .master("yarn") \
                    # 运行模式为client
                    .config("spark.submit.deployMode", "client") \
                    # 使用队列为rec-test
                    .config("spark.yarn.queue","rec-test") \
                    # Driver端的内存8g
                    .config("spark.driver.memory","8g") \
                    # Driver端内存上限10g
                    .config("spark.driver.maxResultSize", "10g") \
                    # Driver端的cores核数为2
                    .config("spark.driver.cores", "2") \
                    # 使用的Executor节点数量为5个
                    .config("spark.executor.instances","5") \
                    # Executor节点内存为8g
                    .config("spark.executor.memory","8g") \
                    # Executor节点的核数为4核
                    .config("spark.executor.cores","4") \
                    # Executor节点最大核数为5核
                    .config("spark.cores.max","5") \
                    # Driver端的内存为8g
                    .config("spark.driver.memory","8g") \
                    # yarn模式下，Executor节点的内存Overhead值为2g
                    .config("spark.yarn.executor.memoryOverhead","2g") \
                    # 文本自动解析的字段数量最大设置为200
                    .config("spark.debug.maxToStringFields","200") \
                    .enableHiveSupport().getOrCreate()
sqlContext = HiveContext(spark.sparkContext)
```

### yarn-cluster
cluster模式一般都是开发完成后,直接用来执行用的,不适用于交互模式.
