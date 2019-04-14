/*
Other Sequences

Vectors

Move evenly balanced access patterns than lists

Vectors are created analogously to Lists

val nums = Vector(1,2,3,4)
They support the same operations as lists, with the exception of :: (cons)
Instead of x :: xs there is
x +: xs -> creates a new vector with the leading element x, followed by all the elements of xs
xs :+ x -> Creates a new vector with trailing element x, preceded by all elements of xs

Not the : always point to the sequence
 */
val nums = Vector(1,2,3,4)
5 +: nums
nums :+ 5


/*
Collection Hierarchy

A common base class of List and Vector is Seq, the class of all sequences

Seq itself is a subclass of Iterable
*/

/*
Arrays and Strings

Arrays and Strings support the same operations as Seq and can implicitly be
converted to sequences where needed.

(They cannot be subclasses of Seq because they come from Java)

 */

val xs: Array[Int] = Array(1,2,3,4)

xs map (2 * _)

val ys: String = "Hello World"

ys filter (_.isUpper)

/*
Ranges

Another simple kind of sequences is the range

it represents a sequence of evenly spaced integers

Three operators:
to (inclusive), until (exclusive), by (to determine step value)

Ranges are represented as single objects with three fields; lower
bound, upper bound, step value
 */

val r : Range = 1 to 5

val s = 1 until 5

1 to 10 by 3

(6 to 1 by -2).toList

/*
Some more operations for sequences
 */

r exists (_ == 4)
r forall (_ > 4)
r zip s
(r zip s).unzip

List(List(1), List(2)).flatMap(_ .map(_ * 2))
ys flatMap (c => List(".", c))

r.sum
r.product
r.max
r.min

val M = 10
val N = 10

(1 to M) flatMap (x => (1 to N) map ( y => (x, y)) )

def isPrime(n : Int): Boolean = (2 until n) forall (d => n % d != 0)

isPrime(3)


/*
Handling Nested Sequences

We can extend the usage of higher order functions on sequences to
do many calculations which are usually expressed using nested loops

Example: Given a positive integer n, find all pairs
of positive integers i and j, with 1 <= j < i < n such that i + j is
prime.

For example, if n = 7 the sought pairs are
i | 2 3 4 4 5 6 6
j | 1 2 1 3 2 1 5
+ | 3 5 5 7 7 7 11
where + = i + j
 */

/*
A natural way to do this is th

-> Generate the sequence of all pairs of integers (i, j) such that
1 <= j < i < n
-> Filter the pairs for which i + j

One natural way to generate the sequece of pairs is to

-> Generate all the integers i between 1 and n (excluded).
-> For ech integer i, generate the list of pairs (i, 1), .., (i, i-1)

This can be achieved by combining until and map
 */

val n = 7

(1 until n) flatMap (i =>
  (1 until i) map (j => (i, j))) filter {case (x, y) => isPrime(x + y)}


/*
For Expressions

Higher order functions such as map, flatMap or filter provide
powerful constructs for manipulating lists

But Sometimes the level of abstraction required by these functions
make the program difficult to understand.

In this case, Scala's for expression notation can help
 */
/*
Let persons be a list of elements of class Person, with fields name and
age
 */
case class Person(name: String, age:Int)

val persons = List(Person("Joe", 21), Person("Olga", 10))
//To obtain the name of persons over 20 yr old, you can write
for (p <- persons if p.age > 20) yield p.name

// which is equivalent to
persons filter (p => p.age > 20) map ( p => p.name)

/*
Syntax of For

A for expression is of the form

for(s) yield e

where s is a sequence of generators and filters, and e is an expression
whose value is returned by an iterations
-> A generator is of the form p <- e where p is a pattern and e an
expression whose value is a collection
-> A filter is of the form if f where f is a boolean expression
-> The sequence must start with a generator
-> If there are several generators in the sequence, the last
generators vary faster than the first

Instead of (s), braces {s} can also be used, adn the the sequence of
generators and filters can be written on multiple lines
with requiring semicolons
 */

for {
  i <- 1 until n
  j <- 1 until i
  if isPrime(i + j)

} yield (i, j)

