package com.snowplowanalytics.hadoop.scalding


import com.twitter.scalding.Args
import com.twitter.scalding._

import cascading.pipe.Pipe
import shapeless.ops.record.{Updater, Remover}


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

import shapeless._
import record._
import syntax.singleton._

import TDsl._

object Utils {

  case class Input(day: Long, status: String, campaign: String, monthly: Boolean)

  object mkField extends Poly1 { implicit def default[K, V] = at[(K, V)]{ case (k, v) => field[K](v) } }

  val dayS = Witness("day")
  val statusS = Witness("status")
  val campaignS = Witness("campaign")
  val monthlyS = Witness("monthly")
  val inputFields = "day".narrow :: "status".narrow :: "campaign".narrow :: "monthly".narrow :: HNil

  def mkPersonRecord(p: Input) = (inputFields zip Generic[Input].to(p)) map mkField


  type InputRecord = :: [shapeless.record.FieldType[dayS.T,Long],
                    ::[shapeless.record.FieldType[statusS.T,String],
                      ::[shapeless.record.FieldType[campaignS.T,String],
                        ::[shapeless.record.FieldType[monthlyS.T, Boolean],
                          HNil]]]] with Serializable
}

class MailReporting2(args: Args) extends Job(args: Args) /*extends JobUtil(args) with CommonFunc with NoDecile*/ {


  //val logInputs = mailedReportedField(args("logs"), 0).discard('TemplateId)
  import Utils._

  val testInput = Input(day = 0L, status = "sent", campaign = "mycampaign", monthly = false )
  val logInputs = IterableSource(Iterator.fill(15)(testInput).toList, 'input).read.toTypedPipe[Input]('input).map(mkPersonRecord(_))

  val NB_REDUCERS = 8

  def map[Out](t: TypedPipe[InputRecord])(from: Witness, to: Witness)(f: (InputRecord) => Out)(implicit remover : Remover[InputRecord, from.T]) = {
    t.map { in: InputRecord =>
      in + (to.value ->> f (in))
      in.remove(from)
    }
  }

  def computeBiReport(input: TypedPipe[InputRecord], startingDate: Option[Long]): Pipe = {

    val inputFiltered = startingDate.map { date =>
      input.filter { in =>  in("day") >= date }
    }.getOrElse(input)

    val inputWithDay = map(input)(Witness("day"), Witness("created")) { in: InputRecord =>

      System.currentTimeMillis
    }.map(_._2)

    inputWithDay.map(x => x("status")).toPipe[String]('input)
    /*val count  = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.size('Count)}
    val start  = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.min('Created) }.rename('Created -> 'StartDate)
    val median = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.average('Created) }.rename('Created -> 'MedianDate)
    val end    = inputWithDay.groupBy('Day, 'Status, 'Campaign, 'Monthly) { _.max('Created) }.rename('Created -> 'EndDate)

    count
      .joinWithTiny(('Day, 'Campaign, 'Status, 'Monthly) -> ('Day, 'Campaign, 'Status, 'Monthly), start)
      .joinWithTiny(('Day, 'Campaign, 'Status, 'Monthly) -> ('Day, 'Campaign, 'Status, 'Monthly), median)
      .joinWithTiny(('Day, 'Campaign, 'Status, 'Monthly) -> ('Day, 'Campaign, 'Status, 'Monthly), end)*/
  }

  //val startingDate = args.optional("starting-date").map(DateUtil.parse(_))

  val biReport = computeBiReport(logInputs, None).mapTo(('Day, 'Campaign, 'Status, 'Count, 'StartDate, 'MedianDate, 'EndDate, 'Monthly) -> 'biReport) {
    fields: (Long, String, String, Int, Long, Long, Long, Boolean) =>

    //val (day, campaign, status, count, startDate, medianDate, lastCreated, monthly) = fields
    //BIReport(day, status, count, campaign, startDate, medianDate, lastCreated, monthly)
  }

  //biReport.write(PackedAvroSource[BIReport](args("output-bi")))
}