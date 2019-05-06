# Lazy Evaluation

---
## Streams


We've seen a number of immutable collections that provide powerful operations, in a particular for combinatorial search.

For instance, to find the second prime number between 1000 and 10000:

```
((1000 to 10000) filter isPrime)(1)
```

This is _much_ shorter than the recursive alternative:

```
def secondPrime(from: Int, to :Int) = nthPrime(from, to 2)
def nthPrime(from: Int, to: Int, n: Int): Int =
  if (from >= to) throw new Error("no prime")
  else if (isPrime(from))
    if (n == 1) from else nthPrime(from + 1, to, n - 1)
  else nthPrime(from + 1, to, n)
  ```

  But from a standpoint of performance the shorter solution is not very efficient.

  It constructs all prime numbers between 1000 and 10000 in a list, but only ever looks at the first two elements of that list.

  Reducing the upper bound would speed things up, but risks that we miss the second prime number all together.

  However, we can make the short code efficient by using a trick

  _Avoid computing the tail of a sequence until it is needed for the evaluation result (which might be never)_.

This idea is implemented in a new class, the Stream.

Streams are similar to lists, but their tail is evaluated only on demand.

Streams are define from a constant Stream.empty and a constuctor Stream.cons

e.g

```
val xs = Stream.cons(1, Stream .cons(2, Stream.empty))
```

They can be created like of other collections via object `Stream` as a factory.

The `toStream` can also convert a collection to a stream.

Let's rewrite a function that returns `(lo until hi).toStream` directly:

```
def streamRange(lo: Int, hi: Int): Stream[Int] =
  if(lo >= hi) Stream.empty
  else Stream.cons(lo, streamRange(lo + 1, hi))
```

The key difference between Lists and Streams is that the cons function is `#::` instead of `::`.

```
trait Stream[+A] extends Seq[A] {
  def isEmpty: Boolean
  def head: A
  def tail: Stream[A]
  //...
}

object Stream {
  def cons[T](hd: T, tl: => Stream[T]) = new Stream[T] {
    override def isEmpty: Boolean = false
    override def head = hd

    override def tail = tl
  }

  val empty = new Stream[Nothing] {
    override def isEmpty: Boolean = true

    override def head: Nothing = throw NoSuchElementException

    override def tails: Iterator[Seq[Nothing]] = throw NoSuchElementException
  }
}
```

---

## Lazy Evaluation

The proposed implementation suffers from a serious potential performance problem: If tail is called several times, the corresponding stream will be recomputed each time.

This problem can be avoided by storing the result of the first evaluation of tail and re-using the stored result instead of recomputing tail.

This optimistaion is sound, since in a purely functional language an expression produces the same result each time it is evaluated.

We call this scheme `lazy evaluation` (as opposed to `by-name evaluation` in the case where everything is recomputed, and `strict evaluation` for normal parameters and `val` definitions).

Haskell is a functional programming language that uses lazy eval by default.

Scala uses strict evaluation by default, but allows lazy evaluation of value definitions with `lazy val` form:

```scala
lazy val x = expr
```

 To adapt our definition of `Stream` to use lazy evaluation:

```
def cons[T](hd: T, tl: => Stream[T]) = new Stream[T] {
  override def head = hd
  lazy val tail = tl
  ...
}
```

---

## Computing with Infinite Sequences

You saw that all elements of a stream except the first one are computed only when they are needed to produce a result.

This opens up the possibility to define infinite streams!!

For instance, here is the stream of all integers starting from a given number:

```
def from(n: Int): Stream[Int] = n #:: from(n+1)
```

The stream of all natural numbers:

```
def from(n: Int): Stream[Int] = n #:: from(n + 1)

val nats = from(0)

val m4s = nats map (_ * 4)
```

The Sieve of Eratosthenes is an ancient technique to calculate prime numbers.

The idea is a follows:

