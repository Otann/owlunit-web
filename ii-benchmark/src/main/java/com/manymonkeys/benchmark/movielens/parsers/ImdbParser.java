package com.manymonkeys.benchmark.movielens.parsers;

import com.manymonkeys.benchmark.movielens.service.FastService;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public abstract class ImdbParser {

    FastService service;

    protected ImdbParser(FastService service) {
        this.service = service;
    }

    public abstract void run() throws Exception;

    protected void crawl() {
        try {
            run();

            System.out.println("All done");
        } catch (Exception e) {
            System.out.println("Shit happened: " + e.getMessage());
        } finally {
        }
    }

}
