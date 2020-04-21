package gol

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GameSpec extends AnyFlatSpec with Matchers {

  import World.{seed, tick}

  "Still life" should "stay block" in {
    val block = Set(Point(2, 2), Point(2, 3), Point(3, 2), Point(3, 3))
    tick(seed(4, 4, block)).cells shouldBe block
  }

  it should "stay beehive" in {
    val beehive = Set(Point(3, 2), Point(4, 2), Point(2, 3), Point(5, 3), Point(3, 4), Point(4, 4))
    tick(seed(6, 5, beehive)).cells shouldBe beehive
  }

  it should "stay loaf" in {
    val loaf = Set(Point(3, 2), Point(4, 2), Point(2, 3), Point(5, 3), Point(3, 4), Point(5, 4), Point(4, 5))
    tick(seed(6, 6, loaf)).cells shouldBe loaf
  }

  it should "stay boat" in {
    val boat = Set(Point(2, 2), Point(3, 2), Point(2, 3), Point(4, 3), Point(3, 4))
    tick(seed(5, 5, boat)).cells shouldBe boat
  }

  it should "stay tub" in {
    val tub = Set(Point(3, 2), Point(2, 3), Point(4, 3), Point(3, 4))
    tick(seed(5, 5, tub)).cells shouldBe tub
  }

  "Oscillators" should "be Blinker" in {
    val blinker1 = Set(Point(2, 3), Point(3, 3), Point(4, 3))
    val blinker2 = Set(Point(3, 2), Point(3, 3), Point(3, 4))
    tick(seed(5, 5, blinker1)).cells shouldBe blinker2
    tick(2, seed(5, 5, blinker1)).cells shouldBe blinker1
  }

  it should "be Toad" in {
    val toad1 = Set(Point(2, 3), Point(3, 3), Point(4, 3))
    val toad2 = Set(Point(3, 2), Point(3, 3), Point(3, 4))
    tick(seed(6, 6, toad1)).cells shouldBe toad2
    tick(2, seed(6, 6, toad1)).cells shouldBe toad1
  }

  it should "be Beacon" in {
    val beacon1 = Set(Point(2, 2), Point(2, 3), Point(3, 2), Point(5, 4), Point(4, 5), Point(5, 5))
    val beacon2 = Set(Point(2, 2), Point(2, 3), Point(3, 2), Point(3, 3), Point(4, 4), Point(5, 4), Point(4, 5), Point(5, 5))
    tick(seed(6, 6, beacon1)).cells shouldBe beacon2
    tick(2, seed(6, 6, beacon1)).cells shouldBe beacon1
  }

}
