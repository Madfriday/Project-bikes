### cluster building

mysql，redis，mogodb集群搭建需要解决的问题有两个：1，分片  2，副本 与hdfs十分相似。

redis集群搭建:

```bash

1.下载redis3的稳定版本，下载地址http://download.redis.io/releases/redis-3.2.10.tar.gz
2.上传redis-3.2.10.tar.gz到服务器
3.解压redis源码包
tar -zxvf redis-3.2.10.tar.gz -C /usr/local/src/
4.进入到源码包中，编译并安装redis
cd /usr/local/src/redis-3.2.10/
make && make install
5.报错，缺少依赖的包
 
6.配置本地YUM源并安装redis依赖的rpm包
yum -y install gcc
7.编译并安装
make && make install
8.报错，原因是没有安装jemalloc内存分配器，可以安装jemalloc或直接输入
 
9.重新编译安装
make MALLOC=libc && make install
10.用同样的方式在其他的机器上编译安装redis
11.在所有机器的/usr/local/下创建一个redis目录，然后拷贝redis自带的配置文件redis.conf到/usr/local/redis
mkdir /usr/local/redis
cp /usr/local/src/redis-3.2.10/redis.conf /usr/local/redis
12.修改所有机器的配置文件redis.conf
daemonize yes  #redis后台运行
cluster-enabled yes  #开启集群把注释去掉
appendonly yes  #开启aof日志，它会每次写操作都记录一条日志

sed -i 's/daemonize no/daemonize yes/' /usr/local/redis/redis.conf
sed -i "s/bind 127.0.0.1/ bind $HOST/" /usr/local/redis/redis.conf
sed -i 's/# cluster-enabled yes/cluster-enabled yes/' /usr/local/redis/redis.conf
sed -i 's/appendonly no/appendonly yes/' /usr/local/redis/redis.conf
sed -i 's/# cluster-node-timeout 15000/cluster-node-timeout 5000/' /usr/local/redis/redis.conf
13.启动所有的redis节点
cd /usr/local/redis
redis-server redis.conf
14.查看redis进程状态
ps -ef | grep redis
 
15.（只要在一台机器上安装即可）配置集群：安装ruby和ruby gem工具（redis3集群配置需要ruby的gem工具，类似yum）
yum -y install ruby rubygems

16.使用gem下载redis集群的配置脚本
gem install redis
ruby --version
 


17.安装RVM
curl -sSL https://rvm.io/mpapis.asc | gpg2 --import -
curl -L get.rvm.io | bash -s stable
source /usr/local/rvm/scripts/rvm
rvm list known

rvm install 2.3.4

#用ruby的工具安装reids
gem install redis
18.使用脚本配置redis集群
cd /usr/local/src/redis-3.2.10/src/
#service iptables stop
#在第一机器上执行下面的命令
./redis-trib.rb create --replicas 1 192.168.10.101:6379 192.168.10.102:6379 192.168.10.103:6379 192.168.10.104:6379 192.168.10.105:6379 192.168.10.106:6379
19.测试(别忘加-c参数)
redis-cli -c -h 192.168.1.13
	
```
---

mogodb集群搭建:

