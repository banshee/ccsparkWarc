package com.banshee.ccspark

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.channels.Channels
import java.util.concurrent.atomic.AtomicLong

import com.google.common.base.Charsets
import com.google.common.io.CharStreams
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.mapred.FileInputFormat
import org.apache.hadoop.mapred.FileSplit
import org.apache.hadoop.mapred.InputSplit
import org.apache.hadoop.mapred.JobConf
import org.apache.hadoop.mapred.RecordReader
import org.apache.hadoop.mapred.Reporter
import org.netpreserve.jwarc.WarcReader
import org.netpreserve.jwarc.WarcReader
import org.netpreserve.jwarc.WarcReader
import org.netpreserve.jwarc.WarcReader
import org.netpreserve.jwarc.WarcRecord
import resource.managed

import scala.collection.JavaConverters._
import scala.collection.Map
import scala.compat.java8.OptionConverters._
import scala.util.Try

/**
 * A simple container for WARC data.
 *
 * @param headers WARC headers
 * @param body    WARC body
 */
case class RawWarcRecord(
  headers: Map[String, List[String]],
  body: String
)

/**
 * Provides a mutable wrapper for Hadoop RecordReader (and functions like
 * [[RawWarcRecordReader#next(org.apache.hadoop.io.LongWritable, com.banshee.ccspark.MutableRawWarcRecord)]]
 *
 * We can't use [[com.banshee.ccspark.MutableRawWarcRecord]] directly because
 * RecordReader wants to write records into a mutable object, presumably to reduce
 * the number of memory allocations it needs to do.
 */
case class MutableRawWarcRecord(var record: RawWarcRecord)

object RawWarcRecord {
  /**
   * Given a [[WarcRecord]], create a [[RawWarcRecord]].
   *
   * Returns a [[Try]] because it has to do IO to read the
   * body from {@code record}.
   *
   * @param record the [[WarcRecord]]
   * @return A [[WarcRecord]] built from {@code record}
   */
  def fromWarcRecord(record: WarcRecord): Try[RawWarcRecord] = {
    val result = for {
      reader <- managed(Channels.newReader(record.body(), Charsets.UTF_8.name()))
    } yield {
      val body: String = CharStreams.toString(reader)
      val headersAsScalaMap: Map[String, List[String]] =
        record.headers().map().asScala.mapValues(_.asScala.toList)
      RawWarcRecord(headers = headersAsScalaMap, body = body)
    }
    result.tried
  }
}

/**
 * A Hadoop [[RecordReader]] for reading WARC files.
 *
 * Creating a [[RawWarcRecordReader]] will immediately start IO using
 * the source passed as the {@code job} parameter.
 *
 * This code relies on the framework to do exception handling.
 *
 * See [[org.apache.hadoop.mapred.RecordReader]] for parameter documentation.
 *
 * @param split
 * @param job
 * @param reporter
 */

class RawWarcRecordReader(split: InputSplit, job: JobConf, reporter: Reporter) extends RecordReader[LongWritable, MutableRawWarcRecord] {
  private val path = split.asInstanceOf[FileSplit].getPath

  // There's no useful key for a WARC record, so we just
  // use an incrementing Long.
  val currentRecordNumber = new AtomicLong(0)

  // WarcReader is a foreign library for reading WARC files
  val warcReader = new WarcReader(path.getFileSystem(job).open(path))

  override def next(key: LongWritable, value: MutableRawWarcRecord): Boolean = {
    warcReader.next().asScala
      .map(RawWarcRecord.fromWarcRecord(_).get)
      .map { record =>
        key.set(currentRecordNumber.getAndIncrement())
        value.record = record
      }
      .isDefined
  }

  override def createKey(): LongWritable = new LongWritable()
  override def createValue(): MutableRawWarcRecord = MutableRawWarcRecord(null)
  override def getPos: Long = warcReader.position()
  override def close(): Unit = warcReader.close()
  override def getProgress: Float = getPos.toFloat / split.getLength.toFloat
}

/**
 * A Hadoop [[org.apache.hadoop.mapred.FileInputFormat]] for [[MutableRawWarcRecord]] elements
 */
class RawWarcInputFormat extends FileInputFormat[LongWritable, MutableRawWarcRecord] {
  // While it looks like WARC files actually could be split, the existing WARC files
  // are also compressed.  Doing the work to make a splittable FileInputPath isn't
  // useful.
  override def isSplitable(fs: FileSystem, filename: Path): Boolean = false
  override def getRecordReader(split: InputSplit, job: JobConf, reporter: Reporter): RecordReader[LongWritable, MutableRawWarcRecord] =
    new RawWarcRecordReader(split, job, reporter)
}

