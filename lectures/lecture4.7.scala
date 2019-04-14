/*
All lists are based on the cons structure e.g head: T, tail: List( head, ....Nil)

The constructors of Lists are:

All lists are constructed from:
- the empty list Nil, and
- the construction operation :: (pronounced cons):
x :: xs gives a new list with the first leement x, followed by the element of xs

 */

/*
Right Associativity

Convention: Operators ending in ":" associate to the right
A :: B :: C is interpreted as A :: (B :: C)

We can thus omit the parentheses in the definition above


Example
val nums = 1 :: 2 :: 3 :: 4 : Nil

nums expression gets expanded to

Ni.::(4).::(3).::(2).::(1)
 */