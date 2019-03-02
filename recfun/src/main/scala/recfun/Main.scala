package recfun


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
        xs match {
          case Nil => res
          case '(':: tail => reduceBracket(tail, res ++ List('('))
          case ')' :: tail if res.nonEmpty && res.head == '(' => reduceBracket(tail, res.tail)
          case ')' :: tail => reduceBracket(tail, res ++ List('('))
          case _ => reduceBracket(xs.tail, res)
        }
      }

      reduceBracket(chars).isEmpty
    }
  
  /**
   * Exercise 3
   */
    def countChange(money: Int, coins: List[Int]): Int = {

      def iterateOverDenominations(money: Int, coins: List[Int], coin: Int): Int = {
        if(money == 0) 1
        else if(money < 0 ) 0
        else if(coins.isEmpty)
          iterateOverDenominations(money - coin, coins, coin)
        else
          iterateOverDenominations(money - coin, coins, coin) +
          iterateOverDenominations(money, coins.tail, coins.head)
      }
      if(coins.isEmpty) 0
      else iterateOverDenominations(money, coins.tail, coins.head)
    }
  }
