#!/bin/bash
_BASEDIR=$(cd $(dirname $0); cd ..;cd ..; pwd)

#source ${_BASEDIR}/bin/env_setting.sh

set -o nounset

echo ${_BASEDIR}

#JAR_FILE=${_BASEDIR}/lib/mt-search-rank-assembly-1.0.jar
JAR_FILE=${_BASEDIR}/target/scala-2.11/mt-search-rank-assembly-1.0.jar
SPARK_SUBMIT="/Users/huchao/tools/spark-2.2.1-bin-hadoop2.6/bin/spark-submit \
              --master local "

action="train"
#action="train2params"
inputFile="/Users/huchao/data/MovieLens/ml-100k"
cvModelPath="/Users/huchao/hive/data/model/cv_als"
modelPath="/Users/huchao/hive/data/model/als"
userFeatPath="/Users/huchao/hive/data/feature/mf_user_feature"
movieFeatPath="/Users/huchao/hive/data/feature/mf_movie_feature"

begin_time=$(date +%s)

$SPARK_SUBMIT \
        --class com.meitu.search.rank.application.model.MatrixFactorizationDriver \
        ${JAR_FILE} \
        --action ${action} \
        --inputFile ${inputFile} \
        --cvModelPath ${cvModelPath} \
        --modelPath ${modelPath} \
        --userFeatPath ${userFeatPath} \
        --movieFeatPath ${movieFeatPath}

end_time=$(date +%s)
cost_time=$(($end_time-$begin_time))
echo "========================="
echo "执行脚本一共花费$cost_time秒"


