package net.nastich.factory.util

import akka.actor.Actor

/**
 * Utility trait to provide a counter to wherever it's mixed in. It is thread-safe as long as it's
 * used as a trait for an actor.
 *
 * @author sena0713
 * @since 20.09.2014 
 */
trait HasSequence { this: Actor =>

  private var _seq: Long = 0

  def nextSeq(): Long = {
    _seq = _seq + 1
    _seq
  }

}
