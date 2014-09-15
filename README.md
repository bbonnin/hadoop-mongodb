Using SQL to query MongoDB with HIVE
====================================

This project aims to show how to use the [MongoDB connector for Hadoop](http://docs.mongodb.org/ecosystem/tools/hadoop/) in order to make SQL queries on data stored in MongoDB.

## Design
This is based on Hive : this tool needs Hadoop to work. The application works in a standalone mode with an embedded Hadoop cluster. 

* The first step is to use the Mini Cluster provided by Hadoop : it is a simple hadoop cluster where all the components are running in a single JVM (HDFS : namenode, datanode; YARN : resource manager, node manager).

* The second step is to start the HIVE components: a metastore and a server.

All these things can be easily done with [Spring for Apache Hadoop](http://projects.spring.io/spring-hadoop/).

In this example, there are two modes:
* Shell mode : you can write your queries in an interactive shell
![Shell mode](/docs/shell.png)
* Server mode : you can start the server part (Hadoop, Hive) and a client application will connect to this server using the JDBC driver
![JDBC mode](/docs/jdbc.png)

## Configuration

See config files in src/main/resources.
* hadoop-hive.xml : Hadoop cluster configuration
* hive-shell.xml : Configuration of the interactive shell
* jdbc-client.xml : JDBC configuration

## Compilation

``` bash
    mvn clean compile
```

## Run

* Start MongoDB

* Start the Hive shell (it includes an embedded Hadoop cluster):
``` bash
echo "Start the hive shell with the following arguments : $*"
rm -f hadoop_mongodb.log
mvn exec:exec -Dexec.arguments="$*" -Phiveshell
```

``` bash
Start the hive shell with the following arguments :
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building hadoop-mongodb 0.0.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO]
[INFO] --- exec-maven-plugin:1.3.2:exec (default-cli) @ hadoop-mongodb ---
Formatting using clusterid: testClusterID
+---------------------------+
| Welcome to the Hive Shell |
+---------------------------+
>
>
```


Now, you can query your MongoDB !

## Queries

* Create tables :
``` SQL
>  create table jobs (id string, name string, job string) stored by 'com.mongodb.hadoop.hive.MongoStorageHandler' WITH SERDEPROPERTIES('mongo.columns.mapping'='{"id":"_id"}') TBLPROPERTIES('mongo.uri'='mongodb://localhost:27017/test.jobs');

OK

> create table individuals (id struct<oid:string, bsontype:int>, name string, age int, address struct<zipcode:int, city:string>) stored by 'com.mongodb.hadoop.hive.MongoStorageHandler' WITH SERDEPROPERTIES('mongo.columns.mapping'='{"id":"_id"}') TBLPROPERTIES('mongo.uri'='mongodb://localhost:27017/test.individuals', 'columns.comments'='');

OK
```

* Get table description:
``` SQL
 > describe extended individuals;
OK
id                      struct<oid:string,bsontype:int> from deserializer
name                    string                  from deserializer
age                     int                     from deserializer
address                 struct<zipcode:int,city:string> from deserializer

Detailed Table Information      Table(tableName:individuals, dbName:default, owner:cdnhdckk, createTime:1409238198, lastAccessTime:0, retention:0, sd:StorageDescriptor(cols:[FieldSchema(name:id, type:struct<oid:string,bsontype:int>, comment:null), FieldSchema(name:name, type:string, comment:null), FieldSchema(name:age, type:int, comment:null), FieldSchema(name:address, type:struct<zipcode:int,city:string>, comment:null)], location:file:/tmp/individuals, inputFormat:com.mongodb.hadoop.hive.input.HiveMongoInputFormat, outputFormat:com.mongodb.hadoop.hive.output.HiveMongoOutputFormat, compressed:false, numBuckets:-1, serdeInfo:SerDeInfo(name:null, serializationLib:com.mongodb.hadoop.hive.BSONSerDe, parameters:{serialization.format=1, mongo.columns.mapping={"id":"_id"}}), bucketCols:[], sortCols:[], parameters:{}, skewedInfo:SkewedInfo(skewedColNames:[], skewedColValues:[], skewedColValueLocationMaps:{}), storedAsSubDirectories:false), partitionKeys:[], parameters:{numFiles=0, mongo.uri=mongodb://localhost:27017/test.individuals, columns.comments=, transient_lastDdlTime=1409238198, COLUMN_STATS_ACCURATE=false, totalSize=0, numRows=-1, storage_handler=com.mongodb.hadoop.hive.MongoStorageHandler, rawDataSize=-1}, viewOriginalText:null, viewExpandedText:null, tableType:MANAGED_TABLE)
```


* Basic "select":
``` SQL
> select * from individuals;
OK
{"oid":"53f4a478e4b0db0f5dbba307","bsontype":8} Bob     34      {"zipcode":12345,"city":"ICI"}
{"oid":"53f4a478e4b0db0f5dbba308","bsontype":8} Bobette 33      {"zipcode":54321,"city":"LA-BAS"}
```

* Count : Hive builds a Map/Reduce job for this kind of query

``` SQL
 > select count(*) from individuals;
Total jobs = 1
Launching Job 1 out of 1
Number of reduce tasks determined at compile time: 1
In order to change the average load for a reducer (in bytes):
  set hive.exec.reducers.bytes.per.reducer=<number>
In order to limit the maximum number of reducers:
  set hive.exec.reducers.max=<number>
In order to set a constant number of reducers:
  set mapreduce.job.reduces=<number>
Starting Job = job_1409311417015_0001, Tracking URL = http://localhost:27800/proxy/application_1409311417015_0001/
Kill Command = /usr/bin/hadoop job  -kill job_1409311417015_0001
Hadoop job information for Stage-1: number of mappers: 1; number of reducers: 1
2014-08-29 13:28:54,017 Stage-1 map = 0%,  reduce = 0%
2014-08-29 13:29:01,791 Stage-1 map = 100%,  reduce = 0%, Cumulative CPU 1.78 sec
2014-08-29 13:29:10,283 Stage-1 map = 100%,  reduce = 100%, Cumulative CPU 3.54 sec
MapReduce Total cumulative CPU time: 3 seconds 540 msec
Ended Job = job_1409311417015_0001
MapReduce Jobs Launched:
Job 0: Map: 1  Reduce: 1   Cumulative CPU: 3.54 sec   HDFS Read: 363 HDFS Write: 2 SUCCESS
Total MapReduce CPU Time Spent: 3 seconds 540 msec
OK
2
```

* Join : in this case, Hive also builds a Map/Reduce job

``` SQL
 > select i.name, j.job from individuals i, jobs j where i.name=j.name;
Total jobs = 1
Launching Job 1 out of 1
Number of reduce tasks is set to 0 since there's no reduce operator
Starting Job = job_1409311417015_0002, Tracking URL = http://localhost:27800/proxy/application_1409311417015_0002/
Kill Command = /usr/bin/hadoop job  -kill job_1409311417015_0002
Hadoop job information for Stage-3: number of mappers: 1; number of reducers: 0
2014-08-29 13:32:33,709 Stage-3 map = 0%,  reduce = 0%
2014-08-29 13:32:42,354 Stage-3 map = 100%,  reduce = 0%, Cumulative CPU 2.71 sec
MapReduce Total cumulative CPU time: 2 seconds 710 msec
Ended Job = job_1409311417015_0002
MapReduce Jobs Launched:
Job 0: Map: 1   Cumulative CPU: 2.71 sec   HDFS Read: 349 HDFS Write: 39 SUCCESS
Total MapReduce CPU Time Spent: 2 seconds 710 msec
OK
Bob     CTO
Bobette UNKNOWN
Bobette DRIVER

```

* Load data : in order to load data in non-native table, you have to create an temporary table
  * First step : create a temporary table
``` SQL
create table src_individuals (name string, age int, zipcode int, city string) row format delimited fields terminated by ',';
```

  * Second step : load the data in the temporary table
``` SQL
> load data local inpath 'file:///tmp/data.csv' into table src_individuals;
Copying data from file:/tmp/data.csv
Copying file: file:/tmp/data.csv
Loading data to table default.src_individuals
Table default.src_individuals stats: [numFiles=2, numRows=0, totalSize=82, rawDataSize=0]
OK
```

  * Last step : insert the data in the collection (it uses a User-Defined Function in order to create an ObjectID instance). Again, for the insert, Hive generates a Map/Reduce job.
``` SQL
> create temporary function newObjectId as 'hadoopmongo.hive.UDFObjectId';
OK

> insert into table individuals select newObjectId() as id, name, age, named_struct('zipcode', zipcode, 'city', city) as address from src_individuals;
Total jobs = 1
Launching Job 1 out of 1
Number of reduce tasks is set to 0 since there's no reduce operator
Starting Job = job_1409311417015_0003, Tracking URL = http://localhost:27800/proxy/application_1409311417015_0003/
Kill Command = /usr/bin/hadoop job  -kill job_1409311417015_0003
Hadoop job information for Stage-0: number of mappers: 1; number of reducers: 0
2014-08-29 13:44:30,593 Stage-0 map = 0%,  reduce = 0%
2014-08-29 13:44:39,147 Stage-0 map = 100%,  reduce = 0%, Cumulative CPU 1.91 sec
MapReduce Total cumulative CPU time: 1 seconds 910 msec
Ended Job = job_1409311417015_0003
MapReduce Jobs Launched:
Job 0: Map: 1   Cumulative CPU: 1.91 sec   HDFS Read: 246 HDFS Write: 0 SUCCESS
Total MapReduce CPU Time Spent: 1 seconds 910 msec
OK

```