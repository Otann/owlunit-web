package com.owlunit

import java.util.concurrent.TimeUnit
import org.apache.log4j.Level
import com.weiglewilczek.slf4s.{Logger, Logging}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

package object crawl {

  object Counter {
    def start(total: Long = 0) = new Counter(total);
  }
  class Counter(val total: Long) {
    var lastStart = System.currentTimeMillis
    var counter = 0

    def time = System.currentTimeMillis - lastStart
    def time(unit: TimeUnit):Long = unit.convert(time, TimeUnit.MILLISECONDS)
    def reset() {
      lastStart = System.currentTimeMillis
      counter = 0
    }

    def tick(log: Logger, limit: Long, items: String) {
      counter += 1;
      if (counter % limit == 0) {
        val speed = limit.toDouble * 1000 / time;
        if (total == 0) {
          log.debug("Processes %d %s at speed %.3f/s." format (counter, items, speed));
        } else {
          val eta = ((total - counter) / speed).toInt
          if (eta < 60) {
            log.debug("Processes %d %s at speed %.3f/s. ETA = %d s" format (counter, items, speed, eta));
          } else if (eta < 3600){
            log.debug("Processes %d %s at speed %.3f/s. ETA = %d m" format (counter, items, speed, eta / 60));
          } else {
            val h = eta / 3600
            val m = eta % 3600 / 60
            log.debug("Processes %d %s at speed %.3f/s. ETA = %d h %d m" format (counter, items, speed, h, m));
          }

          
        }


        lastStart = System.currentTimeMillis
      }
    }
  }

  def stripQuotes(name: String) = {
    if (name.startsWith("\"") && name.endsWith("\""))
      name.substring(1, name.length - 1)
    else
      name
  }
}