def scalarProduct(xs: List[Double], ys: List[Double]): Double = (for {
  (x, y) <- xs zip ys
} yield x * y).sum


/*
Sets are close to sequences
however:
Sets are unordered, can't have duplicates
the fundamental operation on sets is contains element

xs contains x
 */

/*
N-Queens

The eight queens problem is to place eight queens on a chessboard
so that  no queen is threatened by another

- In other words, there can't be two queens in the same row,
column, or diagonal.

We now develop a solution for a chessboard of any size, not just 8

One way to solve the problem is to place a queen on each row.

One we have placed k - 1 queens, one must place the kth queen
in a column where it's not "in check" with any other queen on the
board.
 */

/*
We can solve this problem with an algorithm

- Suppose that we have already generated all the solutions
consisting of placing k - 1 queens on a board of size n
- Each solution is represented by a list (of length k -1) containing
the numbers of columns (between 0 and n -1)
- The column number of the queen in the k - 1th row comes first
in the list, followed by the column number of the queen in row
k - 2, etc.
- The solution set is thus represented as a set of lists, with one
element for each solution
- Now to place the kth queen, we generate all possible
extensions of each solution preceded by a new queen.
 */

def queens(n : Int): Set [List[Int]] = {

  def isSafe(col: Int, queens: List[Int]): Boolean = {
    val row = queens.length
    val queensWithRow = (row - 1 to 0 by -1) zip queens
    queensWithRow forall {
      case (r, c) => col != c && math.abs(col - c) != row - r
    }

  }
  //!(queens contains col)

  def placeQueens(k: Int): Set[List[Int]] =
    if (k == 0 ) Set(List())
    else
      for {
        queens <- placeQueens(k - 1)
        col <- 0 until n
        if isSafe(col, queens)
      } yield col :: queens

  placeQueens(n)
}

def show(queens: List[Int]) = {
  val lines =
    for (col <- queens.reverse)
      yield Vector.fill(queens.length)("* ").updated(col, "X ").mkString
  "\n" + (lines mkString "\n")

}
(queens(4) map show) mkString "\n\n"


/*
Maps

Map[Key, Value]
They are iterables and and functions
 */

val capitalOfCountry = Map("US" -> "Washington", "Switzerland" -> "Bern")

capitalOfCountry("US")

//capitalOfCountry("Andorra") --> NoSuchElement exception

capitalOfCountry get "Andorra"
capitalOfCountry get "US"

//Note that options are case classes

def showCapital(country: String) = capitalOfCountry.get(country) match {
  case Some(capital) => capital
  case None => "missing data"
}

showCapital("US")

val fruits = List("apple", "pear", "orange")
fruits sortWith(_.length < _.length)
fruits.sorted

fruits groupBy(_.head)

class Polynominal(val term0: Map[Int, Double]){

  def this(bindings: (Int, Double)*) = this(bindings.toMap)

  val terms = term0 withDefaultValue 0.0

  def + (other: Polynominal) = new Polynominal(terms ++ (other.terms map adjust))

  def adjust(term: (Int, Double)): (Int, Double) = {
    val (exp, coeff) = term
    exp -> (coeff + terms(exp))
//  terms get exp match {
//  case Some(coeff1) => exp -> (coeff + coeff1)
//  case None => exp -> coeff
//  }
  }

  def add (other: Polynominal) = new Polynominal(
    (other.terms foldLeft terms)(addTerm) // More efficient than +
  )

  def addTerm(terms: Map[Int, Double], term: (Int, Double)):Map[Int, Double] ={
    val (exp, coeff) = term
    terms + (exp -> (terms(exp) + coeff))
  }

  override def toString =
    (for((exp, coeff) <- terms) yield coeff + "x^" + exp) mkString " + "

}

val p1 = new Polynominal(Map(1 -> 2.0, 3 -> 4.0, 5 -> 6.2))
val p2 = new Polynominal(Map(0 -> 3.0, 3 -> 7.0))

p1 + p2

new Polynominal((1, 2.0), (5, 5.0))

s zip s
