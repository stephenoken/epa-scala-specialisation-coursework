{

  def sqrtIter(guess: Double, x: Double): Double =
    if (isGoodEnough(guess, x)) guess
    else sqrtIter(imporve(guess, x), x)

  def isGoodEnough(guess: Double, x: Double) =
    Math.abs(guess * guess - x) / x < 0.001

  def imporve(guess: Double, x: Double) =
    (guess + x / guess) / 2

  def sqrt(x: Double) = sqrtIter(1.0, x)

sqrt(2)
sqrt(4)
sqrt(1e-6)
sqrt(1e60)
}


/*
The use of block allows for definitions within
the block to be hidden from outside functions.
However, outside functions are visible as long
there are no "shadow" functions contained within
the block
 */

def sqrt(x: Double) = {

  def sqrtIter(guess: Double, x: Double): Double =
    if (isGoodEnough(guess, x)) guess
    else sqrtIter(imporve(guess, x), x)

  def isGoodEnough(guess: Double, x: Double) =
    Math.abs(guess * guess - x) / x < 0.001

  def imporve(guess: Double, x: Double) =
    (guess + x / guess) / 2

  sqrtIter(1.0, x)
}

sqrt(2)
sqrt(4)
sqrt(1e-6)
sqrt(1e60)

val x = 0

def f(y: Int) = y + 1

val result = {
  val x = f(3) // example of shaowing
  x * x
} + x

{

  // Tail Recursive Function
  def gcd(a : Int, b :Int) : Int =
    if (b ==0) a else gcd(b, a % b)

  println(gcd(14, 21))

  // Not a tail recursive function
  def factorial(n: Int) : Int =
    if(n == 0) 1 else n * factorial(n -1)

  println(factorial(4))
}

{
  // Tail recursive
  def factorial(n : Int): Int = {
    def loop(acc: Int, n: Int): Int =
      if (n == 0) acc
      else loop(n * acc, n - 1)

    loop(1, n)
  }

  println(factorial(4))
}
