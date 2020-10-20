package com.banshee

import org.scalactic.source
import org.scalatest._

trait BansheeStandardSpec extends FunSpec with Matchers with Inside {
  def todo(specText: String, testTags: Tag*)(implicit pos: source.Position): Unit =
    (new ItWord) (specText, testTags: _*)({pending})(pos)
}