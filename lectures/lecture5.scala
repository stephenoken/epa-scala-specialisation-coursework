/*
xs.length -> The number of elements of xs
xs.last -> the last element
xs.init -> all the ele
xs take n
xs drop n
xs(n)
 */

val xs = List(1,2,3)

xs.length
xs.last
xs.init
xs take 1
xs drop 2
xs(1)

val ys = List(9)

xs ++ ys

xs.reverse
xs updated (1, 1000) // still immutable

xs indexOf 2
xs contains 3


def last[T](xs: List[T]): T = xs match { // Not as efficient as head
  case List() => throw new Error("last of empty")
  case List(x) => x
  case y :: ys => last(ys)
}

last(xs)

def init[T](xs: List[T]): List[T] = xs match {
  case List() => throw new Error("Last of empty list")
  case List(_) => List()
  case y :: ys => y :: init(ys)
}

init(xs)

def concat[T](xs: List[T], ys: List[T]): List[T] = xs match {
  case List() => ys
  case z :: zs => z :: concat(zs, ys)
}

concat(xs, ys)

def reverse[T](xs: List[T]): List[T] = xs match {
  case List() => xs
  case y :: ys => reverse(ys) ++ List(y)
} // Can it be done better

reverse(xs)

def removeAt[T](xs: List[T], n : Int): List[T] = (xs take n) ::: (xs drop n + 1)

//  xs match {
//  case List() => List()
//  case y :: ys => if(xs.indexOf(y) == n) ys
//  else y :: removeAt(ys, n)
//}

removeAt(xs, 1)

// Merge Sort

// Note that this only works for Ints
//def msort(xs: List[Int]): List[Int] = {
//  val n = xs.length / 2
//  if (n == 0) xs
//  else {
//    def merge(xs: List[Int], ys: List[Int]): List[Int] = (xs, ys) match {
//      case (Nil, ys1) => ys1
//      case (xs1, Nil) => xs1
//      case (x :: xs1, y :: ys1) => if (x < y) x :: merge(xs1, y :: ys1) else
//        y :: merge(x :: xs1, ys1)
//    }
//    val (fst, snd) = xs splitAt n
//    merge(msort(fst), msort(snd))
//  }
//}
//
//msort(List(4,1,8,4,3))


//def msort[T](xs: List[T])(lt : (T, T) => Boolean): List[T] = {
//  val n = xs.length / 2
//  if (n == 0) xs
//  else {
//    def merge(xs: List[T], ys: List[T]): List[T] = (xs, ys) match {
//      case (Nil, ys1) => ys1
//      case (xs1, Nil) => xs1
//      case (x :: xs1, y :: ys1) => if (lt(x, y)) x :: merge(xs1, y :: ys1) else
//        y :: merge(x :: xs1, ys1)
//    }
//    val (fst, snd) = xs splitAt n
//    merge(msort(fst)(lt), msort(snd)(lt))
//  }
//}
val nums = List(4,-4,7,1,0,10, -1000)
//msort(List(4,1,8,4,3))((x, y) => x < y)
//
val fruits = List("Bannana", "Apple", "Pineapple", "Orange")
//
//msort(fruits)((x, y) => x.compareTo(y) < 0) // It's a good idea to have the function in the last parameter list as the compiler should be able to infer the types

def msort[T](xs: List[T])(implicit ord: Ordering[T]): List[T] = {
  // Implicit parameters is where the compiler synthesise one
  val n = xs.length / 2
  if (n == 0) xs
  else {
    def merge(xs: List[T], ys: List[T]): List[T] = (xs, ys) match {
      case (Nil, ys1) => ys1
      case (xs1, Nil) => xs1
      case (x :: xs1, y :: ys1) => if (ord.lt(x, y)) x :: merge(xs1, y :: ys1) else
        y :: merge(x :: xs1, ys1)
    }
    val (fst, snd) = xs splitAt n
    merge(msort(fst)(ord), msort(snd)(ord))
  }
}

msort(nums)
msort(fruits)(Ordering.String)
/*
Rules for implicit parameters

Say a function takes an implicit parameter of type T .
The compiler will search an implicit definition that
-> is marked implicit
-> has a type compatible with T
-> is visible at the point of the function call, or is defined in a
companion object associated with T

If there is a single (most specific) definition, it will be taken
as actual argument for the implicit parameter

Otherwise it's an error
*
 */

nums.map(_ + 5)

//def squareList(xs: List[Int]): List[Int] = xs match {
//  case Nil => List()
//  case z :: zx => z * z :: squareList(zx)
//}

def squareList(xs: List[Int]): List[Int] = xs map (x => x * x)

nums filter (x => x % 2 == 0)

nums filterNot (x => x % 2 == 0)

nums partition (x => x % 2 == 0)

nums takeWhile (x => x != 0)

nums dropWhile (_ != 0)

nums span (_ != 0)

def pack[T](xs: List[T]): List[List[T]] = xs match {
  case Nil => Nil
  case x :: xs1 =>
    val (zs, as) = xs1 span (_ == x)
    (x :: zs) ::  pack(as)
}

pack(List("a", "a", "a", "b", "c", "c", "a"))


def encode[T](xs : List[T]): List[(T, Int)] =
  pack(xs) map (ys => (ys.head, ys.length))

encode(List('a', 'a', 'a', 'b', 'c', 'c', 'a'))


def sum(xs : List[Int]): Int = (0 :: xs) reduceLeft(_ + _)
def product(xs : List[Int]): Int = (1 :: xs) reduceLeft(_ * _)


def concat1[T](xs : List[T], ys: List[T]): List[T] =
  (xs foldRight ys) ((x, y) => x :: y)

concat1(nums, nums.reverse)
// Laws of Concat

//(xs ++ ys) ++ zs == xs ++ (ys ++ zs)
//xs ++ Nil == xs
//Nil ++ xs == xs

//Q: How can we prove properties like these?
//A: By structural induction

/*
Natural Induction
Recall the principle of proof by natural induction
To show a property P(n) for all the integers n >= b
- Show that we have P9b) (base case)
- for all intergers n >= b show the induction step
  if one has P(n), then one also has P(n + 1)
 */

/*

 */
