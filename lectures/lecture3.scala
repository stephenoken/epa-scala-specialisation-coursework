abstract class IntSet {
  def incl(x: Int): IntSet
  def contains(x: Int): Boolean
  def union(other: IntSet): IntSet
}


class NonEmpty(elem: Int, left: IntSet, right: IntSet) extends IntSet {
  override def contains(x: Int): Boolean = {
    if (x < elem) left contains x
    else if (x > elem) right contains x
    else true
  }

  override def incl(x: Int): IntSet =
    if (x < elem) new NonEmpty(elem, left incl x, right)
    else if (x > elem) new NonEmpty(elem, left, right incl x)
    else this

  override def union(other: IntSet): IntSet =
    ((left union right) union other) incl elem

  override def toString: String = s"{$left $elem $right}"

}

object Empty extends IntSet { // Singleton object only created when first called
  override def contains(x: Int): Boolean = false
  override def incl(x: Int): IntSet = new NonEmpty(x, Empty, Empty)

  override def union(other: IntSet): IntSet = other

  override def toString: String = "."

}


//^ persistent data structures


val t1 = new NonEmpty(3, Empty, Empty)
val t2 = t1 incl 4
val t3 = t2 incl 6

t3 contains 5
t3 contains 4


//object Hello {
//  def main(args[String]) = println("Hello World")
//}
Empty contains 1
// -> [1/x] [Empty/this] false

new NonEmpty(7, Empty, Empty) contains 7


import funsets.FunSets.Set // Just set
import funsets.FunSets.{union, Set} // union and set
import funsets.FunSets._ //Everything

val s1 = singletonSet(5)

printSet(s1)


//Scala like java is a single inheritance  language

//trait is like an abstract class

trait Planar {
  def height: Int
  def width: Int
  def surface = height * width
}

//class Square extends Shape with Planar with Movable
// Traits can't have parameters

//Types

//Any  => the base type of all types
//     => has methods 'equals', toString
// AnyRef => The base type of all reference types;
//            Alias of java.lang.object
// AnyVal  => The base types of any primitives from java

// Nothing => is at the bottom of Scala's type hierarchy
//        There is no value of type Nothing. Also the

def error(msg: String)  = throw  new Error(msg)
//error("HH")
/*
Scala.Null
is a subtype of all types except AnyVal
 */
val x = null
val y: String = x
//val z: Int = null can't do this

if(true) 1 else false // We get AnyVal since they both fall under AnyVal



//Polymorphisms
/* Cons
In computer programming, consis a fundamental function in most dialects
of the Lisp programming language. cons constructs memory objects which
hold two values or pointers to values. These objects are referred to as
(cons) cells, conses, non-atomic s-expressions ("NATSes"), or (cons)
pairs. In Lisp jargon, the expression "to cons x onto y" means to
construct a new object with (cons x y). The resulting pair has a left
half, referred to as the car (the first element, or content of address
register), and a right half (the second element, or content of decrement
register), referred to as the cdr.

It is loosely related to the object-oriented notion of a constructor,
which creates a new object given arguments, and more closely related
to the constructor function of an algebraic data type system.

The word "cons" and expressions like "to cons onto" are also part of a
more general functional programming jargon. Sometimes operators that
have a similar purpose, especially in the context of list processing,
are pronounced "cons". (A good example is the :: operator in
ML, Scala, F# and Elm or the : operator in Haskell, which adds an
element to the beginning of a list.)

 */


//List(1,2,3)

trait List[T] {
  def isEmpty: Boolean
  def head: T
  def tail: List[T]
}

class Cons[T](val head: T, val tail: List[T]) extends List[T] {
  override def isEmpty: Boolean = false
}

class Nil[T] extends List[T] {
  override def isEmpty: Boolean = true
  def head: Nothing = throw new NoSuchElementException("Nil.Head")
  def tail: Nothing = throw new NoSuchElementException("Nil.Tail")
}

def singleton[T](elem: T) = new Cons[T](elem, new Nil[T])
val si1 = singleton[Int](1)
val si2 = singleton(true)

/*
Type paramters do not affect evaluation in scala
We can assume that all type parameters and type arguments are removed before evaluating
the program

This is also called type erasure

Which is used in OCaml and Haskell

However C++ F# do keep the parameters
 */

/*
Scala has Polymorphism
 */

def nth[T](n: Int, xs: List[T]): T =
  if(xs.isEmpty) throw new IndexOutOfBoundsException
  else if (n == 0) xs.head
  else nth(n - 1, xs.tail)

val list = new Cons(1, new Cons(2, new Cons(3, new Nil)))

nth(2, list)
nth(5, list)
