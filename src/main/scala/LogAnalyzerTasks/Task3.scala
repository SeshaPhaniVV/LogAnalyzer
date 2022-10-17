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

class Task3

object Task3 {
  val conf: Config = ConfigFactory.load("application.conf")

  class Task3Mapper extends Mapper[Object, Text, Text, IntWritable] {
    val count = new IntWritable(1)
    val tag   = new Text()

    /** Mapper method reduces the log data into key, value pairs. for ex: INFO, 1. Filters the data
      * which doesn't match regex and doesn't lie in time stamps
      *
      * @param key
      *   : Text - Log Message Tag
      * @param value
      *   : IntWritable - Log message Count
      * @return
      *   returnType : Unit - (key, value)
      */
    override def map(
        key: Object,
        value: Text,
        context: Mapper[Object, Text, Text, IntWritable]#Context
    ): Unit = {
      val keyValPattern: Regex = conf.getString("logAnalyzer.regexPattern").r
      // Finds all the logs and writes key group(3), value 1
      // key -> INFO | ERROR | DEBUG | WARN
      // val -> 1 (count of occurrence)
      val p = keyValPattern.findAllMatchIn(value.toString)
      p.toList.foreach((pattern) => {
        tag.set(pattern.group(3))
        context.write(tag, count)
      })
    }
  }

  class Task3Reducer extends Reducer[Text, IntWritable, Text, IntWritable] {

    /** Reducer aggregates the values of each map intermediate output and gets the total count of
      * each log type.
      *
      * @param key
      *   : Text - Log Message Tag
      * @param values
      *   : IntWritable - Aggregated message Count
      * @return
      *   returnType : Unit - (key, value)
      */
    override def reduce(
        key: Text,
        values: Iterable[IntWritable],
        context: Reducer[Text, IntWritable, Text, IntWritable]#Context
    ): Unit = {
      val finalCount = values.asScala.foldLeft(0)(_ + _.get)
      context.write(key, new IntWritable(finalCount))
    }
  }
}
