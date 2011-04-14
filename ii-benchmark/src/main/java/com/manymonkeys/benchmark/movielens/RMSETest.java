package com.manymonkeys.benchmark.movielens;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class RMSETest {

    public static double RMSD(List<Double> a, List<Double> b) {
        double sum = 0;
        for (int i = 0; i < a.size(); i++) {
            sum += Math.pow(a.get(i) - b.get(i), 2);
        }
        return Math.sqrt(sum / a.size());
    }

    public static void main(String[] args) {

        Random generator = new Random(UUID.randomUUID().getLeastSignificantBits());

        List<Double> a = Arrays.asList(1d, 2d, 3d, 4d, 5d, 1d, 2d, 3d, 4d, 5d, 1d, 2d, 3d, 4d, 5d, 1d, 2d, 3d, 4d, 5d);
        List<Double> b = Arrays.asList(1d, 2d, 3d, 4d, 5d, 1d, 2d, 3d, 4d, 5d, 1d, 2d, 3d, 4d, 5d, 1d, 2d, 3d, 4d, 5d);

        for (int i = 0; i < b.size(); i++) {
            a.set(i, (double) generator.nextInt(6));
            b.set(i, (double) generator.nextInt(6));
        }

        double rmsd = RMSD(a, b);
        double nrmsd = rmsd / (5 - 1);


        System.out.println(String.format("a = %s", a.toString()));
        System.out.println(String.format("b = %s", b.toString()));
        System.out.println(String.format("RMSD(a, b)  = %.3f", rmsd));
        System.out.println(String.format("NRMSD(a, b) = %.3f", nrmsd));
    }

}
