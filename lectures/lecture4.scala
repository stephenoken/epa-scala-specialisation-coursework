/*
A pure  oop language is one in which every value is an object

if the language is based on classes, this means that the type of each value is a class

is cala a pure oop language?

At first glance there seem to be some exceptions: primitive types, functions

But, let's look closer


Conceptually, types such as Int or Boolean do not receive special treatment in Scala.

 */


/*
abstract class  Boolean {
  def ifThenElse[T](t: => T, e: => T): T


  def && (x: => Boolean): Boolean = ifThenElse(x, false)
  def || (x: => Boolean): Boolean = ifThenElse(true, x)
  def unarary_! : Boolean = ifThenElse(false, true)

  def == (x: Boolean): Boolean = ifThenElse(x, x.unarary_!)
  def != (x: Boolean): Boolean = ifThenElse(x.unarary_!, x)

  def < (x: Boolean): Boolean = ifThenElse(false, x)

}

object true extends Boolean {
  def ifThenElse[T](t: => T, e: => T) = t
}

object false extends Boolean {

  def ifThenElse[T](t: => T, e: => T) = e
}
*/

/*
Java has overloading!!
 */

// Peano Numbers
{

  abstract class Nat {
    def isZero: Boolean

    def predecessor: Nat

    def successor: Nat = new Succ(this)

    def +(that: Nat): Nat

    def -(that: Nat): Nat
  }

  object Zero extends Nat {
    override def isZero: Boolean = true

    override def predecessor: Nat = throw new Error("Only Non negative numbers allowed")

    override def +(that: Nat): Nat = that

    override def -(that: Nat): Nat = if (that.isZero) this else this.predecessor

  }

  class Succ(n: Nat) extends Nat {

    override def isZero: Boolean = false

    override def predecessor: Nat = n

    override def +(that: Nat): Nat = new Succ(n + that)

    override def -(that: Nat): Nat = if (that.isZero) this else n - that.predecessor

  }

  val n1 = Zero
  val n2 = new Succ(Zero)

//  n2 + n1 - n2 - n2
}


/*
Functions are treated as objects in scala

The function type A => B is an abbreviation of
scala.function[A,B], which is roughly defined as
 */
trait Function1[A, B] {
  def apply(x: A): B
}

//There's a limit of function parameters of 22 params

(x: Int) => x * x

//is expanded to

new Function1[Int, Int] {
  def apply(x: Int): Int = x * x
}



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



object List {
  // List() = List.apply()
  def apply[T](): List [T] = new Nil[T]
  // List(1, 2) = List.apply(1, 2)
  def apply[T](x1: T, x2: T): List[T] =  new Cons[T](x1, new Cons[T](x2, new Nil[T]))
}

List()
print(List(1,2).head)


/*
Principal forms of polymorphism

* subtyping
* generics

Tow main areas of polymorphism are:
-> bounds
-> variance


Type Bounds

Consider the method assertAllPos which
- takes an IntSet
- returns the InSet itself if all elements are positive
- throws an exception otherwise

What would be the best type you can give to assertAllPos? Maybe:

def assertAllPos(s: IntSet): IntSet

In most situations this is fine, but can one be more precise

One might want to express that assertAll Pos takes Empty sets to Emptry sets and NonEmpty sets
to NonEmpty sets

A way to express this is:

def assertAllPos[S <: IntSet)(r: S) S =

Here '<: IntSet' is an upper bound of the type parameter S:
It means that S can be instantiated only to types that conform to InSet.

Generally, the notation
- S <: T means: S is a subtype of T, and
- S >: T means: S is a supertype of T, or T is a subtype of S
 */

/*
Lower bounds

you can also use a lower bound for a type variable
Example :
[s >: NonEmpty]

introduces a type parameter S that can range only over supertypes of NonEmpty

So S could be one of NonEmpty, IntSet, AnyRef or Any

We will see later on iin this session where lower bounds are useful
 */

/*
Mixed bounds

Finally, it is also possible to mix a lower bound with an upper bound.

For instance,

[S >: NonEmpty <: IntSet]

would restrict S any type on the interval between NonEmpty and IntSet
 */

/*
Covariance

There;s another interaction between subtyping and type parameters we need to consider. Given:
NonEmpty <: IntSet

is

List[NonEmpty <: List[IntSet]   ?

Intuitively, this makes sense: A list of non-empty sets is a special case of a list of arbitrary sets.

We call types for which this relationship holds covariant because their subtyping relationship varies with
the type parameter.

Does covariance make sense for all types, not just for Lists?

 */

/*
Arrays

For perspective, let's look at arrays in Java and C#

Reminder:
- An array of T elements is written T[] in Java
- In Scala we use parameterised type syntax Array[T] to refer to the same type

Arrays in Java are covariant, so one would have:

NonEmpty[] <: IntSet[]
 */

/*
But covariant array typing causes problems.

To see why, consider the java code below.

NonEmpty[] a = new NonEmpty[]{new NonEmpty(1, Empty, Empty)}
InSet[] b = a
b[0] = Empty
NonEmpty s = a [0]

It looks like we assigned in the last line an Empty set to a variable of type NonEmpty!!

What went wrong?
We have two pointers pointing to the a array and b, since, it is a supertype and can change NonEmpty
to Empty. Thus breaking the typing. Ergo we get an ArrayStoreException at runtime.
To guard against this Java
assigns a tag of the subtype of an array and this sort of defeats the purpose of covariance.

This is a hangover from the earlier versions of java that didn't have generics (pre Java 5)

 */

/*
The Liskov Substitutions Principle

The following principle, stated by Baragra Liskov, tells us when a type can be subtype of another.

If a <: b, then everything one can to do with a value of type b one should also be able to do with a value of type a.

[The actual definition used is a bot more formal. It says:

Let q(x) be a property provable about objects x of type b.
Then q(y) should be provable for objects y of type a where a <: b
 */

/*
In scala Arrays are not covariant, which prevents to above problem happening 
 */

/*
Variance

You have seen in the previous sessions that some types should be covariant whereas others should not

Roughly speaking, a type that accpets mutations of its elements should not be be covariant.

But immutable types can be covariant, if some conditions on methods are met

 */

/*
Say C[T] is a parameterised type and A,B are types such that A <: B.

In general, there are three possible relationships between C[A] and C[B]:

C[A] <: C[B] // C is covariant
C[A] >: C[B] // C is contravariant
neither C[A] nor C[B] is a subtype of the other // C is nonvariant


Scala lets you declare the variance of a type by annotating the type parameter.

class C[+A] { ... } C is covariant
class C[-A] { ... } C is contravariant
class C[A] { ... } C is nonvariant

*/

/*
Exercise
Say you have two function types:

type A = IntSet => NonEmpty
type B = NonEmpty => IntSet

What is the relationship?

A <: B // this is because type A satisfies the contract of type B

 */
{
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

  trait List[+T] {
    def isEmpty: Boolean
    def head: T
    def tail: List[T]
    def prepend [U >: T] (elem: U): List[U] = new Cons(elem, this)
  }

  class Cons[T](val head: T, val tail: List[T]) extends List[T] {
    override def isEmpty: Boolean = false
  }

  object Nil extends List[Nothing] {
    override def isEmpty: Boolean = true
    def head: Nothing = throw new NoSuchElementException("Nil.Head")
    def tail: Nothing = throw new NoSuchElementException("Nil.Tail")
  }

  object test {
    val x: List[String] = Nil // Check the covariance
    def f(xs: List[NonEmpty], x: Empty) = xs prepend x //Should return IntSet as that is a common supertype
  }
}
