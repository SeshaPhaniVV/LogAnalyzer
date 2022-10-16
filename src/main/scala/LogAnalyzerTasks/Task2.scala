package vvakic2.uic.cs441
package LogAnalyzerTasks

import com.typesafe.config.{Config, ConfigFactory}

import java.lang.Iterable
import java.util.StringTokenizer
import org.apache.hadoop.conf.Configuration

import scala.collection.JavaConverters._
import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.mapreduce.{Job, Mapper, Partitioner, Reducer}

import scala.io.Source
import scala.util.matching.Regex

class Task2

/** Mapper1 and Reducer1 are an intermediate mappers and reducers which gets error messages count
  * based on hour. This intermediate output is read by Mapper2 and Reducer2 to sort the output in
  * descending order.
  */
object Task2 {
  val conf: Config = ConfigFactory.load("application.conf")

  class Task2Mapper1 extends Mapper[Object, Text, Text, IntWritable] {
    val count    = new IntWritable(1)
    val interval = new Text()

    /** Displays the count log messages of the type ERROR with injected regex pattern string
      * instances in every one hour(time intervals).
      *
      * @param key
      *   : Object - Time Interval
      * @param value
      *   : Text - message count
      * @return
      *   returnType : Unit - (key, value)
      */
    override def map(
        key: Object,
        value: Text,
        context: Mapper[Object, Text, Text, IntWritable]#Context
    ): Unit = {
      val keyValPattern: Regex  = conf.getString("logAnalyzer.errorRegexPattern").r
      val inject_pattern: Regex = conf.getString("logAnalyzer.injectedStringPattern").r

      // If the a Log entry matches the regex pattern, and the generated log messages matches the injected string pattern
      // the ERROR log message is counted
      // Here. Key - TIME (HOUR) -  GROUP(1) . Time is in HH.mm.ss.SSS format so split(:)(0) is hour
      // Value - count : 1
      val p = keyValPattern.findAllMatchIn(value.toString)

      p.toList.foreach((pattern) => {
        inject_pattern.findFirstMatchIn(pattern.group(5)) match {
          case Some(_) => {
            interval.set(pattern.group(1).split(":")(0))
            context.write(interval, count)
          }
          case None => println("The Log message is not matching inject regex pattern")
        }
      })

    }
  }

  class Task2Reducer1 extends Reducer[Text, IntWritable, Text, IntWritable] {

    /** Task 2: Reducer to display the count log messages of the type ERROR with injected regex
      * pattern string instances in every one hour(time intervals).
      *
      * @param key
      *   : Text - Time Interval
      * @param values
      *   : IntWritable - Aggregated Message count
      * @return
      *   returnType : Unit - (key, value)
      */
    override def reduce(
        key: Text,
        values: Iterable[IntWritable],
        context: Reducer[Text, IntWritable, Text, IntWritable]#Context
    ): Unit = {
      // for every hour the count of log messages are summed
      val sum = values.asScala.foldLeft(0)(_ + _.get())
      context.write(key, new IntWritable(sum))
    }
  }

  class Task2Mapper2 extends Mapper[Object, Text, IntWritable, Text] {

    /** Task 2: Mapper method to display in SORTED ORDER based on the count log messages of the type
      * ERROR with injected regex pattern string instances in every one hour(time intervals)
      *
      * @param key
      *   : Object - Time Interval
      * @param value
      *   : Text - Aggregated Message Count
      * @return
      *   returnType : Unit - (key, value)
      */
    override def map(
        key: Object,
        value: Text,
        context: Mapper[Object, Text, IntWritable, Text]#Context
    ): Unit = {
      // output from reducer1 is split with , separator
      // Here, the count of the log messages is multiplied with -1 and passed as key which will be sorted in descending order by reducer
      val line   = value.toString.split(",")
      val result = line(1).toInt * -1
      context.write(new IntWritable(result), new Text(line(0)))

    }
  }

  class Task2Reducer2 extends Reducer[IntWritable, Text, Text, IntWritable] {

    /** Reducer method to display in SORTED ORDER based on the count log messages of the type ERROR
      * with injected regex pattern string instances in every one hour(time intervals)
      *
      * @param key
      *   : IntWritable - Time Interval
      * @param values
      *   : Text - Sorted order count(descending order)
      * @return
      *   returnType : Unit - (key, value)
      */
    override def reduce(
        key: IntWritable,
        values: Iterable[Text],
        context: Reducer[IntWritable, Text, Text, IntWritable]#Context
    ): Unit = {
      values.asScala.foreach(value => context.write(value, new IntWritable(key.get() * -1)))
    }
  }

}
