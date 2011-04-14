package com.manymonkeys.benchmark.movielens;

import com.manymonkeys.benchmark.movielens.utils.TimeWatch;

import java.io.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class Splitter {

    public static void main(String[] args) throws IOException {

        BufferedReader fileReader = new BufferedReader(new FileReader("../runtime/movielens/ratings.dat"));
        BufferedWriter trainWriter = new BufferedWriter(new FileWriter("../runtime/movielens/r1.train"));
        BufferedWriter testWriter = new BufferedWriter(new FileWriter("../runtime/movielens/r1.test"));

        TimeWatch watch = TimeWatch.start();

        String line = fileReader.readLine();
        long done = 0;

        while (line != null) {
            done++;

            if (done % 5 == 0) {
                testWriter.write(line);
                testWriter.write("\n");
            } else {
                trainWriter.write(line);
                trainWriter.write("\n");
            }

            if (done % 100 == 0) {
                long milliseconds = watch.time();
                watch.reset();
                System.out.println(String.format("Processed %d lines. Going at speed %.3f / second", done, 100d * 1000 / milliseconds));
            }

            line = fileReader.readLine();
        }

        System.out.println("Fucking done");
    }

}
