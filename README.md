## Log File Analyzer

### Author: Venkata Sesha Phani, Vakicherla
### Email: phani.vakicherla@gmail.com

### Introduction
The goal of this project is to gain experience with solving a distributed computational problem using cloud computing technologies.
I have built a solution to analyze huge log files and solved a set of use cases using big data techniques such as hadoop Map Reduce and deployed this application in AMAZON EMR cluster.

Video Link : https://www.youtube.com/watch?v=HIEiFrPHoSE

The video explains deployment of hadoop application in AWS EMR Cluster

### Environment

```
OS: Ubuntu

IDE: IntelliJ IDEA 2022.2.3 (Ultimate Edition)

SCALA Version: 3.2.0

SBT Version: 1.7.1

Hadoop Version: 3.3.4
```

### Running the test file

Test Files can be found under the directory /src/test/scala/LogAnalyzerTests
I have used mockito and scalatestplus libraries and mocked the context to test the mapper functions

````
sbt clean compile test
````

### Running the project

1) Clone this repository

```
repo https://github.com/SeshaPhaniVV/LogAnalyzer
```
```
cd LogFileAnalyzer
```

2) Open the project in intelliJ

```
set up scala code formatter with the configuration file set as `.scalafmt.conf` 
https://www.jetbrains.com/help/idea/work-with-scala-formatter.html
```

3) Generate jar file by running the following command in the terminal from project root folder. Jar file is created in target/scala-3.2.0/LogFileGenerator-assembly-0.1.jar

````
sbt clean compile assembly
````

4) Running project via SBT Run. Please delete the `output` folder in project root before you run the below command.

```
sbt run
```

### Running the jobs
1) Setup hadoop for ubuntu following the guide https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html

2) Once hadoop is set up go to the installation directory and start the hadoop processes.

```
cd /usr/local/hadoop/sbin
./start-all.sh
./mr-jobhistory-daemon.sh start historyserver
```

3) Once both dfs, yarn and job history processes are started - check them using `jps` command. Output should contain below info
````
DataNode
NameNode
SecondaryNameNode
JobHistoryServer
NodeManager
ResourceManager
````

4) Once we started the hadoop processes move the input files to hadoop filesystem. From LogAnalyzer directory in terminal Execute the below commands
````
hdfs dfs -rm -r log // Removes the existing log files
hdfs dfs -rm -r output // Removes existing output directory
hadoop fs -mkdir -p log // Creates log difrectory in the root folder
hdfs dfs -put log/LogFileGenerator.2022-09-*.log log // copies log files from local to the hdfs file system
````

5) Run Map-Reduce Tasks

   hadoop jar </nameOfJarFile/>

   nameOfJarFile : LogFileGenerator-assembly-0.1.jar

````
hadoop jar target/scala-3.2.0/LogFileGenerator-assembly-0.1.jar
````

6) To view the output use the following commands

   hdfs dfs -get </OutputDirectory/> </outputPath/>

   OutputPath : The path to copy the output

``
hdfs dfs -get output logAnalyzerOutput // copies the output into your local
``

Now output is copied to logAnalyzerOutput from hdfs output directory

7) Generating .csv output file

   7.1) Move to output path in which the output is copied and ls to see the output of each task

              cd logAnalyzerOutput

   7.2) To generate .csv output file

   ````
   hadoop fs -cat output/task1/part-r-00000 > task1/Task1.csv  
   hadoop fs -cat output/task2/part-r-00000 > task2/Task2.csv  
   hadoop fs -cat output/task3/part-r-00000 > task3/Task3.csv  
   hadoop fs -cat output/task4/part-r-00000 > task4/Task4.csv
   ````

Now, logAnalyzerOutput will have all the output files in both normal and .csv format


### File Structure

1) Input files(LogFiles) : /src/main/resources/LogFileGenerator.2022-09-*.log
2) Application Configuration : /src/main/resources/application.conf
3) Tasks - /src/main/scala/LogAnalyzerTasks

   3.1) Task1 - /src/main/scala/LogAnalyzerTasks/Task1 

   3.2) Task2 - /src/main/scala/LogAnalyzerTasks/Task2
   
   3.3) Task3 - /src/main/scala/LogAnalyzerTasks/Task3
   
   3.4) Task4 - /src/main/scala/LogAnalyzerTasks/Task4

   3.5) LogAnalyzer - /src/main/scala/LogAnalyzer : This is the main map reduce class that runs all tasks

