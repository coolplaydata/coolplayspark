## 1.问题描述
假设我们有一份test.py，使用pyspark将单个指标时间序列数据分发到不同的机器节点上，按照Prophet算法(很多异常检测算法并未提供分布式版本)执行预测和监控。

现实状况：
* 很多异常检测算法并未提供分布式版本，如Prophet等。
* spark集群的各个worker节点不提供安装第三方python环境包。
* spark集群即使提供安装，后续添加新算法，仍然需要在每台节点上安装对应的算法包。

基于以上问题，我们需要提供一种能够解决python环境在分布式环境的下的运行方式。

## 2.解决方案
经过多次调研，目前调研的可行方案如下：
* 在任意一台Centos服务器上安装Anaconda2，以此python环境为基础环境。
* 基于Anaconda2，通过conda创建python虚拟环境
* 基于创建的python虚拟环境安装工程需要使用的算法包
* 将该虚拟环境导出为统一的zip包文件，并将该zip包文件上传到spark的HDFS。
* 在spark-submit中提交作业，通过配置archives参数和spark.pyspark.python指定每台worker节点的python环境（archives指定虚拟环境分发的路径，spark.pyspark.python指定运行是获取的虚拟环境路径）
* 在spark作业提交服务器上部署Anaconda2。运行时通过配置spark.pyspark.driver.python，指定提交作业的python环境(即本机的Anaconda2)

## 3.方案明细
* 安装Anaconda2
```sh
sh Anaconda2-4.1.1-Linux-x86_64.sh -b -p /usr/local/anaconda2
```
* 创建虚拟环境
```sh
/usr/local/anaconda2/bin/conda create --name pylib
source activate pylib# 在该~/.conda/envs/pylib/目录下存在pylib的虚拟环境
```
* 安装相关算法包
```sh
conda install arrow
conda install -c conda-forge fbprophet
```
* 导出虚拟环境并上传到hdfs
```sh
cd ~/.conda/envs
zip -r pylib.zip pylib/
hadoop fs -put pylib.zip hdfs://xxx/user/hadoopuser/hc/pyenv/
```
* 提交运行代码
```sh
/www/apache-spark/bin/spark-submit \
            --queue rec \
            --master yarn \
            --deploy-mode client \
            --driver-memory 3G \
            --executor-memory 6G \
            --executor-cores 2 \
            --driver-cores 2 \
            --conf "spark.pyspark.python=./pylib.zip/pylib/bin/python" \ # 每个Worker节点上的python环境引用
            --conf "spark.pyspark.driver.python=/usr/local/anaconda2/bin/python" \ # Driver节点的python环境
            --archives "hdfs://xxx/user/hadoopuser/hc/pyenv/pylib.zip" \ # 虚拟python环境分发
            monitor_distribute_test.py
```


基于CentOS的虚拟环境包参考[pylib](https://pan.baidu.com/s/1VpyKAu8T8rY_x7bfxUPARg)




。