```bash
###【在多台机器上执行下面的命令
#在所有创建一个xiaoniu普通用户：
useradd xiaoniu
#为xiaoniu用户添加密码：
echo 123456 | passwd --stdin xiaoniu
#将xiaoniu添加到sudoers
echo "xiaoniu ALL = (root) NOPASSWD:ALL" | tee /etc/sudoers.d/xiaoniu
chmod 0440 /etc/sudoers.d/xiaoniu
#解决sudo: sorry, you must have a tty to run sudo问题，在/etc/sudoer注释掉 Default requiretty 一行
sudo sed -i 's/Defaults    requiretty/Defaults:xiaoniu !requiretty/' /etc/sudoers

#创建一个mongo目录
mkdir /mongo
#给相应的目录添加权限
chown -R xiaoniu:xiaoniu /mongo

#配置mongo的yum源
cat >> /etc/yum.repos.d/mongodb-org-3.4.repo << EOF
[mongodb-org-3.4]
name=MongoDB Repository
baseurl=https://repo.mongodb.org/yum/redhat/\$releasever/mongodb-org/3.4/x86_64/
gpgcheck=1
enabled=1
gpgkey=https://www.mongodb.org/static/pgp/server-3.4.asc
EOF

#关闭selinux
sed -i 's/SELINUX=enforcing/SELINUX=disabled/' /etc/selinux/config

#重新启动
reboot
------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------
#分别在多台机器上使用xiaoniu用户登录
sudo yum install -y mongodb-org

#继续角色信息
node-1        node-2          node-3

mongos        mongos          mongos        路由服务器，寻址
config        config          config        配置服务器，保存配置
shard1(主)    shard2（主）     shard3（主）   分片：保存数据
shard2        shard3          shard1   副本集：备份数据，可以配置读写分离（主负责写，从负责同步数据和读）
shard3        shard1          shard2

#分别在多台机器上创建mongo config server对应的目录
mkdir -p /mongo/config/{log,data,run}

#分别在多台机器上修改config server的配置文件
cat >> /mongo/config/mongod.conf << EOF
systemLog:
  destination: file
  logAppend: true
  path: /mongo/config/log/mongod.log
storage:
  dbPath: /mongo/config/data
  journal:
    enabled: true
processManagement:
  fork: true
  pidFilePath: /mongo/config/run/mongod.pid
net:
  port: 27100
replication:
  replSetName: config
sharding:
  clusterRole: configsvr
EOF

# clusterRole: configsvr这个配置是固定的


#【重要】启动所有的mongo config server服务
mongod --config /mongo/config/mongod.conf

#登录任意一台配置服务器，初始化配置副本集
mongo --port 27100

#创建配置
config = {
   _id : "config",
    members : [
        {_id : 0, host : "192.168.1.11:27100" },
        {_id : 1, host : "192.168.1.12:27100" },
        {_id : 2, host : "192.168.1.13:27100" }
    ]
}

#初始化副本集配置
rs.initiate(config)

#查看分区状态
rs.status()

#注意:其中，"_id" : "config"对应配置文件中配置的 replicaction.replSetName 一致，"members" 中的 "host" 为三个节点的ip和port

------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------

#配置第一个分片和副本集
#修改mongo shard1 server的配置文件
mkdir -p /mongo/shard1/{log,data,run}

#分别在多台机器上修改shard1 server的配置文件
cat >> /mongo/shard1/mongod.conf << EOF
systemLog:
  destination: file
  logAppend: true
  path: /mongo/shard1/log/mongod.log
storage:
  dbPath: /mongo/shard1/data
  journal:
    enabled: true
processManagement:
  fork: true
  pidFilePath: /mongo/shard1/run/mongod.pid
net:
  port: 27001
replication:
  replSetName: shard1
sharding:
  clusterRole: shardsvr
EOF

#启动所有的shard1 server
mongod --config /mongo/shard1/mongod.conf

#登陆任意一台shard1服务器(希望哪一台机器是主，就登录到那一台机器上)，初始化副本集
mongo --port 27001
#使用admin数据库
use admin
#定义副本集配置
config = {
   _id : "shard1",
    members : [
        {_id : 0, host : "192.168.1.11:27001" },
        {_id : 1, host : "192.168.1.12:27001" },
        {_id : 2, host : "192.168.1.13:27001" }
    ]
}
#初始化副本集配置
rs.initiate(config);

#查看分区状态
rs.status()

------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------

#配置第二个分片和副本集
#修改mongo shard2 server的配置文件
mkdir -p /mongo/shard2/{log,data,run}

#分别在多台机器上修改shard2 server的配置文件
cat >> /mongo/shard2/mongod.conf << EOF
systemLog:
  destination: file
  logAppend: true
  path: /mongo/shard2/log/mongod.log
storage:
  dbPath: /mongo/shard2/data
  journal:
    enabled: true
processManagement:
  fork: true
  pidFilePath: /mongo/shard2/run/mongod.pid
net:
  port: 27002
replication:
  replSetName: shard2
sharding:
  clusterRole: shardsvr
EOF

#启动所有的shard2 server
mongod --config /mongo/shard2/mongod.conf

#登陆(node2)的shard2服务器，初始化副本集
mongo --port 27002
#使用admin数据库
use admin
#定义副本集配置
config = {
   _id : "shard2",
    members : [
        {_id : 0, host : "192.168.1.12:27002" },
        {_id : 1, host : "192.168.1.13:27002" },
        {_id : 2, host : "192.168.1.11:27002" }
    ]
}
#初始化副本集配置
rs.initiate(config)

#查看分区状态
rs.status()

------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------

#配置第三个分片和副本集
#修改mongo shard3 server的配置文件
mkdir -p /mongo/shard3/{log,data,run}

#分别在多台机器上修改shard3 server的配置文件
cat >> /mongo/shard3/mongod.conf << EOF
systemLog:
  destination: file
  logAppend: true
  path: /mongo/shard3/log/mongod.log
storage:
  dbPath: /mongo/shard3/data
  journal:
    enabled: true
processManagement:
  fork: true
  pidFilePath: /mongo/shard3/run/mongod.pid
net:
  port: 27003
replication:
  replSetName: shard3
sharding:
  clusterRole: shardsvr
EOF

#启动所有的shard3 server
mongod --config /mongo/shard3/mongod.conf

#登陆node-1上的shard3服务器，初始化副本集
mongo --port 27003
#使用admin数据库
use admin
#定义副本集配置
config = {
   _id : "shard3",
    members : [
        {_id : 0, host : "192.168.1.13:27003" },
        {_id : 1, host : "192.168.1.11:27003" },
        {_id : 2, host : "192.168.1.12:27003" }
    ]
}
#初始化副本集配置
rs.initiate(config)

#查看分区状态
rs.status()

------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------


------------------------------------------------------------------------------------------------
######注意：启动mongos是守候进程是因为/mongo/mongos/mongod.conf缺少了fork: true这个配置#######
------------------------------------------------------------------------------------------------
mkdir -p /mongo/mongos/{log,data,run}

#添加mongs的配置文件
cat >> /mongo/mongos/mongod.conf << EOF
systemLog:
  destination: file
  logAppend: true
  path: /mongo/mongos/log/mongod.log
processManagement:
  fork: true
  pidFilePath: /mongo/mongos/run/mongod.pid
net:
  port: 27200
sharding:
  configDB: config/192.168.1.111:27100,192.168.1.12:27100,192.168.1.13:27100
EOF

#注意，这里configDB后面的config要与配置服务器的_id保持一致

#启动路由服务器
mongos --config /mongo/mongos/mongod.conf

#登录其中的一台路由节点，手动启用分片
mongo --port 27200

#添加分片到mongos
sh.addShard("shard1/192.168.1.11:27001,192.168.1.12:27001,192.168.1.13:27001")
sh.addShard("shard2/192.168.1.12:27002,192.168.1.13:27002,192.168.1.11:27002")
sh.addShard("shard3/192.168.1.13:27003,192.168.1.11:27003,192.168.1.12:27003")

#设置slave可读(在命令行中生效一次)，如果配置从接到可读，那么是连接客户端指定的
rs.slaveOk()

------------------------------------------------------------------------------------------------
####没有分片是因为没有开启分片规则####################
------------------------------------------------------------------------------------------------
#创建mobike数据库
use admin

#创建mobike数据库

#对bikes这个数据库开启分片功能
db.runCommand({"enablesharding":"mobike"}) 

#创建bikes集合
db.createCollection("bikes")

##对bike数据库下的users集合按id的hash进行分片
db.runCommand({"shardcollection":"mobike.bikes","key":{_id:'hashed'}})


#启动所有的config server
mongod --config /mongo/config/mongod.conf
#启动所有的shard1
mongod --config /mongo/shard1/mongod.conf
#启动所有的shard2
mongod --config /mongo/shard2/mongod.conf
#启动所有的shard3
mongod --config /mongo/shard3/mongod.conf
#启动所有的mongos
mongos --config /mongo/mongos/mongod.conf


#关闭服务
mongod --shutdown --dbpath /mongo/shard3/data
mongod --shutdown --dbpath /mongo/shard2/data
mongod --shutdown --dbpath /mongo/shard1/data
mongod --shutdown --dbpath /mongo/config/data
```

