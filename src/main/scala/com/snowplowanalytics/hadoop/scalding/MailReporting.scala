package com.snowplowanalytics.hadoop.scalding

import org.joda.time.DateMidnight

import com.twitter.scalding.Args
import com.twitter.scalding._

import cascading.pipe.Pipe


class MailReporting(args: Args) extends Job(args: Args) /*extends JobUtil(args) with CommonFunc with NoDecile*/ {


  //val logInputs = mailedReportedField(args("logs"), 0).discard('TemplateId)

  val NB_REDUCERS = 8


  def computeBiReport(input: Pipe, startingDate: Option[Long]): Pipe = {

    val inputFiltered = startingDate.map { date =>
      input.filter('Created) { c: Long =>  c >= date }
    }.getOrElse(input)

    val inputWithDay = inputFiltered.map('Created -> 'Day) { created: Long =>
      new org.joda.time.DateMidnight(created).toDate().getTime()
    }

    val count  = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.size('Count)(NB_REDUCERS)}
    val start  = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.min('Created)(NB_REDUCERS) }.rename('Created -> 'StartDate)
    val median = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.average('Created)(NB_REDUCERS) }.rename('Created -> 'MedianDate)
    val end    = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.max('Created)(NB_REDUCERS) }.rename('Created -> 'EndDate)

    count
      .joinWithTiny(('Day, 'Campaign, 'Status, 'Monthly) -> ('Day, 'Campaign, 'Status, 'Monthly), start)
      .joinWithTiny(('Day, 'Campaign, 'Status, 'Monthly) -> ('Day, 'Campaign, 'Status, 'Monthly), median)
      .joinWithTiny(('Day, 'Campaign, 'Status, 'Monthly) -> ('Day, 'Campaign, 'Status, 'Monthly), end)
  }

  //val startingDate = args.optional("starting-date").map(DateUtil.parse(_))

  val biReport = computeBiReport(logInputs, None).mapTo(('Day, 'Campaign, 'Status, 'Count, 'StartDate, 'MedianDate, 'EndDate, 'Monthly) -> 'biReport) {
    fields: (Long, String, String, Int, Long, Long, Long, Boolean) =>

  val (day, campaign, status, count, startDate, medianDate, lastCreated, monthly) = fields
      BIReport(day, status, count, campaign, startDate, medianDate, lastCreated, monthly)
  }

  //biReport.write(PackedAvroSource[BIReport](args("output-bi")))
}
