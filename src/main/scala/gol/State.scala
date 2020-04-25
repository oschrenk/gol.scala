package gol

case class State(world: World, rules: Rules) {
  def tick(): State = {
    State(World.tick(world), rules)
  }
}

case class Rules(tick: Int, speed: Int, seed: Seed)

sealed trait Seed
object Seed {
  case class random(width: Int, height: Int, value: Int, saturation: Double) extends Seed
  case class custom(width: Int, height: Int, cells: Set[Point]) extends Seed
}