---
mycat集群搭建:

```bash
#首先在node-1、node-2、node-3上安装MySQL
#配置MySQL 5.7的yum源

sudo tee -a /etc/yum.repos.d/mysql-community.repo << EOF
[mysql57-community]
name=MySQL 5.7 Community Server
baseurl=http://repo.mysql.com/yum/mysql-5.7-community/el/6/\$basearch/
enabled=1
gpgcheck=0
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-mysql
EOF


#查看mysql源的信息
yum repolist enabled | grep mysql

#安装mysql的server
sudo yum install -y mysql-community-server

#启动mysql
sudo service mysqld start

#获取启动日志中的默认初始密码
#sudo grep 'temporary password' /var/log/mysqld.log

#获取密码并赋给一个变量
PASSWORD=`sudo grep 'temporary password' /var/log/mysqld.log | awk '{print $NF}'` 

#使用root用户登录
mysql -uroot -p$PASSWORD

#修改root用户的密码
ALTER USER 'root'@'localhost' IDENTIFIED BY 'XiaoNiu_123!';

#修改mysql远程登录权限
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'XiaoNiu_123!' WITH GRANT OPTION;
FLUSH PRIVILEGES;

----------------------------------------------------------------------------------------------------
----------------------------------------------------------------------------------------------------


#然后在node-4安装JDK并配置环境变量

#然后在node-4安装mycat
#上传Mycat-server-1.6.5-release-20171008170112-linux.tar.gz安装包


#修改conf目录下主要以下三个注意配置文件
	server.xml是Mycat服务器参数调整和用户授权的配置文件
	schema.xml是逻辑库定义和表以及分片定义的配置文件
	rule.xml是分片规则的配置文件

#修改server.xml(修改了mycat的用户和逻辑的database)

<user name="xiaoniu" defaultAccount="true">
        <property name="password">123456</property>
        <property name="schemas">bigdata</property>
</user>

<user name="user">
        <property name="password">user</property>
        <property name="schemas">bigdata</property>
        <property name="readOnly">true</property>
</user>


#修改schema.xml（配置逻辑库下的逻辑表，已经数据存放的mysql节点）
    <schema name="bigdata" checkSQLschema="false" sqlMaxLimit="100">
        <!-- auto sharding by id (long) -->
        <table name="travelrecord" dataNode="dn1,dn2,dn3" rule="auto-sharding-long" />

        <!-- global table is auto cloned to all defined data nodes ,so can join
            with any table whose sharding node is in the same data node -->
        <table name="company" primaryKey="ID" type="global" dataNode="dn1,dn2,dn3" />

        <!-- random sharding using mod sharind rule -->
        <table name="hotnews" primaryKey="ID" autoIncrement="true" dataNode="dn1,dn2,dn3"
               rule="mod-long" />
    </schema>

    <dataNode name="dn1" dataHost="node1" database="db1" />
    <dataNode name="dn2" dataHost="node2" database="db2" />
    <dataNode name="dn3" dataHost="node3" database="db3" />

    <dataHost name="node1" maxCon="1000" minCon="10" balance="0"
              writeType="0" dbType="mysql" dbDriver="native" switchType="1"  slaveThreshold="100">
        <heartbeat>select user()</heartbeat>
        <!-- can have multi write hosts -->
        <writeHost host="hostM1" url="192.168.10.101:3306" user="root" password="XiaoNiu_123!">
        </writeHost>
    </dataHost>

    <dataHost name="node2" maxCon="1000" minCon="10" balance="0"
              writeType="0" dbType="mysql" dbDriver="native" switchType="1"  slaveThreshold="100">
        <heartbeat>select user()</heartbeat>
        <!-- can have multi write hosts -->
        <writeHost host="hostM1" url="192.168.10.102:3306" user="root" password="XiaoNiu_123!">
        </writeHost>
    </dataHost>

    <dataHost name="node3" maxCon="1000" minCon="10" balance="0"
              writeType="0" dbType="mysql" dbDriver="native" switchType="1"  slaveThreshold="100">
        <heartbeat>select user()</heartbeat>
        <!-- can have multi write hosts -->
        <writeHost host="hostM1" url="192.168.10.103:3306" user="root" password="XiaoNiu_123!">
        </writeHost>
    </dataHost>

#在三台mysql上分别创建数据库db1、db2、db3
#然后在每一个database中创建表，有三张（travelrecord、company、hotnews）注意主键的名称

#在node-4上启动mycat
```

