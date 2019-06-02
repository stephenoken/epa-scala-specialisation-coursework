package quickcheck

import common._

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop._

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  lazy val genHeap: Gen[H] =
    for {
      n <- arbitrary[Int]
      h <- oneOf(genHeap, const(this.empty))
    } yield this.insert(n, h)

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  property("gen1") = forAll { (h: H) =>
    val m = if (isEmpty(h)) 0 else findMin(h)
    findMin(insert(m, h)) == m
  }

  property("mini1") = forAll { a: Int =>
    val h = insert(a, empty)
    findMin(h) == a
  }

  property("findMin: for all ints inserted into a heap with 2 elements the smallest is always returned") =
    forAll {(x: Int,  y: Int) =>
      val h1 = insert(x, empty)
      val h2 = insert(y, h1)
      val min = Math.min(x,y)
      findMin(h2) == min
  }

  property("deleteMin: if an element is inserted into a heap and then removed the head should be empty") =
    forAll { n: Int => {
      val h = insert(n, empty)
      isEmpty(deleteMin(h))
    }
  }

  def extractMinimaFromHeap(heap: H): List[Int] = {
    if(isEmpty(heap)) List()
    else {
      val minHeap = findMin(heap)
      minHeap :: extractMinimaFromHeap(deleteMin(heap))
    }

  }
  property("deleteMin2: ordered list of heap") = {
    forAll(genHeap) { heap =>
      extractMinimaFromHeap(heap) == extractMinimaFromHeap(heap).sorted
    }
  }

  property("deleteMin3: ordered list after melding two heaps") = {
    forAll(genHeap, genHeap) {(heap1, heap2) => {
      val sortedMinimaFromHeaps =  (extractMinimaFromHeap(heap1) ++ extractMinimaFromHeap(heap2)).sorted

      extractMinimaFromHeap(meld(heap1, heap2)) == sortedMinimaFromHeaps
    }

    }
  }

  property("meld1: find lowest meld of two heaps") = {
    forAll(genHeap, genHeap) { (heap1, heap2) =>
      val expectedMin = Math.min(findMin(heap1), findMin(heap2))

      findMin(meld(heap1, heap2)) == expectedMin
    }
  }
}
