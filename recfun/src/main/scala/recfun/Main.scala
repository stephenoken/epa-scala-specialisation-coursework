package recfun

import scala.annotation.tailrec

object Main {
  def main(args: Array[String]) {
    println("Pascal's Triangle")
    for (row <- 0 to 10) {
      for (col <- 0 to row)
        print(pascal(col, row) + " ")
      println()
    }
  }

  /**
   * Exercise 1
   */
    def pascal(c: Int, r: Int): Int =
      if (c == 0 || r == c) 1 else
      pascal(c - 1, r - 1 ) + pascal(c, r - 1)

  /**
   * Exercise 2
   */
    def balance(chars: List[Char]): Boolean = {

      def reduceBracket(xs:List[Char], res: List[Char]=List()): List[Char] = {

        if(xs.isEmpty) return res
        if(xs.head == '(') return  reduceBracket(xs.tail, res ++ List(xs.head))
        if(xs.head == ')' && res.nonEmpty && res.head == '(') return reduceBracket(xs.tail, res.tail)
        if(xs.head == ')' && res.isEmpty) return reduceBracket(xs.tail, res ++ List(xs.head))
        reduceBracket(xs.tail, res)
      }

      reduceBracket(chars).isEmpty
    }
  
  /**
   * Exercise 3
   */
    def countChange(money: Int, coins: List[Int]): Int = {

      def iterateOverDenominations(money: Int, coins: List[Int], coin: Int): Int = {
        if(money == 0) return 1
        if(money < 0 ) return 0
        if(coins.isEmpty) return iterateOverDenominations(money - coin, coins, coin)
        iterateOverDenominations(money - coin, coins, coin) +
          iterateOverDenominations(money, coins.tail, coins.head)
      }

      iterateOverDenominations(money, coins.tail, coins.head)
    }
  }
