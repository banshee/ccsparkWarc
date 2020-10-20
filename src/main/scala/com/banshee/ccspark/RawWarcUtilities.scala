package com.banshee.ccspark

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter

import com.google.common.base.Charsets
import org.netpreserve.jwarc.WarcReader
import resource.managed

import scala.collection.JavaConverters._


object RawWarcUtilities {
  /**
   * Given an {@code inputStream} containing WARC data, write
   * that data to {@code outputStreamWriter} as json.
   *
   * @param inputStream
   * @param outputStreamWriter
   */
  def rewriteWarc(inputStream: InputStream, outputStreamWriter: OutputStreamWriter) = {
    for {
      warcReader <- managed(new WarcReader(inputStream))
      nativeRecord <- warcReader.iterator().asScala
      record <- RawWarcRecord.fromWarcRecord(nativeRecord)
    } {
      import io.circe.generic.auto._
      import io.circe.syntax._
      val jsonString = record.asJson.noSpaces
      outputStreamWriter.write(jsonString)
      outputStreamWriter.write("\n")
    }
  }

  def main(args: Array[String]): Unit = {
    val (inputFilename, outputFilename) = args.toList match {
      case Nil => ("-", "-")
      case i :: Nil => (i, "-")
      case i :: j :: _ => (i, j)
    }
    val inputFile = inputFilename match {
      case "-" => System.in
      case f => new FileInputStream(f)
    }
    val writer = {
      val t: OutputStream = outputFilename match {
        case "-" => System.out
        case f => new FileOutputStream(f)
      }
      new java.io.OutputStreamWriter(t, Charsets.UTF_8)
    }
    RawWarcUtilities.rewriteWarc(inputFile, writer)
    inputFile.close()
    writer.close()
  }
}