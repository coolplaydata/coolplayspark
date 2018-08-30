#!/bin/bash

_BASEDIR=$(cd $(dirname $0); cd ..; cd ..; pwd)
cd ${_BASEDIR}

set -o nounset

dat_file="recsys-content-spark-1.0.tar.gz"
proj_name="recsys-content-spark"

if [ -f ${_BASEDIR}/target/${dat_file} ];then
    rm -rf ${_BASEDIR}/target/${dat_file}
    echo "rm the older ${dat_file} file."
fi

sbt clean compile assembly

mkdir -p ${_BASEDIR}/target/${proj_name}/bin/
mkdir -p ${_BASEDIR}/target/${proj_name}/lib/
mkdir -p ${_BASEDIR}/target/${proj_name}/logs/
mkdir -p ${_BASEDIR}/target/${proj_name}/conf/

JAR_FILE=${_BASEDIR}/target/scala-2.11/${proj_name}-assembly-1.0.jar
echo ${JAR_FILE}

cp ${_BASEDIR}/src/script/start.sh ${_BASEDIR}/target/${proj_name}/bin/
cp ${_BASEDIR}/src/main/resources/* ${_BASEDIR}/target/${proj_name}/conf/

cp ${JAR_FILE} ${_BASEDIR}/target/${proj_name}/lib/

cd ${_BASEDIR}/target/
tar -zcvf ${dat_file} ${proj_name}/

rsync -av ${dat_file} root@124.243.219.212::data_dev_upload/

cd ${_BASEDIR}
rm -rf ${_BASEDIR}/target/${proj_name}/
