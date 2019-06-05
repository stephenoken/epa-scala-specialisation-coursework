package frp

object NoSignal extends Signal[Nothing](???) {
  override def computeValue(): Unit = ()
}

