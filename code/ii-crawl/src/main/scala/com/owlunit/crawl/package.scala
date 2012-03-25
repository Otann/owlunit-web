package com.owlunit

import java.util.concurrent.TimeUnit
import com.codahale.logula.{Log, Logging}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

package object crawl extends  Logging {

  class Timer() {
    var lastStart = System.currentTimeMillis
    var counter = 0

    def time = System.currentTimeMillis - lastStart
    def time(unit: TimeUnit) = unit.convert(time, TimeUnit.MILLISECONDS)
    def reset() {
      lastStart = System.currentTimeMillis
      counter = 0
    }

    def tick(log: Log, limit: Long, message: String, items: String) {
      counter += 1;
      if (counter % limit == 0) {
          val speed = limit.toDouble * 1000 / time;
          log.debug("%s Processed %d %s at speed %.3f per second." format (message, counter, items, speed));
          reset();
      }
    }
  }
  object Timer {
    def start  = new Timer();
  }
  
  
  private class ContinueException extends RuntimeException { }
  def continuable(op: => Unit) {
    try {
      op
    } catch {
      case ex: ContinueException =>
    }
  }


}