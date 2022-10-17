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
import java.util.StringTokenizer
import scala.jdk.CollectionConverters._
import scala.util.matching.Regex

class Task4

object Task4 {
  val conf: Config = ConfigFactory.load("application.conf")

  class Task4Mapper extends Mapper[Object, Text, Text, IntWritable] {
    val tag = new Text()

    /** Map method produces the number of characters in each log message for each log message type
      * of the matched regex pattern.
      *
      * @param key
      *   : Object - Log Message Tag
      * @param value
      *   : Text - value 1
      * @return
      *   returnType : Unit - [Key, Value]
      */
    override def map(
        key: Object,
        value: Text,
        context: Mapper[Object, Text, Text, IntWritable]#Context
    ): Unit = {

      val keyValPattern: Regex = conf.getString("logAnalyzer.regexPattern").r
      val injectPattern: Regex = conf.getString("logAnalyzer.injectedStringPattern").r

      /** If the Log entry matches the regex pattern, the generated log messages matches the
        * injected string patter, Key -> Log Messgae Tag and value -> log message length
        */
      val patternMatch = keyValPattern.findFirstMatchIn(value.toString)
      patternMatch.toList.foreach((pattern) => {
        injectPattern.findFirstMatchIn(pattern.group(5)) match {
          case Some(_) => {
            val charLength = new IntWritable(pattern.group(5).length)
            tag.set(pattern.group(3))
            context.write(tag, charLength)
          }
          case None => println("error")
        }
      })
    }
  }

  class Task4Reducer extends Reducer[Text, IntWritable, Text, IntWritable] {

    /** Reducer method gets the length of each message and gets the maximum length for a log type
      * ex: INFO, 89 -> Max characters present in one INFO Log across the logs are 89
      *
      * @param key
      *   : Text - Log Message Tag
      * @param values
      *   : IntWritable - max value of every log message tag
      * @return
      *   returnType : Unit - (Key, Value)
      */
    override def reduce(
        key: Text,
        values: Iterable[IntWritable],
        context: Reducer[Text, IntWritable, Text, IntWritable]#Context
    ): Unit = {

      // the max of the value for a specific log message tag is retrieved
      val maxLength = values.asScala.foldLeft(0)(_ max _.get)
      context.write(key, new IntWritable(maxLength))
    }
  }
}
