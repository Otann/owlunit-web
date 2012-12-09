package com.owlunit.crawl.lib

import java.util.concurrent.TimeUnit
import com.weiglewilczek.slf4s.Logger

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */

class Counter(val total: Long, sleepInterval: Long = 0, sleepDuration: Long = 0) {
  var lastStart = System.currentTimeMillis
  var counter = 0L

  def time = System.currentTimeMillis - lastStart
  def time(unit: TimeUnit):Long = unit.convert(time, TimeUnit.MILLISECONDS)
  def reset() {
    lastStart = System.currentTimeMillis
    counter = 0
  }

  def tick(log: Logger, limit: Long, items: String) {
    counter += 1
    if (counter % limit == 0) {
      val speed = limit.toDouble * 1000 / time
      if (total == 0) {
        log.debug("%d %s at speed %.3f/s." format (counter, items, speed))
      } else {
        val eta = ((total - counter) / speed).toInt
        if (eta < 60) {
          log.debug("%d %s at speed %.3f/s. ETA = %d s" format (counter, items, speed, eta))
        } else if (eta < 60 * 60) {
          log.debug("%d %s at speed %.3f/s. ETA = %d m" format (counter, items, speed, eta / 60))
        } else if (eta < 60 * 60 * 24) {
          val h = eta / (60 * 60)
          val m = eta % (60 * 60) / 60
          log.debug("%d %s at speed %.3f/s. ETA = %d h %d m" format (counter, items, speed, h, m))
        } else {
          val d = eta / (60 * 60 * 24)
          val h = eta % (60 * 60 * 24) / (60 * 60)
          val m = eta % (60 * 60 * 24) / 60
          log.debug("%d %s at speed %.3f/s. ETA = %s days %d h %d m" format (counter, items, speed, d, h, m))
        }
      }
      lastStart = System.currentTimeMillis
    }

    if (sleepInterval != 0 && counter % sleepInterval == 0) {
      log.debug("%s amount achieved, sleeping for %s" format (sleepInterval, sleepDuration))
      Thread.sleep(sleepDuration)
    }
  }
}

object Counter {
  def start(total: Long = 0, sleepInterval: Long = 0, sleepDuration: Long = 0) =
    new Counter(total, sleepInterval, sleepDuration)
}
