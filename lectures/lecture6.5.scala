import scala.io.Source

val in = Source.fromURL("https://raw.githubusercontent.com/benofben/progfun1/master/forcomp/target/scala-2.11/classes/forcomp/linuxwords.txt")

val words = in.getLines.toList filter (word => word forall (p => p.isLetter))

val mnen = Map(
  '2' -> "ABC", '3' -> "DEF", '4' -> "GHI", '5' -> "JKL",
  '6' -> "MNO", '7' -> "PQRS", '8' -> "TUV", '9' -> "WXYZ"
)

val charCode: Map[Char, Char] = for {
  (digit, str) <- mnen
  ltr <- str
} yield ltr -> digit

def wordsCode(word: String): String = word.toUpperCase map charCode
//  for (
//  char <- word.toUpperCase
//) yield charCode(char)

wordsCode("Java")

val wordsForNum: Map[String, Seq[String]] =  words groupBy wordsCode withDefaultValue Seq()


def encode(number: String): Set[List[String]] =
  if(number.isEmpty) Set(List())
  else {
    for {
      split <- 1 to number.length
      word <- wordsForNum(number take split)
      rest <- encode(number drop split)
    } yield word :: rest
  }.toSet

encode("7225247386")

def translate(number: String ): Set[String] =
  encode(number) map (_.mkString(" "))

translate("7225247386")