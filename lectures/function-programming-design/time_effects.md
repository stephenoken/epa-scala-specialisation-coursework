# Timely Effects
---
## Imperative Event Handling: the Observer Pattern

The observer pattern is widely used when views need to react to changes in a model.

Variants of it are also called:
* publish/subscribe
* model/view/controller

### A publisher trait
```
trait Publisher {
  private var subscribers: Set[Subscriber] = Set()

  def subscribe(subscriber: Subscriber): Unit = subscribers += subscriber

  def unsubscribe(subscriber: Subscriber): Unit = subscribers -= subscriber

  def publish(): Unit = subscribers foreach (_.handler(this))
}
```

### A Subscriber Trait
```
trait Subscriber {
  def handler(pub: Publisher)
}
```

### Observing Bank Accounts
Let's make BankAccount a Publisher:

```
class BankAccount extends Publisher {
  private var balance = 0

  def currentBalance: Int = balance

  def depoist(amount: Int): Unit =
    if (amount > 0) {
      balance = balance + amount
      publish()
    }

    def withdraw(amount: Int): Unit =
      if(0 < amount && amount <= balance) {
        balance = balance - amount
        publish()
      } else throw new Error("insufficient funds")
}
```

A subscriber to maintain the total balance of a list of accounts:

```
class Consolidator(observed: List[BankAccount]) extends Subscriber {
  private var total: Int = sum()

  private def sum() = observed.map(_.currentBalance).sum
  def handler(pub: Publisher) = sum()
  def totalBalance = total
}
```
### Observer Pattern, The Good

* Decouples views from state
* Allows to have a varying number of views of a given state
* Simple to set up

### Observer Pattern, The Bad

* Forces imperative style, since handlers are Unit-typed
* Many moving parts that need to be coordinated.
* Concurrency makes things more complicated
* Views are still tightly bound to one state; view update happens immediately.

To quantify (Adobe presentation from 2008):
* 1/3rd of the code in Adobe's desktop applications is devoted to event handling.
* 1/2 of the bugs are found in this code.

### How to Improve?

The rest of this course will explore different ways in which we can improve on the imperative view of reactive programming embodied in the observer pattern..

This Week: Functional Reactive Programming
Next 2 weeks: Abstracting over events and eventstreams with futures and observables
Last 3 weeks: Handling concurrency with Actors

---

## Functional Reactive Programming

### What is FRP?

Reactive programming is about reacting to sequences of events that happen in time.

Functional view: Aggregate an event sequence into a signal.

* A signal is a value that changes over time
* It is represented as a function from time to the value domain.
* Instead of propagating updates to mutable state, we define new signals in terms of existing ones.

### Example: Mouse Positions

**Event based view:**
Whenever the mouse, an event

`MousMoved(toPos: Position)`

is fired.

**FRP view:**
A signal,

`mousePosition: Signal[Position]`

which at any point in time represents the current mouse position.

### Origins of FRP

FRP stated in 1997 with the paper Functional Reactive Animation by Conal Elliott and Paul Hudak and the Fran library.

There have been many FRP systems since, both stand-alone languages and embedded libraries.

Some exaples are: Flapjax, Elm, Bacon.js, React4J

Event streaming dataflow programming systems such as Rx (which we will see in two weeks), are related but the term FRP is not commonly used for them.

We will introduce FRP by means of a minimal class `frp.Signal` whose implementation is explained at the end of this module.

`frp.Signal` is modelled after `Scala.react`, which is described in the paper Deprecating the Observer Pattern.

### Fundamental Signal Operations

There are two fundamental operations over signals:

1. Obtain the value of the signal at the current time. In our library this is expressed by () application.

`mousePosition() // the current mouse position`

2. Define a signal in terms of other signals. In our library, this is expressed by signal constructor.
```
def inReactangle(LL:Position, UR: Position): Signal[Boolean] =
  Signal {
    val pos = mousePosition()
    LL <= pos && pos <= UR
  }
```

### Constant Signals
The Signal(..) syntax can also be used to define a signal that has always the same value:

`val sig = Signal(3) // the signal always returns 3`

### Time-Varying Signals

How do we define a signal that varies in time?

* We can externally define signals, such as mousePosition and map over them.
* Or we can use a `Var`.

### Variable Signals
Values of type Signal are immutable.

But our library also defines a subclass Var of Signal that can be changed.

Var provides an "update" operation, which allows to redefine the value of a signal from the current time on.

```
val sig = Var(3)
sig.update(5 // From now on, sig returns 5 instead of 3)
```

### Aside: Update Syntax
In Scala, calls to update can be written as assignments.
For instance, for an array arr
`arr(i) = 0`

is translated to