---
ngnix+keeplived搭建:

```bash
下载keepalived官网:http://keepalived.org

将keepalived解压到/usr/local/src目录下
tar -zxvf  keepalived-1.3.6.tar.gz -C /usr/local/src

进入到/usr/local/src/keepalived-1.3.6目录
cd /usr/local/src/keepalived-1.3.6

开始configure
./configure

#编译并安装
make && make install
拷贝执行文件
cp /usr/local/sbin/keepalived /usr/sbin/
将init.d文件拷贝到etc下,加入开机启动项
cp /usr/local/src/keepalived-1.3.6/keepalived/etc/init.d/keepalived /etc/init.d/keepalived
将keepalived文件拷贝到etc下
cp /usr/local/src/keepalived-1.3.6/keepalived/etc/sysconfig/keepalived /etc/sysconfig/ 
创建keepalived文件夹
mkdir -p /etc/keepalived
将keepalived配置文件拷贝到etc下
	cp /usr/local/src/keepalived-1.3.6/keepalived/etc/keepalived/keepalived.conf /etc/keepalived/keepalived.conf
添加可执行权限
chmod +x /etc/init.d/keepalived

添加keepalived到开机启动
chkconfig --add keepalived	
chkconfig keepalived on

----------------------------
	配置keepalived虚拟IP
#MASTER节点
global_defs {
}
vrrp_instance VI_1 {
    state MASTER
    interface eth0
    virtual_router_id 51
    priority 100
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        10.0.0.10/24
    }
}

#BACKUP节点
global_defs {
}
vrrp_instance VI_1 {
    state BACKUP
    interface eth0
    virtual_router_id 51
    priority 99
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }
    virtual_ipaddress {
        10.0.0.10/24
    }
}

#分别启动两台机器上的keepalived
service keepalived start
-----------------
	配置keepalived心跳检查
	#MASTER节点
global_defs {
}

vrrp_script chk_health {
    script "[[ `ps -ef | grep nginx | grep -v grep | wc -l` -ge 2 ]] && exit 0 || exit 1"
    interval 1
    weight -2
}

vrrp_instance VI_1 {
    state MASTER
    interface eth0
    virtual_router_id 1
    priority 100
    advert_int 2
    authentication {
        auth_type PASS
        auth_pass 1111
    }

    track_script {
        chk_health
    }

    virtual_ipaddress {
        10.0.0.10/24
    }

    notify_master "/usr/local/keepalived/sbin/notify.sh master"
    notify_backup "/usr/local/keepalived/sbin/notify.sh backup"
    notify_fault "/usr/local/keepalived/sbin/notify.sh fault"
}

#添加切换通知脚本
vi /usr/local/keepalived/sbin/notify.sh
#!/bin/bash

case "$1" in
    master)
        /usr/local/nginx/sbin/nginx
        exit 0
    ;;
backup)
        /usr/local/nginx/sbin/nginx -s stop
        /usr/local/nginx/sbin/nginx
        exit 0
    ;;
    fault)
        /usr/local/nginx/sbin/nginx -s stop
        exit 0
    ;;
    *)
        echo 'Usage: notify.sh {master|backup|fault}'
        exit 1
    ;;
esac

#添加执行权限
chmod +x /usr/local/keepalived/sbin/notify.sh
global_defs {
}

vrrp_script chk_health {
    script "[[ `ps -ef | grep nginx | grep -v grep | wc -l` -ge 2 ]] && exit 0 || exit 1"
    interval 1
    weight -2
}

vrrp_instance VI_1 {
    state BACKUP
    interface eth0
    virtual_router_id 1
    priority 99
    advert_int 1
    authentication {
        auth_type PASS
        auth_pass 1111
    }

    track_script {
        chk_health
    }

    virtual_ipaddress {
        10.0.0.10/24
    }
	
    notify_master "/usr/local/keepalived/sbin/notify.sh master"
    notify_backup "/usr/local/keepalived/sbin/notify.sh backup"
    notify_fault "/usr/local/keepalived/sbin/notify.sh fault"
}

#在第二台机器上添加notify.sh脚本
#分别在两台机器上启动keepalived
service keepalived start 
chkconfig keepalived on
```

