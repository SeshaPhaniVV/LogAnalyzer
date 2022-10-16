package vvakic2.uic.cs441

import org.slf4j.{Logger, LoggerFactory}
import HelperUtils.TaskEnqueuer

object LogAnalyzer {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    logger.info("Starting MapReduce for all tasks")
    val TaskEnqueuer      = new TaskEnqueuer(logger)
    val task1             = TaskEnqueuer.enqueueTask1()
    val task3             = TaskEnqueuer.enqueueTask3()
    val task4             = TaskEnqueuer.enqueueTask4()
    val task2Intermediate = TaskEnqueuer.enqueueIntermediateTask2()
    // Wait for completion of task2Intermediate for task2 to sort on task2Intermediate output
    task2Intermediate.waitForCompletion(true)
    val task2Final = TaskEnqueuer.enqueueTask2()
    // On Successful completion log the success message else failed message
    if (
      task1.waitForCompletion(true) && task2Final
        .waitForCompletion(true) && task3.waitForCompletion(true) && task4.waitForCompletion(true)
    ) {
      logger.info("Log analysis is successful")
    } else {
      logger.info("Log analysis failed unfortunately")
    }
  }
}
