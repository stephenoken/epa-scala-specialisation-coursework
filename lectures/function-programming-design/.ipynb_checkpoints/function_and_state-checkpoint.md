# Functions and State

Principles of Reactive Programming
---

Until now our programs our programs have been side effect free. Therefore, the concept of _time_ wasn't important.
For all programs that terminate, any sequence of actions would have given the same results.
This was also reflected in the substitution model of computations.

 Programs can be evalutated by rewriting.
The most important rewrite rule covers function applications:
```
def f(x1,..., xn) = B;, ... f(v1,...,vn)
def f(x1, ..., xn) =B; ... [v1/x1, ..., vn/xn]B
```

### Stateful objects

In proactive, objects with state are usually represented by objects that have some variable members.

**Example**: here is a class modeling a bank account.
```
class BankAccount {
  private var balance = 0
  def deposite(amount: Int): Unit = {
    if (amount > 0) balance = balance + amount
  }

  def withdraw(amount: int): Int =
    if (0 < amount && amount <= balance) {
      balance - balance - amount
      balance
    } else throw new Error("Insufficient Funds")
}
```

The class `BankAccount` defines a variable `balance` that contains the current balance of the account.

The methods `deposit` and `withdraw` change the value of the `balance` through assignments.

Note that balance is private in the BankAccount class, it therefore cannot be accessed from outside the class.

To create bank accounts, we use the usual notation for object creation.


A `var` does not necessarily make an object Stateful.

---

# Identity & Change

Assignment poses the new problem of deciding whether two expressions are the same.

When one excludes assignments and one writes:
`val x = E; val y = E`

where `E` is an arbitrary expression, then it is reasonable to assume that `x` and `y` are the same. That is to say that we could have also written:

`val z = E; val y = x`
(This property is usually call **referential transparency**)


### Operation Equivalence

To deal with questions where referential transparency is not clear. We must specify what is meant by "the same".

The precise meaning of "being the sane" is defined by the property of **operations equivalence**.

In a somewhat informal way, this property states as follows.

Suppose we have 2 different definitions x and y.

x and y are operationally equivalent if no possible test can distinguish between them.

To test if `x` and `y` are the same, we must
* Execute the definitions followed by an arbitrary sequence `f` of operations that involves `x` an `y`, observing the possible outcomes.

```
val x = new BankAccount   val x = new BankAccount
val y = new BankAccount   val y = new BankAccount
f(x,y)                    f(x,x)
```

* Then, execute the definitions with another sequence `S'` obtained by renaming all occurrences of `y` by `x` in `S`.
* If the results are different, then the expressions `x` and `y` are certainly different.
* On the other hand, if all possible pairs of sequences `(S, S')` produce the same result, then `x` and `y` are the same.

### Assignment and Substitution Model

With the use of assignments the model of computation by substitution cannot be used.

Indeed, according to this model, one can always replace the name of a value by the expression that defines it. For example, in

```
val x = new BankAccount
val y = x
```

the `x` in the definition of `y` could be replaced by `new BankAccount`.

---

## Loops

**Proposition:** Variables are enough to model all imperative programs.

But what about control statements like loops?

We can model them using functions.

### While and Repeat

**Example:** Here is a Scala program that uses a while loop:

```
def power(x: Double, exp: Int): Double = {
  var r = 1.0
  var i = exp
  while (i > 0) {r =r * x; i = i - 1}
  r
}

```

In Scala, `while` is a keyword.
But how could we define while using a function (call it WHILE)?

```
def WHILE(condition: => Boolean)(command: => Unit): Unit  =
  if(condition) {
    command
    WHILE(condition)(command)
  }
  else ()
```

**Note**: The condition and the command must be passed by name so that they're revaluated in each iteration.
**Note**: WHILE is tail recursive, so it can operate with a constant stack size.

Write a function implementing a `repeat` loop that is used as follows:

```
REPEAT {
  command
} (condition)
```

It should execute command one or more times, until `condition` is true.

```
def REPEAT(command\: => Unit)(condition: => Boolean): Unit = {
  command
  if(condition) () else REPEAT(command)(condition)
}
```

### For Loops

The classical `for` loop in Java can not be modeled simply by a higher-order function.

The reasosn is that in a Java program like

```
for( int i = 1; i < 3; i = i + 1){ System.out.println(i + " ")}
```
the arguments of for contain the **declaration** of the variable i, which is visible in other arguments and in the body.

However, in Scala  there is a kind of for loop similar to Java's extended for loop:
```
for( i< 1 unitl 3) {System.out.println(i + " ")}
```

### Translation of For-Loops

For-Loops translate similarly to for-expressions, but using the foreach combinator instead of `map` or `flatMap`.

`foreach` is defined on collections with elements of type T as follows

```
def foreach(f: T => Unit): Unit =
  // apply f to each element of the collection

```
**Example:**
```
for(i <- 1 until 3; j <- "abc") println(i + " " + j)
```
translates to:
```
(1 until 3) foreach (i => "abc" foreach (j => println(i + " " + j)))
```

---

## Discrete Event Simulation

Here's an example that shows how assignments and higher-order functions can be combined in interesting ways.

We will construct a digital circuit simulator.

The simulator is based on a general framework for discrete event simulation.

Let's start with a small description language for digital circuits.

A digital circuit is composed of wires and of functional components.

Wires transport signals that are transformed by components.

We represent signals that are transformed by components.

We represent signals using booleans true and false.

The base components (gates)  are:
* The *Inventor*, whose output is the inverse of its input.
* The *AND gate*, whose output is the conjunction of its inputs.
* The *OR gate*, whose output is the disjunction of its inputs.

Other components can be constructed by combining these base components.

The components have a reaction time (or *delay*), i.e their outputs don't change immediately after a change to their inputs.


![digital circuit diagram](./resources/digital_circuit_diagram.png)
