package com.manymonkeys.benchmark.movielens;

import com.manymonkeys.benchmark.movielens.parsers.*;
import com.manymonkeys.benchmark.movielens.service.FastService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class DatasetImporter {

    public static void main(String[] args) throws Exception {

        FastService service = new FastService();
        MoviesParser.parse(service);
        new ImdbKeywordsParser("../runtime/imdb/keywords.list", service).run();
//        new ImdbPersonParser("../runtime/imdb/actors.list", service, Constants.INITIAL_PERSON_WEIGHT).run();
//        new ImdbPersonParser("../runtime/imdb/actresses.list", service, Constants.INITIAL_PERSON_WEIGHT).run();
//        new ImdbPersonParser("../runtime/imdb/directors.list", service, Constants.INITIAL_PERSON_WEIGHT).run();

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("D:\\service.dat"));
        oos.writeObject(service);
        oos.close();

    }
}
