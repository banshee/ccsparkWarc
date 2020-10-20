package com.banshee.ccspark

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

import com.banshee.BansheeStandardSpec
import com.google.common.io.Files

/**
 * This isn't a spec, just some example code to
 * exercise [[RawWarcRecord.fromWarcRecord]]
 */
class RawWarcUtilitiesSpec extends BansheeStandardSpec {
  describe("RawWarcUtilities") {
    it("rewriteWarc") {
      val in = Files.asByteSource(new File("/home/james/workspace/dataccspark/short1.txt"))
      val out = File.createTempFile("prefix1", ".json")
      val fileOutputStream = new FileOutputStream(out)
      val outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)
      RawWarcUtilities.rewriteWarc(
        in.openBufferedStream(),
        outputStreamWriter,
      )
      outputStreamWriter.close()
    }
  }
}