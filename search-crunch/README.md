# Cloudera Search - Crunch Indexer Tool

## Building

This step builds the software from source. It also runs the unit tests.

```bash
git clone http://github.mtv.cloudera.com/CDH/search
cd search
#git tag # list available releases
#git checkout master
git checkout cdh5-1.0.0 # or whatever the latest version is
mvn clean package -pl search-crunch # This can take several minutes
cd search-crunch
find target -name '*.jar'
```

## Getting Started

The steps below assume you have already built the software as described above.
In addition, below we assume a working MapReduce or Spark cluster, for example as installed by Cloudera Manager.

## CrunchIndexerTool

`CrunchIndexerTool` is a Spark or MapReduce ETL batch job that pipes data from (splittable or non-splittable) HDFS files into Apache  Solr,  and  along the way runs the data through a Morphline  for extraction  and transformation. The program is
designed for flexible, scalable and fault-tolerant batch ETL pipeline jobs. It is implemented as an  Apache  Crunch  pipeline and as such can run
on either the Apache Hadoop MapReduce or Apache Spark execution engine. More details are available through the command line help:

<pre>
$export HADOOP_CLASSPATH=$myDependencyJarPaths; hadoop jar $myDriverJar org.apache.solr.crunch.CrunchIndexerTool -help

MapReduceUsage: export HADOOP_CLASSPATH=$myDependencyJarPaths; hadoop jar $myDriverJar 
org.apache.solr.crunch.CrunchIndexerTool --libjars $myDependencyJarFiles [MapReduceGenericOptions]...
        [--input-file-list URI] [--input-file-format FQCN] [--input-file-projection-schema FILE]
        [--input-file-reader-schema FILE] --morphline-file FILE [--morphline-id STRING]
        [--pipeline-type STRING] [--xhelp] [--mappers INTEGER] [--dry-run] [--log4j FILE] [--chatty]
        [HDFS_URI [HDFS_URI ...]]

SparkUsage: spark-submit [SparkGenericOptions]... --master local|yarn --deploy-mode client|cluster
--jars $myDependencyJarFiles --class org.apache.solr.crunch.CrunchIndexerTool $myDriverJar
        [--input-file-list URI] [--input-file-format FQCN] [--input-file-projection-schema FILE]
        [--input-file-reader-schema FILE] --morphline-file FILE [--morphline-id STRING]
        [--pipeline-type STRING] [--xhelp] [--mappers INTEGER] [--dry-run] [--log4j FILE] [--chatty]
        [HDFS_URI [HDFS_URI ...]]

Spark or MapReduce ETL batch job that pipes  data  from  (splittable or non-splittable) HDFS files into Apache
Solr, and along the way runs the data  through  a  Morphline for extraction and transformation. The program is
designed for flexible, scalable and fault-tolerant batch  ETL  pipeline  jobs.  It is implemented as an Apache
Crunch pipeline and as such can run on either the Apache Hadoop MapReduce or Apache Spark execution engine.

The program proceeds in several consecutive phases, as follows: 

1) Randomization phase: This (parallel) phase  randomizes  the  list  of  HDFS  input files in order to spread
ingestion load more evenly among the mapper tasks  of  the  subsequent  phase. This phase is only executed for
non-splittables files, and skipped otherwise.

2) Extraction phase: This (parallel) phase  emits  a  series  of  HDFS  file input streams (for non-splittable
files) or a series of input data records (for splittable files). 

3) Morphline phase: This (parallel) phase receives the  items  of  the previous phase, and uses a Morphline to
extract the relevant content, transform it and load  zero  or  more documents into Solr. The ETL functionality
is flexible and  customizable  using  chains  of  arbitrary  morphline  commands  that  pipe  records from one
transformation command to another. Commands to parse  and  transform  a  set  of standard data formats such as
Avro, Parquet, CSV, Text, HTML, XML, PDF, MS-Office, etc.  are  provided out of the box, and additional custom
commands and parsers for additional file or data formats  can  be added as custom morphline commands. Any kind
of data format can be processed and  any  kind  output  format  can  be  generated by any custom Morphline ETL
logic. Also, this phase can be used  to  send  data  directly  to  a  live SolrCloud cluster (via the loadSolr
morphline command).

The program is implemented as a Crunch  pipeline  and  as  such  Crunch optimizes the logical phases mentioned
above into an efficient physical execution plan that  runs  a  single mapper-only job, or as the corresponding
Spark equivalent.

Fault Tolerance: Task attempts are retried on failure  per  the  standard MapReduce or Spark semantics. If the
whole job fails you can retry simply by rerunning the program again using the same arguments.

Comparison with MapReduceIndexerTool: 

1) CrunchIndexerTool can also run on the Spark execution engine, not just on MapReduce. 
2) CrunchIndexerTool enables interactive low latency prototyping, in particular in Spark 'local' mode. 
3) CrunchIndexerTool supports updates (and deletes) of existing documents in Solr, not just inserts. 
4) CrunchIndexerTool can exploit data locality for splittable Hadoop files (text, avro, avroParquet). 
We recommend MapReduceIndexerTool for large scale  batch  ingestion  use  cases  where updates (or deletes) of
existing documents in Solr are not required, and we recommend CrunchIndexerTool for all other use cases.

