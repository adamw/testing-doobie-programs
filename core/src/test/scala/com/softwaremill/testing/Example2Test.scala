package com.softwaremill.testing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class Example2Test extends AnyFlatSpec with Matchers {

  it should "add 1 point" in {
    import Example2._
    PointsLogic.calculatePointsIncrease(15) shouldBe 16
  }

  it should "add 3 points" in {
    import Example2._
    PointsLogic.calculatePointsIncrease(16) shouldBe 19
  }
}
