## CS441 - Engineering Distributed Objects for Cloud Computing

## HomeWork 1 - Log File Analyzer

### Author: Venkata Sesha Phani, Vakicherla
### Email: vvakic2@uic.edu
### UIN: 651416734

### Introduction
The goal of this project is to gain experience with solving a distributed computational problem using cloud computing technologies.

Video Link : <TODO>

The video explains deployment of hadoop application in AWS EMR Instance

### Environment

```
OS: Ubuntu

IDE: IntelliJ IDEA 2022.2.3 (Ultimate Edition)

SCALA Version: 3.2.0

SBT Version: 1.7.1

Hadoop Version: 3.3.4
```

### Running the test file

Test Files can be found under the directory /src/test/scala/*

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

3) Generate jar file by running the following command in the terminal. Jar file is created in target/scala-3.2.0/LogFileGenerator-assembly-0.1.jar

````
sbt clean compile assembly
````

### Running the jobs
1) Setup hadoop for ubuntu using the guide https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-common/SingleCluster.html

2) Once hadoop is set up go to the installation directory and start the hadoop processes.

```
cd /usr/local/hadoop/sbin
./start-all.sh
```

3) Once both dfs and yarn processes are started - check them using `jps` command. Output should contain below info
````
DataNode
NameNode
SecondaryNameNode
NodeManager
ResourceManager
````

4) Once we started the hadoop processes move the input files to hadoop filesystem. From LogAnalyzer directory in terminal Execute the below commands
````
hdfs dfs -rm -r log // Removes the existing log files
hdfs dfs -rm -r output // 
hadoop fs -mkdir -p log
hdfs dfs -put log/LogFileGenerator.2022-09-*.log log
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
hdfs dfs -get output logAnalyzerOutput
``

Now output is copied to logAnalyzerOutput from hdfs output directory

7) Generating .csv output file

   7.1) Move to output path in which the output is copied and ls to see the output of each task

              cd logAnalyzerOutput

   7.2) To generate .csv output file

   ````
     hadoop fs -cat output/task1/* > Task1.csv  
     hadoop fs -cat output/task2/* > Task2.csv  
     hadoop fs -cat output/task3/* > Task3.csv  
     hadoop fs -cat output/task4/* > Task4.csv   
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

   3.5) MapperReducerTasks - /src/main/scala/LogAnalyzer : This is the main map reduce class that runs all tasks

4) LogAnalyzerTest - /src/test/scala/LogAnalyzerTests/LogAnalyzerTest
5) Jar File - /target/scala-3.2.0/LogFileGenerator-assembly-0.1.jar : Generated Jar files


### Map Reduce Tasks Implementation

There are totally 4 tasks created in this homework. They are clearly listed below

**Mapper:** The input data is first processed by all Mappers/Map tasks, and then the intermediate output is generated.

**Combiner:** Before the shuffle/sort phase, the Combiner optimizes all intermediate outputs using local aggregation. Combiners' main purpose is to reduce bandwidth by reducing the number of key/value pairs that must be shuffled across the network and delivered as input to the Reducer.

**Partitioner:** Partitioner controls the partitioning of the keys of the intermediate map output in Hadoop. To determine partition, the hash function is employed. Each map output is partitioned based on the key-value pair. Each partition (inside each mapper) contains records with the same key value, and each partition is subsequently forwarded to a Reducer. Partition phase takes place in between mapper and reducer.
The Hash Partitioner (Default Partitioner) computes a hash value for the key and assigns the partition based on it.

**Reducer:**
In Hadoop MapReduce, a reducer condenses a set of intermediate values that share a key into a smaller set. Reducer takes a set of intermediate key-value pairs produced by the mapper as input in the MapReduce job execution flow. Reducer then aggregates, filters, and combines key-value pairs, which necessitates extensive processing.

Firstly, the regex pattern is checked across the log entry for all four tasks. If it succeeds, it moves on to the job computation.
Note : regex pattern is mentioned in the application.conf

**1) Task 1:** To find the count of generated log messages with injected regex pattern for each message type in a predefined time interval.

A predefined time interval is mentioned in the application.conf. Using this start and end time, the log messages with that injected regex pattern for every log message tag is counted.(predefined time interval - startTime and endTime are configured in `application.conf`)

Task1Mapper: (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]), (value -> 1)

Task1Reducer : (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]) , (value -> sum of the logMessage count)

**2) Task 2:** To display the time intervals sorted in the descending order that contained most log messages of the type ERROR with injected regex pattern string instances.

Every one hour is chosen as the time interval here. So, for every hour the ERROR tag injected regex pattern log messages is counted and displayed in descending order based on the count.

Task2 has two mapper and reducer. The first mapper and reducer provides the aggregated count of log message. The second mapper and reducer is used for sorting. Task2Reducer1 output is passed to Task2Mapper2 and Task2Mapper2 sends the (key - count of log messages * -1,value - the time) to Task2reducer2 where the sorting is done based on the key.

Task2Mapper1: (Key, Value) (key -> Hour - [1..24]), (value -> 1)

Task2Reducer1 : (Key, Value) (key -> Hour - [1..24]) , (value -> sum of the Error tag logMessage count)

Task2Mapper2: (Key, Value) (key -> sum of the Error tag logMessage count * -1 ), (value -> hour [1..24])

In Task2Reducer2, the sorted is done based on the key value sent by the Task2Mapper2

Task2Reducer2 : (Key, Value) (key -> Hour - [1..24]) , (value -> sum of the Error tag logMessage count)

**3) Task 3:** To find the count of generated log messages for each message type.

The log messages for every log message tag is counted

Task3Mapper: (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]), (value -> 1)

Task3Reducer : (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]) , (value -> sum of the logMessage count)

**4) Task 4:** To produce the number of characters in each log message for each log message type that contain the highest number of characters in the detected instances of the designated regex pattern.

The max length of every injected regex pattern log message for every log message is displayed

Task4Mapper: (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]), (value -> logMessageLength)

Task4Reducer : (Key, Value) (key -> logMessageTag - [ERRO/INFO/WARN/DEBUG]) , (value -> max of the logMessageLength)

### Output

The output of every task can be located under OutputFiles folder
You can see two output files from reach reduce task.

task1/task2/task3/task4 : One comma separated output file with the desired output.

task2Intermediate : This is the intermediate output of task2 before sorting. This is the output from the Task2Reducer1.
