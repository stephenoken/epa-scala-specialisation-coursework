
/*

 */

//trait Expr {
//  def isNumber: Boolean
//  def isSum :Boolean
//  def numValue: Int
//  def leftOp: Expr
//  def rightOp: Expr
//
//}
//
//class Number(n: Int) extends Expr {
//  def isNumber: Boolean = true
//  def isSum: Boolean = false
//  def numValue: Int = n
//  def leftOp: Expr = throw new Error("Number.leftOp")
//  def rightOp: Expr = throw new Error("Number.rightOp")
//}
//
//class sum(e1: Expr, e2: Expr) extends Expr {
//  override def isNumber: Boolean = false
//
//  override def isSum: Boolean = true
//
//  override def numValue: Int = throw new Error("Sum.numValue")
//
//  override def leftOp: Expr = e1
//
//  override def rightOp: Expr = e2
//}


// A tedious way of evaluation
//def eval(e: Expr): Int = {
//  if (e.isNumber) e.numValue
//  else if(e.isSum) eval(e.leftOp) + eval(e.rightOp)
//  else throw new Error(s"Unknown expression $e" )
//}


//Casting exists in Scala but is discouraged


// Solutiuon 1

//trait Expr {
//  def eval: Int
//}
//
//class Number(n: Int) extends Expr {
//  def eval: Int = n
//}
//
//class Sum(e1: Expr, e2: Expr) extends Expr {
//  def eval: Int = e1.eval + e2.eval
//}
// If we need to add a new method to the trait then we'll have to potentially make changes in all implementations
// It's harder to simplify an expression e.g a * b + a * c => a * (b + c)

/*
Pattern Matching for decomposition

The sole purpose of the test and acessor functions is to reverse the construction process

- Which subclass was used
- What were the arguments of the constructor


Done in the way of case class definitions, which is similar to a normal class definition, except
that it is preceded by the modifier case. For example:

trait Expr
case class Number(m: Int) extends Expr
case class Sum(e1: Expr, e2: Expr) extends Expr

Like before, this defines a trait Expr, and two concrete subclasses Number and Sum

It also implicitly defines companion objects with apply methods

object Number {
  def apply(n: Int) = new Number(n)

  }

  object Sum {
    def apply(e1: Expr...

  }

so you can write Number(1) instead of Number(1)

However, these classes are now empty. So how can we access their components

Pattern matching is a generalisation of switch from C/Java to class hierarchies

It's expressed in Scala using keywords match

Example

def eval(e: Expr): Int = e match {
  case Number(n) => n
  case Sum(e1, e2) => eval(e1) + eval(e2)
}

Rules:

- match is followed by a sequence of cases, pat => expr
- Each case associates an expression expr with a pattern pat.
- A MatchError exception is thrown if no pattern matches the value of the selector

Patterns are constructed from:
- constructors, eg Number, Sum
- variables e.g n. e1. e2
- wildcard patterns
- constants, e.g 1, true

Variables always begin with a lowercase letter
The same variable name can only appear once in a pattern. So, Sum(x, x) is not a legal pattern
Names of constants begin with a capital letter, with the exception of the reserved words null, true, false

What do patterns match?

- A constructor pattern C(p1...pn) matches all the value of type C(or a subtype) that have been
constructed with arguments matching the pattern p1...pn

- A variable pattern x matches any value, and binds the name of the variable to this value

- A constant pattern c matches values that are equal to c ( in the sense of ==)



It is possible to define the evaluation function as a method of the base trait

Example :
 */ {
  trait Expr {
    def eval: Int = this match {
      case Number(n) => n
      case Sum(e1, e2) => e1.eval + e2.eval
    }

  }

  case class Number(n: Int) extends Expr
  case class Sum(e1: Expr, e2: Expr) extends Expr
  case class Prod(e1: Expr, e2: Expr) extends Expr
  case class Var(x: String ) extends Expr

  object exprs {

    def show(e: Expr): String = e match {
      case Number(n) => s"$n"
      case Var(x) => x
      case Sum(e1, e2) => s"${show(e1)} + ${show(e2)}"
      case Prod(e1: Sum, e2 :Sum) => s"(${show(e1)}) * (${show(e2)})"
      case Prod(e1: Sum, e2) => s"(${show(e1)}) * ${show(e2)}"
      case Prod(e1, e2) => s"${show(e1)} * ${show(e2)}"
    }
  }

  val t1 = Sum(Sum(Number(2), Number(4)), Number(3))
  val t2 = Sum(Prod(Number(2), Var("x")), Var("y"))
  val t3 = Prod(Sum(Number(2), Var("x")), Var("y"))
  val t4 = Prod(Sum(Number(2), Var("x")), Sum(Var("y"), Var("z")))


  println (exprs.show(t1))
  println(exprs.show(t2))
  println(exprs.show(t3))
  println(exprs.show(t4))
}