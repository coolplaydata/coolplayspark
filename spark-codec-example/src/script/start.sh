#!/bin/bash
_BASEDIR=$(cd $(dirname $0); cd ..; pwd)

set -o nounset

echo ${_BASEDIR}


JAR_FILE=${_BASEDIR}/lib/recsys-content-spark-assembly-1.0.jar

# 具体参数设置需要依赖于集群提供的队列资源进行调优
SPARK_SUBMIT="/www/harbinger-spark/bin/spark-submit \
              --queue rec-meipai \
              --master yarn \
              --deploy-mode client \
              --driver-memory 5g \
              --driver-cores 3 \
              --executor-memory 7g \
              --executor-cores 5 \
              --num-executors 5 \
              --conf spark.driver.maxResultSize=5g \
              --conf spark.kryoserializer.buffer=64 \
              --conf spark.kryoserializer.buffer.max=1g "

inputFile="/user/hadoopuser/hc/nlp/corpus/SogouCmini/Sample"
labelFile="/user/hadoopuser/hc/nlp/corpus/SogouCmini/ClassList.txt"
outputDir="/user/hadoopuser/hc/nlp/result/sogouminni"
topicsDir="/user/hadoopuser/hc/nlp/result/sogouTopic"

ldaK=10
ldaMaxIter=1
ldaFeatSize=500
ldaFeatType="CountVectorizer"

begin_time=$(date +%s)

$SPARK_SUBMIT \
        --class com.open.recsys.content.driver.TextLdaDriver \
        ${JAR_FILE} \
        --labelFile ${labelFile} \
        --inputFile ${inputFile} \
        --outputDir ${outputDir} \
        --topicsDir ${topicsDir} \
        --ldaK ${ldaK} \
        --ldaMaxIter ${ldaMaxIter} \
        --ldaFeatSize ${ldaFeatSize} \
        --ldaFeatType ${ldaFeatType}

end_time=$(date +%s)
cost_time=$(($end_time-$begin_time))
echo "========================="
echo "执行脚本一共花费$cost_time秒"