`arr.update(i,0)`
which calls an update which can be thought of as follows:
```
class Array[T] {
  def update(idx: Int, value:T): Unit = ...
}
```

Generally, an indexed assignment like f(E1,..., En) = E is translated to f.update(E1,...,En, E).
This works also if n = 0: f() = E is shorthand for f.update(E)

Hence,

`sig.update(5)`

can be abbreviated to

`sig() = 5`

### Signal and Variables

Signals of type var look a bit like mutable variables, where

`sig()`

is dereferencing, and

`sig() = newValue`

is update.

But there's a crucial difference:

We can map over signals, which gives us a relation between two signals that is maintained automatically, at all future points in time.

No such mechanism exists for mutable variables; we have to propagate all updates manually.

### Example

Repeat the BankAccount example of last section with signals.

Add a signal balance to BankAccounts.

Define a function consolidation which produces the sum of all balances of a given list of accounts.

What savings were possible compared to the publish/subscribe implementation?

```
class BankAccount  {
  val balance = Var(0)

  def deposit(amount: Int): Unit =
    if (amount > 0) {
      val b = balance()
      balance() = b + amount
    }

  def withdraw(amount: Int): Unit =
    if(0 < amount && amount <= balance()) {
      val b = balance()
      balance() = b - amount
    } else throw new Error("insufficient funds")
}

def consolidated(accts: List[BankAccount]) : Signal[Int] =
  Signal(accts.map(_.balance()).sum)

val a = new BankAccount()
val b = new BankAccount()
val c = consolidated(List(a, b))
c()
a deposit 20
```

You'll notice that the solution above is both cleaner and shorter.

Note that there;s an important difference between the variable assignment

`v = v + 1`

and the signal update

`s() = s() + 1`

In the first case, the new value of v becomes the old v plus 1.

In the second case, we try to define a signal s to be at all points in time one larger than itself.

The obviously makes no sense!

### Exercise

Consider the two code fragments below

1.
```
val num = Signal(1)
val twice = Signal(num() * 2)
num() = 2
```
2.
```
var num = Singal(1)
val twice = Signal(num() * 2)
num = Signal(2)
```

Do they yield the same value?

- [ ] yes
- [x] no

Why?? In the first code fragment we're taking the signal `num()` and updating the value in the signal. Where as in the second, we are updating the signal itself into a new signal. The new signal does not have a relationship with the twice signal.

## A simple FRP implmentation

We now develop a simple implementation of Signals and Vars, which together make up the basis of our approach to functional reactive programming.

The classes are assumed to be in a package frp

The user-facing APIs are summarised below:

```
class Signal[T](expr: => T) {
  def apply(): T = ???
}

object Signal {
  def apply[T](expr: => T) = new Signal(expr)
}
```

```
class Var[T](expr: => T) extends Signal[T](expr){

  def update(expr: => T): Unit = ???

}

object Var {
  def apply[T](expr: => T) = new Var(expr)
}
```


### Implementation Idea

Each signal maintains:

* its current value
* the current expression that defines the signal value
* a set of observers: the other signals that depend on its value

Then, if the signal changes, all observers need to be re-evaluated.

### Dependency Maintenance

How do we record dependencies in observers?

* when evaluating a signal-valued expression, need to know which signal caller gets defined or updated by the expression.
* If we know that , then executing a sig() means adding caller to the observers of sig.
* When signal sig's value changes, all previously observing signals are re-evaluated and the set sig.observers is cleared.
* Re-evaluation will re-enter a calling signal caller in sig.observers, as long as caller's value still depends on sig.

### Who's Calling?

How do we find out on whose behalf a signal expression is evaluated?

One simple (simplistic?) way to do this is to maintain a global data structure referring to the current caller. (We will discuss and refine this later).

That data structure is accessed in a stack-like fashion because one evaluation of a signal might trigger others.

### Set up in Object Signal

We also evaluate signal expressions at the top-level when there is no other signal that;s defined or updated.

We use the "sentinel" object NoSignal as the caller for these expressions.

### Revaluating Callers

A signal's current value can change when
* somebody calls an update operation on a Var, or
* the value of a dependent signal changes

Propagating requires a more refined implementation of computeValue:

```
protected def computeValue(): Unit = {
  val newValue = caller.withValue(this)(myExper())
  if(myValue != newValue) {
    myValue = newValue
    val obs = observers
    observers = Set()
    obs.foreach(_.computeValue())
  }
}
```

### Discussion

Our implementation of FRP is quite stunning in its simplicity.

But you might argue that it is too simplistic.

In particular, it makes use of the worst kind of state: global state.

One immediate problem is: What happens if we try to evaluate several signal expressions in parallel?
* The caller signal will become "global" by concurrent updates.
