import frp.Var

object Main extends App {
  val x = Var(3)
  val y = Var(x() + 5)
  println(y()) // 8
  x() = 6
  println(y()) // 11
}