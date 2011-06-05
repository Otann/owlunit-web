package com.manymonkeys.crawlers.common;

import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class TimeWatch {

    long starts;
    long counter;

    public static TimeWatch start() {
        return new TimeWatch();
    }

    private TimeWatch() {
        reset();
        counter = 0;
    }

    public TimeWatch reset() {
        starts = System.currentTimeMillis();
        return this;
    }

    public long time() {
        long ends = System.currentTimeMillis();
        return ends - starts;
    }

    public long time(TimeUnit unit) {
        return unit.convert(time(), TimeUnit.MILLISECONDS);
    }

    public void tick(Logger logger, long limit, String message, String items) {
        counter++;
        if (counter % limit == 0) {
            double speed = ((double) limit) * 1000 / time();
            System.out.println(String.format("%s Processed %d %s at speed %.3f per second.", message, counter, items, speed));
            reset();
        }

    }
}