4) LogAnalyzerTest - /src/test/scala/LogAnalyzerTests/LogAnalyzerTest
5) Jar File - /target/scala-3.2.0/LogFileGenerator-assembly-0.1.jar : Generated Jar file

### Map Reduce Task Details

This project solves total of 4 tasks. They are explained below:

**Mapper:** Mapper maps input key/value pairs to a set of intermediate key/value pairs.

Maps are the individual tasks that transform input records into intermediate records. The transformed intermediate records do not need to be of the same type as the input records. A given input pair may map to zero or many output pairs.

**Combiner:** Before the shuffle/sort phase, the Combiner optimizes all intermediate outputs using local aggregation. Combiners' main purpose is to reduce bandwidth by reducing the number of key/value pairs that must be shuffled across the network and delivered as input to the Reducer.

**Partitioner:** Partitioner controls the partitioning of the keys of the intermediate map output in Hadoop. To determine partition, the hash function is employed. Each map output is partitioned based on the key-value pair. Each partition (inside each mapper) contains records with the same key value, and each partition is subsequently forwarded to a Reducer. Partition phase takes place in between mapper and reducer.
The Hash Partitioner (Default Partitioner) computes a hash value for the key and assigns the partition based on it.

**Reducer:** In Hadoop MapReduce, a reducer condenses a set of intermediate values that share a key into a smaller set. Reducer takes a set of intermediate key-value pairs produced by the mapper as input in the MapReduce job execution flow. Reducer then aggregates, filters, and combines key-value pairs, which necessitates extensive processing.

Firstly, the regex pattern is checked across the log entry for all four tasks. If it succeeds, it moves on to the job computation.
Note : regex pattern is mentioned in the application.conf

**1) Task 1:** To find the count of generated log messages with injected regex pattern for each message type in a predefined time interval.
```
A predefined time interval is mentioned in the application.conf. Using this start and end time, the log messages with that injected regex pattern for every log message tag is counted.(predefined time interval - startTime and endTime are configured in `application.conf`)
Task1Mapper: (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]), (value -> 1)
Task1Reducer : (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]) , (value -> sum of the logMessage count)
```

**2) Task 2:** To display the time intervals sorted in the descending order that contained most log messages of the type ERROR with injected regex pattern string instances.
```
Every one hour is chosen as the time interval here. So, for every hour the ERROR tag injected regex pattern log messages is counted and displayed in descending order based on the count.
Task2 has two mapper and reducer. The first mapper and reducer provides the aggregated count of log message. The second mapper and reducer is used for sorting. Task2Reducer1 output is passed to Task2Mapper2 and Task2Mapper2 sends the (key - count of log messages * -1,value - the time) to Task2reducer2 where the sorting is done based on the key.
Task2Mapper1: (Key, Value) (key -> Hour - [1..24]), (value -> 1)
Task2Reducer1 : (Key, Value) (key -> Hour - [1..24]) , (value -> sum of the Error tag logMessage count)
Task2Mapper2: (Key, Value) (key -> sum of the Error tag logMessage count * -1 ), (value -> hour [1..24])
In Task2Reducer2, the sorted is done based on the key value sent by the Task2Mapper2
Task2Reducer2 : (Key, Value) (key -> Hour - [1..24]) , (value -> sum of the Error tag logMessage count)
```

**3) Task 3:** To find the count of generated log messages for each message type.
```
The log messages for every log message tag is counted
Task3Mapper: (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]), (value -> 1)
Task3Reducer : (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]) , (value -> sum of the logMessage count)
```

**4) Task 4:** To produce the number of characters in each log message for each log message type that contain the highest number of characters in the detected instances of the designated regex pattern.
```
The max length of every injected regex pattern log message for every log message is displayed
Task4Mapper: (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]), (value -> logMessageLength)
Task4Reducer : (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]) , (value -> max of the logMessageLength)
```
### Output
The output of every task can be located under output folder
```
task1/task2/task3/task4: Final output for each task is stored in the individual folders
task2Intermediate : This is the intermediate output of task2 before sorting.
```
