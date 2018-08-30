# Jupyter+Pyspark研发环境搭建
## 1.环境准备
* Anaconda 4.3.22
* java 1.8.0_101
* spark-2.0.2-bin-hadoop2.6

## 2.基础组件安装
### 2.1.Anaconda安装
(1).软件下载

Anaconda是python科学计算的集成，从Anaconda官网(http://continuum.io/downloads)下载对应的版本，一般Mac下面可以下载：[Anaconda2-4.3.22-Linux-x86_64.sh](http://continuum.io/downloads%E4%B8%8B%E8%BD%BD%E5%AF%B9%E5%BA%94%E7%9A%84%E7%89%88%E6%9C%AC%EF%BC%8C%E4%B8%80%E8%88%ACMac%E4%B8%8B%E9%9D%A2%E5%8F%AF%E4%BB%A5%E4%B8%8B%E8%BD%BD%EF%BC%9AAnaconda2-4.3.22-Linux-x86_64.sh)
```sh
sh Anaconda2-4.3.22-Linux-x86_64.sh
#使用如下命令安装，不会有默认提示，且不写入当前的环境变量中
# sh Anaconda2-4.1.1-Linux-x86_64.sh -b -p /usr/local/anaconda2
```
检查环境变量是否加入，
```sh
vim ~/.bash_profile
export PYTHONPATH=/Users/huchao/anaconda/bin
export PATH="/Users/huchao/anaconda/bin:$PATH"
```

(2).软件安装

安装过程中会给出提示信息
首先会阅读说明文件，到结尾的时候，提示是否安装，输入：yes
然后会提示输入安装路径，默认安装在/root/anaconda目录下面，这里可以更改到：/usr/local/anaconda
过程中会提示，是否将环境变量写入/root/.bashrc，输入：no

(3).安装完成 最终成功之后回给出如下提示：

```sh
You may wish to edit your .bashrc or prepend the Anaconda2 install location:

$ export PATH=/usr/local/anaconda2/bin:$PATH


You may wish to edit your .bashrc or prepend the Anaconda2 install location:

$ export PATH=/usr/local/anaconda2/bin:$PATH

+ echo 'Thank you for installing Anaconda2!

Share your notebooks and packages on Anaconda Cloud!
Sign up for free: https://anaconda.org
'
Thank you for installing Anaconda2!

Share your notebooks and packages on Anaconda Cloud!
Sign up for free: https://anaconda.org
```

### 2.2.Java安装
下载对应的java版本进行安装，该过程简略。
在Mac下可用使用brew命令进行快捷安装，如下所示：
```sh
brew install java8
```

### 2.3.spark安装
下载spark-2.0.2-bin-hadoop2.6，并解压缩。
如，我们解压缩到如下所示目录：/Users/huchao/tools/spark-2.0.2-bin-hadoop2.6

我们将spark加入到环境变量中，并配置pyspark相关变量，
```sh
vim ~/.bash_profile
export SPARK_HOME=/Users/huchao/tools/spark-2.0.2-bin-hadoop2.6
export PATH=$PATH:$SPARK_HOME/bin
```

## 3.Jupyter集成环境安装
Anaconda自带ipython和jupyter，在安装完成上述软件后，可使用如下所示命令查看已经安装的kernel：
```sh
jupyter kernelspec list
```

### 3.1.pyspark安装
* 安装相关依赖包
```sh
cd /data/sysdir/spark/python/
cp -r pyspark/ ~/tools/anaconda2/pkgs/

pip install py4j
```
* 将spark(python版)环境加入python notebook。
* 执行脚本创建profile_spark
```sh
ipython profile create spark

[ProfileCreate] Generating default config file: u'/Users/huchao/.ipythonprofile_spark/ipython_config.py'
[ProfileCreate] Generating default config file: u'/Users/huchao/.ipython/profile_spark/ipython_kernel_config.py'
```
* 在目录$user/.ipython/profile_spark/startup下面新建notebook启动文件00-pyspark-setup.py，文件内容如下：
```py
import os
import sys

# Configure the environment
if 'SPARK_HOME' not in os.environ:
    os.environ['SPARK_HOME'] = '/Users/huchao/tools/spark-2.0.2-bin-hadoop2.6'

 # Create a variable for our root path
SPARK_HOME = os.environ['SPARK_HOME']

# Add the PySpark/py4j to the Python Path
sys.path.insert(0, os.path.join(SPARK_HOME, "python", "build"))
sys.path.insert(0, os.path.join(SPARK_HOME, "python"))
```

* 环境变量配置
```sh
export PYTHONPATH=$SPARK_HOME/python/:$PYTHONPATH
export PYTHONPATH=$SPARK_HOME/python/lib/py4j-0.8.2.1-src.zip:$PYTHONPATH
# 如下两个参数必须添加，否则启动默认是终端启动
# 指定pyspark通过jupyter启动
export PYSPARK_DRIVER_PYTHON=jupyter
# 指定.jupyter下的配置文件
export PYSPARK_DRIVER_PYTHON_OPTS="notebook"
IPYTHON_OPTS="notebook"$SPARK_HOME/bin/pyspark
```

* 启动pyspark:
```sh
pyspark

# jupyter notebook --config=.ipython/profile_spark/startup/00-pyspark-setup.py --no-browser --certfile=.jupyter/mycert.pem --ip=10.22.240.157 --port=8889
```
```
[TerminalIPythonApp] WARNING | Subcommand `ipython notebook` is deprecated and will be removed in future versions.
[TerminalIPythonApp] WARNING | You likely want to use `jupyter notebook` in the future
[W 16:57:31.240 NotebookApp] Unrecognized JSON config file version, assuming version 1
[I 16:57:31.735 NotebookApp] [nb_conda_kernels] enabled, 3 kernels found
[I 16:57:31.811 NotebookApp] ✓ nbpresent HTML export ENABLED
[W 16:57:31.811 NotebookApp] ✗ nbpresent PDF export DISABLED: No module named nbbrowserpdf.exporters.pdf
[I 16:57:31.813 NotebookApp] [nb_conda] enabled
[I 16:57:31.841 NotebookApp] [nb_anacondacloud] enabled
[I 16:57:31.845 NotebookApp] Serving notebooks from local directory: /Users/huchao
[I 16:57:31.845 NotebookApp] 0 active kernels
[I 16:57:31.845 NotebookApp] The Jupyter Notebook is running at: https://localhost:8888/
[I 16:57:31.845 NotebookApp] Use Control-C to stop this server and shut down all kernels (twice to skip confirmation).
[I 16:57:38.287 NotebookApp] 302 GET / (10.168.41.18) 0.53ms
```
这样新建python文件就可以使用spark环境了。

### 3.2.Jupyter安装
Anaconda自带有Jupyter，因此不需要单独安装Jupyter。
```sh
pip install jupyter
```
也可通过如上所示命令安装。

Jupyter安装成功，但是默认配置下只能从本地访问(http://127.0.0.1:8888)。 如果你只在本机使用notebook，那么下一个步可以省了。如果你想把notebook做为公共服务供其它人使用，配置允许远程访问：

* 生成配置文件(生成的配置文件位于 ~/.jupyter/jupyter_notebook_config.py)：
```sh
jupyter notebook --generate-config
```

* 生成自签名SSL证书：
```sh
cd ~/.jupyter
openssl req -x509 -nodes -days 365 -newkey rsa:1024 -keyout notebook_cert.key -out notebook_cert.pem
```

* 生成一个密码hash：
```sh
python -c "import IPython;print(IPython.lib.passwd())"
```
示例hash串：sha1:991ec9cd2f39:522598e19891bab1ecaa3a9072e71f45811af9f2

* 编辑配置文件：
```sh
vim ~/jupyter/jupyter_notebook_config.py
```
```py
c = get_config()
c.NotebookApp.certfile = u'/data/home/huchao/.jupyter/notebook_cert.pem'
c.NotebookApp.keyfile = u'/data/home/huchao/.jupyter/notebook_cert.key'
c.NotebookApp.password = u'sha1:991ec9cd2f39:522598e19891bab1ecaa3a9072e71f45811af9f2'
c.NotebookApp.ip = '127.0.0.1'
c.NotebookApp.port = 8080
c.NotebookApp.open_browser = False
```

再次启动Notebook：
```sh
jupyter notebook
```
* 如果你开启了防火墙，配置打开8080端口。
* 使用浏览器访问：https://your_domainor_IP:8080
* https不可以省，负责会出现如下错误：貌似不能从http自动重定向为https。
```sh
[W 10:16:14.157 NotebookApp] SSL Error on 8 ('192.168.0.109', 51582): [SSL: WRONG_VERSION_NUMBER] wrong version number (_ssl.c:590)
```
* 由于使用的是自签名证书，浏览器会发出警告信息。要使用Let’s Encrypt，参考http://jupyter-notebook.readthedocs.io/en/latest/public_server.html#notebook-server-security。

### 3.3.Jupyter-scala安装
```sh
jupyter kernelspec list
```
默认情况下，只安装了
```
Available kernels:
python2 /data1/anaconda/lib/python2.7/site-packages/ipykernel/resources
```
当环境配置为：spark 2.0 +2.11scala
应该安装toree的版本为[toree 0.2.0.dev1](https://anaconda.org/hyoon/toree)此方法需要 python 2.7 +conda，
```sh
sudo /usr/local/anaconda2/bin/pip install https://dist.apache.org/repos/dist/dev/incubator/toree/0.2.0/snapshots/dev1/toree-pip/toree-0.2.0.dev1.tar.gz
```
切换到hadoopuser用户，通过如下命令安装：
```
 /usr/local/anaconda2/bin/jupyter toree install --interpreters=Scala --spark_home=/www/harbinger-spark/ --user --kernel_name=apache_toree --interpreters=PySpark,SparkR,Scala,SQL
```
检查一下现在的kernel列表：
```sh
jupyter kernelspec list
```
```
Available kernels:
  scala210                /home/hadoopuser/.ipython/kernels/scala210
  python2                 /usr/local/anaconda2/lib/python2.7/site-packages/ipykernel/resources
  apache_toree_pyspark    /home/hadoopuser/.local/share/jupyter/kernels/apache_toree_pyspark
  apache_toree_scala      /home/hadoopuser/.local/share/jupyter/kernels/apache_toree_scala
  apache_toree_sparkr     /home/hadoopuser/.local/share/jupyter/kernels/apache_toree_sparkr
  apache_toree_sql        /home/hadoopuser/.local/share/jupyter/kernels/apache_toree_sql
```

## 4.代理反射设置
比如我们在服务器192.168.199.107上以hadoopuser账户启动了：
```
$ jupyter notebook

[W 13:27:32.665 NotebookApp] Unrecognized JSON config file version, assuming version 1
[I 13:27:33.760 NotebookApp] [nb_conda_kernels] enabled, 6 kernels found
[I 13:27:33.860 NotebookApp] ✓ nbpresent HTML export ENABLED
[W 13:27:33.860 NotebookApp] ✗ nbpresent PDF export DISABLED: No module named nbbrowserpdf.exporters.pdf
[I 13:27:33.864 NotebookApp] [nb_conda] enabled
[I 13:27:33.905 NotebookApp] [nb_anacondacloud] enabled
[I 13:27:33.907 NotebookApp] Serving notebooks from local directory: /home/hadoopuser/hc/search
[I 13:27:33.907 NotebookApp] 0 active kernels
[I 13:27:33.907 NotebookApp] The Jupyter Notebook is running at: https://127.0.0.1:8989/
[I 13:27:33.907 NotebookApp] Use Control-C to stop this server and shut down all kernels (twice to skip confirmation).
```

需要在Mac本上配置：
```sh
vim .ssh/config
```

配置如下：
```
# 跳板机配置
Host gateway
    HostName xxx.xxx.xxx.xxx
    User hc
    Port 34185
# 目标服务器配置
Host dev
    User hadoopuser
    HostName xxx.xxx.xxx.xxx
    Port 34185
    ProxyCommand ssh -q -W %h:%p gateway
```

启动：
```sh
ssh dev -L127.0.0.1:2255:127.0.0.1:8989
```

在本机Chrome浏览器上输入如下地址即可访问：
```
https://127.0.0.1:2255/tree/#
```


## 常见问题：

***SSL Error on 6 ('127.0.0.1', 46565): [SSL: WRONG_VERSION_NUMBER] wrong version number (_ssl.c:590)***

解决方案：将http协议改为https协议即可
```
http://127.0.0.1:2255/tree    ==>    https://127.0.0.1:2255/tree
```


***出错为：Unsupported major.minor version 52.0的解决办法***

```
Exception in thread "main" java.lang.UnsupportedClassVersionError: com/typesafe/config/ConfigMergeable : Unsupported major.minor version 52.0
```
出错原因为：Unsupported major.minor version 52.0
经分析，问题应该出在版本不对应，
查阅资料：
```
Java SE 9 = 53,
Java SE 8 = 52,
Java SE 7 = 51,
Java SE 6.0 = 50,
Java SE 5.0 = 49,
JDK 1.4 = 48,
JDK 1.3 = 47,
JDK 1.2 = 46,
JDK 1.1 = 45
```
检查版本情况：
Java版本：
```sh
[hadoopuser@BJSH-DATATEST-199-107.meitu-inc.com ~]$ java -version
java version "1.7.0_80"
Java(TM) SE Runtime Environment (build 1.7.0_80-b15)
Java HotSpot(TM) 64-Bit Server VM (build 24.80-b11, mixed mode)
```
javac版本：
```sh
[hadoopuser@BJSH-DATATEST-199-107.meitu-inc.com ~]$ javac -version
javac 1.7.0_80
```
可以看到java的版本为jdk1.7.0_80，而报错提示是52对应的是java8版本，因此在当前用户的环境变量中设置JAVA版本为java 8的版本
```sh
export JAVA_HOME=/usr/java/jdk1.8.0_51
export JAVA_BIN=/usr/java/jdk1.8.0_51/bin
export PATH=$PATH:${JAVA_HOME}/bin
export CLASSPATH=.:${JAVA_HOME}/lib/dt.jar:${JAVA_HOME}/lib/toools.jar
export JAVA_HOME JAVA_BIN PATH CLASSPATH
```


```
jupyter notebook --no-browser --port 6000 --ip=192.168.1.103
```













.
