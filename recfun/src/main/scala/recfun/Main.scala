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

        if(xs.isEmpty) return res
        if(xs.head == '(') return  reduceBracket(xs.tail, res ++ List(xs.head))
        if(xs.head == ')' && res.nonEmpty && res.head == '(') return reduceBracket(xs.tail, res.tail)
        if(xs.head == ')' && res.isEmpty) return reduceBracket(xs.tail, res ++ List(xs.head))
        reduceBracket(xs.tail, res)
      }

      val x = reduceBracket(chars)
      println(x)
      x.isEmpty
    }
  
  /**
   * Exercise 3
   */
    def countChange(money: Int, coins: List[Int]): Int = ???
  }
