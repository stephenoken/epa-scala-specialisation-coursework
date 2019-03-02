{
  def product(f: Int => Int) (a: Int, b: Int): Int =
    if( a > b) 1
    else f(a) * product(f)(a + 1, b)

  def fact(n: Int) = product(x => x)(1,n)

  def mapReduce(f: Int => Int, combine: (Int, Int) => Int, zero: Int)(a: Int, b: Int): Int =
    if( a > b) zero
    else combine(f(a),mapReduce(f,combine,zero)(a + 1, b))

  def product2(f: Int => Int )(a:Int, b:Int): Int =
    mapReduce(f, (x, y) => x * y, 1)(a,b)
  product2(x => x * x)(3,4)

  def product3(a: Int, b:Int): Int = mapReduce(x=>x*x, (x, y) => x * y, 1)(a, b)
  product3(3,4)
}

{
// Rational Numbers as a class
  class Rational(x: Int, y:Int) {
    def numerator = x
    def denominator = y

  def add(that: Rational) =
    new Rational(
    numerator * that.denominator + that.numerator * denominator,
      denominator * that.denominator
    )

  def neg = new Rational(-numerator, denominator)

  def subtract(that: Rational) =  add(that.neg)

  override def toString = numerator + "/" + denominator
  }

  val x = new Rational(1,2)
  x.numerator
  x.denominator

val y = new Rational(2,3)
  x.add(y)

  println(x.subtract(y).subtract(x.neg))
}

{
  class Rational(x: Int, y:Int) {
    require(y != 0, "denominator must be nonzero") // Used to enforce a precondition on the caller of the function
    // assert is used to the check the code of the function itself

    def this(x: Int) = this(x, 1) // another constructor using the implicit constructor

    private def gcd(a : Int, b :Int) : Int = if (b ==0) a else gcd(b, a % b)
    // Remember there 3 ways we could calculate the global common denominator
    // 1: method below
    // 2: have the def num and den call gcd, in this case gcd is evaluated
    // 3: same as 2 but change to val instead of def, this way we evaluate at the point initialisation
    private val g = gcd(x,y)

    def numerator = x / g
    def denominator = y / g

    def add(that: Rational) =
      new Rational(
        numerator * that.denominator + that.numerator * denominator,
        denominator * that.denominator
      )

    def neg = new Rational(-numerator, denominator)

    def subtract(that: Rational) =  add(that.neg)

    def less(that: Rational) = numerator * that.denominator < that.numerator * denominator
    def max(that:Rational) = if (this.less(that)) that else this

    override def toString = numerator + "/" + denominator
  }
  val x = new Rational(1,2)
  x.numerator
  x.denominator

  val y = new Rational(2,3)
  x.add(y)

  println(x.subtract(y).subtract(x.neg))
  println(x.less(y))
  println(x.max(y))

//  val strange = new Rational(1,0)
//  println(strange.add(strange))
  println(new Rational(4))
}

{
  //Class and Substitutions

  class Rational(x: Int, y:Int) {
    require(y != 0, "denominator must be nonzero")

    def this(x: Int) = this(x, 1)

    private def gcd(a : Int, b :Int) : Int = if (b ==0) a else gcd(b, a % b)
    private val g = gcd(x,y)

    def numerator = x / g
    def denominator = y / g

    def + (that: Rational) = // Infix operators <3
      new Rational(
        numerator * that.denominator + that.numerator * denominator,
        denominator * that.denominator
      )

    def unary_- : Rational = new Rational(-numerator, denominator) //Prefix operator syntax, note the space between - and :

    def -(that: Rational) =  this + -that

    def <(that: Rational) = numerator * that.denominator < that.numerator * denominator
    def max(that:Rational) = if (this < that) that else this

    override def toString = numerator + "/" + denominator
  }

  val x = new Rational(1,2)
  val y = new Rational(2,3)
  println(x + y)
  println(x - y - -x)
  println(x < y)
  println(x max y)

/*
Precedence Order
(All letters)
|
^
&
< >
= !
:
+ -
* / %
(all other special characters)
a + b ^? c ?^ d less a ==> b | c
((a + b) ^? (c ?^ d)) less ((a ==> b) | c)
  */
}