CrunchIndexerOptions:
  HDFS_URI               HDFS URI of file or directory tree to ingest. (default: [])
  --input-file-list URI, --input-list URI
                         Local URI or HDFS URI of  a  UTF-8  encoded  file  containing  a list of HDFS URIs to
                         ingest, one URI per line in the  file.  If  '-'  is specified, URIs are read from the
                         standard input. Multiple --input-file-list arguments can be specified.
  --input-file-format FQCN
                         The Hadoop FileInputFormat to use  for  extracting  data  from splittable HDFS files.
                         Can be a fully qualified Java class  name  or one of ['text', 'avro', 'avroParquet'].
                         If this option is present  the  extraction  phase  will  emit  a series of input data
                         records rather than a series of HDFS file input streams.
  --input-file-projection-schema FILE
                         Relative or absolute path to an Avro schema  file on the local file system. This will
                         be used as the projection schema for Parquet input files.
  --input-file-reader-schema FILE
                         Relative or absolute path to an Avro schema  file on the local file system. This will
                         be  used  as  the  reader  schema   for   Avro   or  Parquet  input  files.  Example:
                         src/test/resources/test-documents/strings.avsc
  --morphline-file FILE  Relative or  absolute  path  to  a  local  config  file  that  contains  one  or more
                         morphlines. The file must be UTF-8 encoded. It  will be uploaded to each remote task.
                         Example: /path/to/morphline.conf
  --morphline-id STRING  The identifier of the morphline that  shall  be  executed within the morphline config
                         file specified by  --morphline-file.  If  the  --morphline-id  option  is omitted the
                         first (i.e. top-most) morphline within the config file is used. Example: morphline1
  --pipeline-type STRING
                         The engine to use for executing  the  job.  Can  be 'mapreduce' or 'spark'. (default:
                         mapreduce)
  --xhelp, --help, -help
                         Show this help message and exit
  --mappers INTEGER      Tuning knob that  indicates  the  maximum  number  of  MR  mapper  tasks  to  use. -1
                         indicates use all map slots available on  the cluster. This parameter only applies to
                         non-splittable input files (default: -1)
  --dry-run              Run the pipeline but print documents  to  stdout  instead  of loading them into Solr.
                         This can be  used  for  quicker  turnaround  during  early  trial  &  debug sessions.
                         (default: false)
  --log4j FILE           Relative or absolute  path  to  a  log4j.properties  config  file  on  the local file
                         system. This file will  be  uploaded  to  each  remote task. Example: /path/to/log4j.
                         properties
  --chatty               Turn on verbose output. (default: false)

SparkGenericOptions:     To print all options run 'spark-submit --help'

MapReduceGenericOptions: Generic options supported are
  --conf &lt;configuration file&gt;
                         specify an application configuration file
  -D &lt;property=value&gt;    use value for given property
  --fs &lt;local|namenode:port&gt;
                         specify a namenode
  --jt &lt;local|jobtracker:port&gt;
                         specify a job tracker
  --files &lt;comma separated list of files&gt;
                         specify comma separated files to be copied to the map reduce cluster
  --libjars &lt;comma separated list of jars&gt;
                         specify comma separated jar files to include in the classpath.
  --archives &lt;comma separated list of archives&gt;
                         specify comma separated  archives  to  be  unarchived  on  the compute
                         machines.

The general command line syntax is
bin/hadoop command [genericOptions] [commandOptions]

Examples: 

# Prepare - Copy input files into HDFS:
export myResourcesDir=src/test/resources # for build from git
export myResourcesDir=/opt/cloudera/parcels/CDH/share/doc/search-*/search-crunch # for CDH with parcels
export myResourcesDir=/usr/share/doc/search-*/search-crunch # for CDH with packages
hadoop fs -copyFromLocal $myResourcesDir/test-documents/hello1.txt hdfs:/user/systest/input/

# Prepare variables for convenient reuse:
export myDriverJarDir=target # for build from git
export myDriverJarDir=/opt/cloudera/parcels/CDH/lib/solr/contrib/crunch # for CDH with parcels
export myDriverJarDir=/usr/lib/solr/contrib/crunch # for CDH with packages
export myDependencyJarDir=target/lib # for build from git
export myDependencyJarDir=/opt/cloudera/parcels/CDH/lib/search/lib/search-crunch # for CDH with parcels
export myDependencyJarDir=/usr/lib/search/lib/search-crunch # for CDH with packages
export myDriverJar=$(find $myDriverJarDir -maxdepth 1 -name 'search-crunch-*.jar' ! -name '*-job.jar' ! -name '*-sources.jar')
export myDependencyJarFiles=$(find $myDependencyJarDir -name '*.jar' | sort | tr '\n' ',' | head -c -1)
export myDependencyJarPaths=$(find $myDependencyJarDir -name '*.jar' | sort | tr '\n' ':' | head -c -1)
export myJVMOptions="-DmaxConnectionsPerHost=10000 -DmaxConnections=10000" # for solrj 

