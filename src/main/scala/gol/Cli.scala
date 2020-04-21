package gol

object Cli extends App {

  import World._

  def clearScreen(): Unit = {
    print("\u001b[2J")
  }

  val points = Set(
    Point(3, 5),
    Point(4, 5),
    Point(5, 5)
  )
  clearScreen()
  val world = World.random(4, 4, 0.5)
  println(world)
  println(tick(3, world))
}
