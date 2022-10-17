package vvakic2.uic.cs441
package LogAnalyzerTasks

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.mapreduce.{Job, Mapper, Partitioner, Reducer}

import java.lang.Iterable
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.StringTokenizer
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

class Task1

object Task1 {
  val conf: Config = ConfigFactory.load("application.conf")

  class Task1Mapper extends Mapper[Object, Text, Text, IntWritable] {
    val count = new IntWritable(1)
    val tag   = new Text()

    /** Finds the log messages with injected regex pattern for each message type in a predefined
      * time interval.
      *
      * @param key
      *   \- Object - Log Message Tag
      * @param value
      *   \- Text - Count of the Log Message Tag
      * @return
      *   \- returnType : Unit - Key, Value
      */
    override def map(
        key: Object,
        value: Text,
        context: Mapper[Object, Text, Text, IntWritable]#Context
    ): Unit = {
      val keyValPattern: Regex = conf.getString("logAnalyzer.regexPattern").r
      val injectPattern: Regex = conf.getString("logAnalyzer.injectedStringPattern").r
      val formatter            = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

      // Start and end time are pre defined in configuration
      val startTime = LocalTime.parse(conf.getString("logAnalyzer.startTime"), formatter)
      val endTime   = LocalTime.parse(conf.getString("logAnalyzer.endTime"), formatter)

      /** Finds matched regex log messages and checks if it is inside the time interval set in
        * configuration. If both the cases are successful then we will produce output as key -> INFO
        * \| ERROR | DEBUG | WARN val -> 1 (count of occurrence)
        */
      val patternMatch = keyValPattern.findFirstMatchIn(value.toString)
      patternMatch.toList.foreach(x => {
        injectPattern.findFirstMatchIn(x.group(5)) match {
          case Some(_) => {
            val time = LocalTime.parse(x.group(1), formatter)
            if (startTime.isBefore(time) && endTime.isAfter(time)) {
              tag.set(x.group(3))
              context.write(tag, count)
            }
          }
          case None => println("The Log message is not matching inject regex pattern")
        }
      })
    }
  }

  class Task1Reducer extends Reducer[Text, IntWritable, Text, IntWritable] {

    /** finds the count of generated log messages with injected regex pattern for each message type
      * in a predefined time interval in application.conf
      *
      * @param key
      *   : Text - Unique Log Message Tag
      * @param values
      *   : IntWritable - aggregated count of the Log Message Tag
      * @return
      *   returnType : Unit - Key, Value
      */
    override def reduce(
        key: Text,
        values: Iterable[IntWritable],
        context: Reducer[Text, IntWritable, Text, IntWritable]#Context
    ): Unit = {
      val sum = values.asScala.foldLeft(0)(_ + _.get)
      context.write(key, new IntWritable(sum))
    }
  }
}