* Start with all integers from 2, the first prime number.
* Eliminate all multiples of 2.
* The first element of the resulting list is 3, a prime number.
* Eliminate all multiples of 3.
* Iterate forever. At each step, the first number i
n the list is a prime number and we eliminate all its multiples.

```
def sieve(s: Stream[Int]): Stream[Int] =
s.head #:: sieve(s.tail filter (_ % s.head  != 0))

val primes = sieve(from(2))

(primes take 100).toList
```

Our previous algorithm for square roots used a `isGoodEnough` test to tell when to terminate the iteration.

With streams we can now express the concept of a converging sequence without having to worry about when to terminate it:

```
def sqrtStream(x: Double): Stream[Double] = {
  def improve(guess: Double) = (guess + x / guess) / 2
  lazy val guesses: Stream[Double] = 1 #:: (guesses map improve)
  guesses
}

def isGoodEnough(guess: Double, x : Double) =
math.abs((guess * guess - x) / x) < 0.0001

(sqrtStream(4) filter (isGoodEnough(_, 4))).take(10).toList
```

---

## Case Study : The Water Pouring problem

Water pouring puzzles (also called water jug problems or measuring puzzles) are a class of puzzle involving a finite collection of water jugs of known integer capacities (in terms of a liquid measure such as litres or gallons). Initially each jug contains a known integer volume of liquid, not necessarily equal to its capacity. Puzzles of this type ask how many steps of pouring water from one jug to another (until either one jug becomes empty or the other becomes full) are needed to reach a goal state, specified in terms of the volume of liquid that must be present in some jug or jugs.

```

class Pouring(capacity: Vector[Int]){
  //States

  type State = Vector[Int]
  val initalState = capacity map (_ => 0)

  //Moves
  trait Move {
    def change(state: State): State
  }
  case class Empty(glass: Int) extends Move {
    override def change(state: State): State = state updated (glass, 0)
  }
  case class Fill(glass: Int) extends Move {
    override def change(state: State): State = state updated (glass, capacity(glass))
  }
  case class Pour(from: Int, to: Int) extends Move {
    override def change(state: State): State = {
      val amount = state(from) min (capacity(to) - state(to))
      state updated(from, state(from) - amount) updated(to, state(to) + amount)
    }
  }

  val glasses = capacity.indices

  val moves =
    ( for (g <- glasses) yield Empty(g)) ++
    ( for (g <- glasses) yield  Fill(g)) ++
    ( for (from <- glasses; to <- glasses; if from != to) yield Pour(from, to))

  //Paths

  class Path(history: List[Move], val endState: State) {

//    private def trackState(xs: List[Move]): State = xs match  {
//      case Nil => initalState
//      case move :: xs1 => move change trackState(xs1)
//    }

//    def endState: State = (history foldRight initalState) (_ change _) //trackState(history)
    def extend(move: Move) = new Path(move :: history, move change endState)
    override def toString: String = (history.reverse mkString " ") + "--> " + endState
  }

  val initalPath = new Path(Nil, initalState)

  def from(paths: Set[Path], explored: Set[State]): Stream[Set[Path]] =
    if (paths.isEmpty) Stream.empty
    else {
      val more = for {
        path <- paths
        next <- moves map path.extend
        if !(explored contains next.endState)
      } yield  next
      paths #:: from(more, explored ++ (more map (_.endState)))
    }

  val pathSets = from(Set(initalPath), Set(initalState))

  def solutions(target: Int): Stream[Path] =
    for {
      pathSet <- pathSets
      path <- pathSet
      if path.endState contains target
    } yield path
}

val problem = new Pouring(Vector(4, 9))
problem.moves
problem.pathSets.take(3).toList
problem.solutions(6)
```

In a program of the complexity of the pouring program, there are many choices to be made.

Choice of representations.

* Specific classes for moves and paths, or some encoding?
* Object-orientated methods, or naked data structures with functions?

The present elaboration is just one solution, and not necessarily the shortest one.

### Guiding Principles for Good Desing

* Name everything you can.
* Put operations into natural scopes
* Keep degrees of freedom for future refinements
