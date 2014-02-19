package com.snowplowanalytics.hadoop.scalding


import com.twitter.scalding.Args
import com.twitter.scalding._

import cascading.pipe.Pipe


class MailReporting(args: Args) extends Job(args: Args) /*extends JobUtil(args) with CommonFunc with NoDecile*/ {


  //val logInputs = mailedReportedField(args("logs"), 0).discard('TemplateId)

  val logInputs = IterableSource(List(4,5,6,7), 'foo).read

  val NB_REDUCERS = 8


  def computeBiReport(input: Pipe, startingDate: Option[Long]): Pipe = {

    val inputFiltered = startingDate.map { date =>
      input.filter('Created) { c: Long =>  c >= date }
    }.getOrElse(input)

    val inputWithDay = inputFiltered.map('Created -> 'Day) { created: Long =>
      System.currentTimeMillis
    }

    val count  = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.size('Count)}
    val start  = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.min('Created) }.rename('Created -> 'StartDate)
    val median = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.average('Created) }.rename('Created -> 'MedianDate)
    val end    = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.max('Created) }.rename('Created -> 'EndDate)

    count
      .joinWithTiny(('Day, 'Campaign, 'Status, 'Monthly) -> ('Day, 'Campaign, 'Status, 'Monthly), start)
      .joinWithTiny(('Day, 'Campaign, 'Status, 'Monthly) -> ('Day, 'Campaign, 'Status, 'Monthly), median)
      .joinWithTiny(('Day, 'Campaign, 'Status, 'Monthly) -> ('Day, 'Campaign, 'Status, 'Monthly), end)
  }

  //val startingDate = args.optional("starting-date").map(DateUtil.parse(_))

    val biReport = computeBiReport(logInputs, None).mapTo(('Day, 'Campaign, 'Status, 'Count, 'StartDate, 'MedianDate, 'EndDate, 'Monthly) -> 'biReport) {
    fields: (Long, String, String, Int, Long, Long, Long, Boolean) =>

  //val (day, campaign, status, count, startDate, medianDate, lastCreated, monthly) = fields
      //BIReport(day, status, count, campaign, startDate, medianDate, lastCreated, monthly)
  }

  //biReport.write(PackedAvroSource[BIReport](args("output-bi")))
}