# MapReduce on Yarn - Ingest text file line by line into Solr:
export HADOOP_CLIENT_OPTS="$myJVMOptions"; export HADOOP_CLASSPATH=$myDependencyJarPaths; hadoop \
  --config /etc/hadoop/conf.cloudera.YARN-1 \
  jar $myDriverJar org.apache.solr.crunch.CrunchIndexerTool \
  --libjars $myDependencyJarFiles \
  -D mapreduce.map.java.opts="-Xmx500m $myJVMOptions" \
  -D morphlineVariable.ZK_HOST=$(hostname):2181/solr \
  --files $myResourcesDir/test-documents/string.avsc \
  --morphline-file $myResourcesDir/test-morphlines/loadSolrLine.conf \
  --pipeline-type mapreduce \
  --chatty \
  --log4j $myResourcesDir/log4j.properties \
  /user/systest/input/hello1.txt

# Spark in Local Mode (for rapid prototyping) - Ingest into Solr:
spark-submit \
  --master local \
  --deploy-mode client \
  --jars $myDependencyJarFiles \
  --executor-memory 500M \
  --conf "spark.executor.extraJavaOptions=$myJVMOptions" \
  --driver-java-options "$myJVMOptions" \
  # --driver-library-path /opt/cloudera/parcels/CDH/lib/hadoop/lib/native # for Snappy on CDH with parcels\
  # --driver-library-path /usr/lib/hadoop/lib/native # for Snappy on CDH with packages \
  --class org.apache.solr.crunch.CrunchIndexerTool \
  $myDriverJar \
  -D morphlineVariable.ZK_HOST=$(hostname):2181/solr \
  --morphline-file $myResourcesDir/test-morphlines/loadSolrLine.conf \
  --pipeline-type spark \
  --chatty \
  --log4j $myResourcesDir/log4j.properties \
  /user/systest/input/hello1.txt

# Spark on Yarn in Client Mode (for testing) - Ingest into Solr:
Same as above, except replace '--master local' with '--master yarn'

# View the yarn executor log files (there is no GUI yet):
yarn logs --applicationId $application_XYZ

# Spark on Yarn in Cluster Mode (for production) - Ingest into Solr:
spark-submit \
  --master yarn \
  --deploy-mode cluster \
  --jars $myDependencyJarFiles \
  --executor-memory 500M \
  --conf "spark.executor.extraJavaOptions=$myJVMOptions" \
  --driver-java-options "$myJVMOptions" \
  --class org.apache.solr.crunch.CrunchIndexerTool \
  --files $(ls $myResourcesDir/log4j.properties),$(ls $myResourcesDir/test-morphlines/loadSolrLine.conf)\
  $myDriverJar \
  -D hadoop.tmp.dir=/tmp \
  -D morphlineVariable.ZK_HOST=$(hostname):2181/solr \
  --morphline-file loadSolrLine.conf \
  --pipeline-type spark \
  --chatty \
  --log4j log4j.properties \
  /user/systest/input/hello1.txt

# Spark on Yarn in Cluster Mode (for production) - Ingest into Secure (Kerberos-enabled) Solr:
# Spark requires two additional steps compared to non-secure solr:
# (NOTE: MapReduce does not require extra steps for communicating with kerberos-enabled Solr)
# 1) Create a delegation token file
#    a) kinit as the user who will make solr requests
#    b) request a delegation token from solr and save it to a file:
#       e.g. using curl:
#       "curl --negotiate -u: http://solr-host:port/solr/?op=GETDELEGATIONTOKEN > tokenFile.txt"
# 2) Pass the delegation token file to spark-submit:
#    a) add the delegation token file via --files
#    b) pass the file name via -D tokenFile
#       spark places this file in the cwd of the executor, so only list the file name, no path
  spark-submit \
  --master yarn \
  --deploy-mode cluster \
  --jars $myDependencyJarFiles \
  --executor-memory 500M \
  --conf "spark.executor.extraJavaOptions=$myJVMOptions" \
  --driver-java-options "$myJVMOptions" \
  --class org.apache.solr.crunch.CrunchIndexerTool \
  --files $(ls $myResourcesDir/log4j.properties),$(ls $myResourcesDir/test-morphlines/loadSolrLine.conf),tokenFile.txt \
  $myDriverJar \
  -D hadoop.tmp.dir=/tmp \
  -D morphlineVariable.ZK_HOST=$(hostname):2181/solr \
  -D tokenFile=tokenFile.txt \
  --morphline-file loadSolrLine.conf \
  --pipeline-type spark \
  --chatty \
  --log4j log4j.properties \
  /user/systest/input/hello1.txt
</pre>
  