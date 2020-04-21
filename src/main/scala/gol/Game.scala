package gol

import scala.util.Random

case class Point(x: Int, y: Int)

case class World private(width: Int, height: Int, cells: Set[Point]) {

  private def neighbors(p: Point): Set[Point] = {
    (for {
      x <- Math.max(1, p.x - 1).to(Math.min(this.width, p.x + 1))
      y <- Math.max(1, p.y - 1).to(Math.min(this.height, p.y + 1))
    } yield Point(x, y)).toSet - p
  }

  def changeSet: (Set[Point], Set[Point], Set[Point]) = {
    this.cells.map(neighbors)
      .foldLeft(Map.empty[Point, Int]) { case (allNeighbors, neighborSet) =>
        neighborSet.foldLeft(allNeighbors) { case (currentNeighbors, neighbor) =>
          currentNeighbors + (neighbor -> (currentNeighbors.getOrElse(neighbor, 0) + 1))
        }
      }
      .foldLeft((Set.empty[Point], Set.empty[Point], Set.empty[Point])) {
        case (s@(survivors, dying, born), (k, v)) =>
          (this.cells.contains(k), v) match {
            case (true, 2) => (survivors + k, dying, born)
            case (true, 3) => (survivors + k, dying, born)
            case (true, _) => (survivors, dying + k, born)
            case (false, 3) => (survivors, dying, born + k)
            case (false, _) => s
          }
      }
  }
}

object World {

  def seed(width: Int, height: Int, points: Set[Point]): World = {
    World(width, height, points)
  }

  def random(width: Int, height: Int, saturation: Double, rnd: Random = new Random()): World = {
    assert(saturation >= 0 && saturation <= 1)

    @scala.annotation.tailrec
    def populate(count: Int, set: Set[Point]): Set[Point] = {
      if (count == 0) set
      else {
        val p = Point(rnd.nextInt(width) + 1, rnd.nextInt(height) + 1)
        if (set.contains(p)) populate(count, set)
        else populate(count - 1, set + p)
      }
    }

    World(width, height, populate((width * height * saturation).toInt, Set.empty))
  }

  def tick(w: World): World = {
    val (survivors, _, born) = w.changeSet
    World(w.width, w.height, survivors ++ born)
  }

  def tick(n: Int, w: World): World = 1.to(n).foldLeft(w) { case (acc, _) => tick(acc) }
}
