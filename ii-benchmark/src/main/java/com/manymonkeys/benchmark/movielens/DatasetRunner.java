package com.manymonkeys.benchmark.movielens;

import com.manymonkeys.benchmark.movielens.parsers.MoviesParser;
import com.manymonkeys.benchmark.movielens.parsers.RatingsParser;
import com.manymonkeys.benchmark.movielens.parsers.TagsParser;
import com.manymonkeys.benchmark.movielens.service.FastService;

import java.io.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class DatasetRunner {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        System.out.println("Reading data about service from file...");
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("D:\\service.dat"));
        FastService readService = (FastService) ois.readObject();
        System.out.println("Done.");

        RatingsParser.parse(readService);

    }
}