---

elasticsearch搭建:

```bash
单机:
1.安装JDK（1.8）
2.上传解压Elasticsearch-5.4.3
3.创建一个普通用户，然后将对于的目录修改为普通用户的所属用户和所属组
4.修改配置文件config/elasticsearch.yml
	network.host: 192.168.100.211
5.启动ES，发现报错
	bin/elasticsearch
	#出现错误
	[1]: max file descriptors [4096] for elasticsearch process is too low, increase to at least [65536]
	[2]: max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]

	#用户最大可创建文件数太小
	sudo vi /etc/security/limits.conf
	* soft nofile 65536
	* hard nofile 65536

	#查看可打开文件数量
	ulimit -Hn


	#最大虚拟内存太小
	sudo vi /etc/sysctl.conf 
	vm.max_map_count=262144

	#查看虚拟内存的大小
	sudo sysctl -p

6重启linux
	shutdown -r now

7.通过浏览器访问ES
	192.168.100.211:9200

集群:
http://www.elastic.co/guide/en/elasticsearch/reference/current/setup-configuration.html
https://github.com/elastic/elasticsearch
###【在多台机器上执行下面的命令】###
#es启动时需要使用非root用户，所有创建一个xiaoniu用户：
useradd xiaoniu
#为hadoop用户添加密码：
echo 123456 | passwd --stdin xiaoniu
#将bigdata添加到sudoers
echo "xiaoniu ALL = (root) NOPASSWD:ALL" | tee /etc/sudoers.d/xiaoniu
chmod 0440 /etc/sudoers.d/xiaoniu
#解决sudo: sorry, you must have a tty to run sudo问题，在/etc/sudoer注释掉 Default requiretty 一行
sudo sed -i 's/Defaults    requiretty/Defaults:xiaoniu !requiretty/' /etc/sudoers

#创建一个bigdata目录
mkdir /{bigdata,data}
#给相应的目录添加权限
chown -R xiaoniu:xiaoniu /{bigdata,data}

-------------------------------------------------------------------------------------------------
1.安装jdk（jdk要求1.8.20以上）

2.上传es安装包

3.解压es
tar -zxvf elasticsearch-5.4.3.tar.gz -C /bigdata/

4.修改配置
vi /bigdata/elasticsearch-5.4.3/config/elasticsearch.yml
#集群名称，通过组播的方式通信，通过名称判断属于哪个集群
cluster.name: bigdata
#节点名称，要唯一
node.name: es-1
#数据存放位置
path.data: /data/es/data
#日志存放位置(可选)
path.logs: /data/es/logs
#es绑定的ip地址
network.host: 192.168.10.16
#初始化时可进行选举的节点
discovery.zen.ping.unicast.hosts: ["node-4", "node-5", "node-6"]


/bigdata/elasticsearch-5.4.3/bin/elasticsearch -d
-------------------------------------------------------------------------------------------------
#出现错误
[1]: max file descriptors [4096] for elasticsearch process is too low, increase to at least [65536]
[2]: max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]

#用户最大可创建文件数太小
sudo vi /etc/security/limits.conf
* soft nofile 65536
* hard nofile 65536

#查看可打开文件数量
ulimit -Hn


#最大虚拟内存太小
sudo vi /etc/sysctl.conf 
vm.max_map_count=262144

#查看虚拟内存的大小
sudo sysctl -p


5.使用scp拷贝到其他节点
scp -r elasticsearch-5.4.3/ node-5:$PWD
scp -r elasticsearch-5.4.3/ node-6:$PWD

6.在其他节点上修改es配置，需要修改的有node.name和network.host

7.启动es（/bigdata/elasticsearch-5.4.3/bin/elasticsearch -h查看帮助文档） 
/bigdata/elasticsearch-5.4.3/bin/elasticsearch -d


8.用浏览器访问es所在机器的9200端口
http://192.168.10.16:9200/
{
  "name" : "node-2",
  "cluster_name" : "bigdata",
  "cluster_uuid" : "v4AHbENYQ8-M3Aq8J5OZ5g",
  "version" : {
    "number" : "5.4.3",
    "build_hash" : "eed30a8",
    "build_date" : "2017-06-22T00:34:03.743Z",
    "build_snapshot" : false,
    "lucene_version" : "6.5.1"
  },
  "tagline" : "You Know, for Search"
}

kill `ps -ef | grep Elasticsearch | grep -v grep | awk '{print $2}'`

#查看集群状态
curl -XGET 'http://192.168.10.16:9200/_cluster/health?pretty'
http://192.168.10.16:9200/_cluster/health?pretty
------------------------------------------------------------------------------------------------------------------

RESTful接口URL的格式：
http://192.168.10.16:9200/<index>/<type>/[<id>]
其中index、type是必须提供的。
id是可选的，不提供es会自动生成。
index、type将信息进行分层，利于管理。
index可以理解为数据库；type理解为数据表；id相当于数据库表中记录的主键，是唯一的。


#向store索引中添加一些书籍
curl -XPUT 'http://192.168.10.16:9200/store/books/1' -d '{
  "title": "Elasticsearch: The Definitive Guide",
  "name" : {
    "first" : "Zachary",
    "last" : "Tong"
  },
  "publish_date":"2015-02-06",
  "price":"49.99"
}'

#在linux中通过curl的方式查询
curl -XGET 'http://192.168.10.18:9200/store/books/1'

#通过浏览器查询
http://192.168.10.18:9200/store/books/1


#在添加一个书的信息
curl -XPUT 'http://192.168.10.18:9200/store/books/2' -d '{
  "title": "Elasticsearch Blueprints",
  "name" : {
    "first" : "Vineeth",
    "last" : "Mohan"
  },
  "publish_date":"2015-06-06",
  "price":"35.99"
}'


# 通过ID获得文档信息
curl -XGET 'http://192.168.10.18:9200/store/books/1'

#在浏览器中查看
http://92.168.10.18:9200/store/books/1
```