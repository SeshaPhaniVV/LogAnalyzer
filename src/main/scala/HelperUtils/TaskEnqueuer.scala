package vvakic2.uic.cs441
package HelperUtils

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapreduce.Job
import org.slf4j.Logger
import org.apache.hadoop.io.{DoubleWritable, Text, IntWritable}
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.{FileOutputFormat, TextOutputFormat}
import org.apache.hadoop.fs.Path

import LogAnalyzerTasks.{Task1, Task2, Task3, Task4}
import LogAnalyzerTasks.Task1.{Task1Mapper, Task1Reducer}
import LogAnalyzerTasks.Task2.{Task2Mapper1, Task2Mapper2, Task2Reducer1, Task2Reducer2}
import LogAnalyzerTasks.Task3.{Task3Mapper, Task3Reducer}
import LogAnalyzerTasks.Task4.{Task4Mapper, Task4Reducer}

class TaskEnqueuer(logger: Logger):
   private val conf: Config          = ConfigFactory.load("application.conf")
   private val inputFile: String     = conf.getString("logAnalyzer.inputFile")
   private val outputFile: String    = conf.getString("logAnalyzer.outputFile")
   private val task1Name             = "task1"
   private val task2IntermediateName = "task2Intermediate"
   private val task2Name             = "task2"
   private val task3Name             = "task3"
   private val task4Name             = "task4"

   /** Task 1: Enqueues a Job which finds the count of log messages with injected regex pattern for
     * each message type in a predefined time interval.
     */
   def enqueueTask1(): Job =
      logger.info(s"Enqueuing task ${task1Name}")
      val configuration: Configuration = new Configuration()
      configuration.set("mapred.textoutputformat.separator", ",")
      val task1: Job = Job.getInstance(configuration, task1Name)
      task1.setJarByClass(classOf[Task1])
      task1.setMapperClass(classOf[Task1Mapper])
      task1.setReducerClass(classOf[Task1Reducer])
      task1.setOutputKeyClass(classOf[Text])
      task1.setOutputValueClass(classOf[IntWritable])
      task1.setOutputFormatClass(classOf[TextOutputFormat[Text, IntWritable]])
      FileInputFormat.addInputPath(task1, new Path(inputFile))
      FileOutputFormat.setOutputPath(task1, new Path(outputFile + "/" + task1Name))
      task1

   /** Task 2: Enqueues a Job which gets Error Messages count from the logs with injected regex
     * pattern.
     */
   def enqueueIntermediateTask2(): Job =
      logger.info(s"Enqueuing task ${task2Name}")
      val configuration: Configuration = new Configuration()
      configuration.set("mapred.textoutputformat.separator", ",")
      val task2: Job = Job.getInstance(configuration, task2IntermediateName)
      task2.setJarByClass(classOf[Task2])
      task2.setMapperClass(classOf[Task2Mapper1])
      task2.setReducerClass(classOf[Task2Reducer1])
      task2.setOutputKeyClass(classOf[Text])
      task2.setOutputValueClass(classOf[IntWritable])
      FileInputFormat.addInputPath(task2, new Path(inputFile))
      FileOutputFormat.setOutputPath(task2, new Path(outputFile + "/" + task2IntermediateName))
      task2.waitForCompletion(true)
      task2

   /** Task 2: Enqueues a Job which Sorts above intermediate error messages count data into
     * descending order
     */
   def enqueueTask2(): Job =
      val configuration: Configuration = new Configuration()
      configuration.set("mapred.textoutputformat.separator", ",")
      val task2Final = Job.getInstance(configuration, task2Name)
      task2Final.setJarByClass(classOf[Task2])
      task2Final.setMapperClass(classOf[Task2Mapper2])
      task2Final.setReducerClass(classOf[Task2Reducer2])
      task2Final.setNumReduceTasks(1)
      task2Final.setMapOutputKeyClass(classOf[IntWritable])
      task2Final.setMapOutputValueClass(classOf[Text])
      task2Final.setOutputKeyClass(classOf[Text])
      task2Final.setOutputValueClass(classOf[IntWritable])
      FileInputFormat.addInputPath(task2Final, new Path(outputFile + "/" + task2IntermediateName))
      FileOutputFormat.setOutputPath(task2Final, new Path(outputFile + "/" + task2Name))
      task2Final

   /** Task 3: Enqueues a Job To find the count of generated log messages for each message type. */
   def enqueueTask3(): Job =
      logger.info(s"Enqueuing task ${task3Name}")
      val configuration: Configuration = new Configuration()
      configuration.set("mapred.textoutputformat.separator", ",")
      val task3: Job = Job.getInstance(configuration, task3Name)
      task3.setJarByClass(classOf[Task3])
      task3.setMapperClass(classOf[Task3Mapper])
      task3.setReducerClass(classOf[Task3Reducer])
      task3.setOutputKeyClass(classOf[Text])
      task3.setOutputValueClass(classOf[IntWritable])
      task3.setOutputFormatClass(classOf[TextOutputFormat[Text, IntWritable]])
      FileInputFormat.addInputPath(task3, new Path(inputFile))
      FileOutputFormat.setOutputPath(task3, new Path(outputFile + "/" + task3Name))
      task3

   /** Task 4: Enqueues a Job which generates the highest number of characters in the matched regex
     * patterns from the logs for each of the log message type.
     */
   def enqueueTask4(): Job =
      logger.info(s"Enqueuing task $task4Name")
      val configuration: Configuration = new Configuration()
      configuration.set("mapred.textoutputformat.separator", ",")
      val task4: Job = Job.getInstance(configuration, task4Name)
      task4.setJarByClass(classOf[Task4])
      task4.setMapperClass(classOf[Task4Mapper])
      task4.setReducerClass(classOf[Task4Reducer])
      task4.setOutputKeyClass(classOf[Text])
      task4.setOutputValueClass(classOf[IntWritable])
      task4.setOutputFormatClass(classOf[TextOutputFormat[Text, IntWritable]])
      FileInputFormat.addInputPath(task4, new Path(inputFile))
      FileOutputFormat.setOutputPath(task4, new Path(outputFile + "/" + task4Name))
      task4

end TaskEnqueuer
