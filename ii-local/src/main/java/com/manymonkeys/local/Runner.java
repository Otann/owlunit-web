package com.manymonkeys.local;

import com.manymonkeys.local.dataset.*;
import com.manymonkeys.local.formula.AntonOriginal;
import com.manymonkeys.local.formula.AntonRMSE;
import com.manymonkeys.local.formula.IlyaSquare;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class Runner {

    public static void runSet(DataSet set, List<Formula> formulas) {
        Map<InformationItem, Double> a = set.getA();
        Map<InformationItem, Double> b = set.getB();

        System.out.println("=========================================================================");
        System.out.println(String.format("    Set name: %s", set.getName()));
        System.out.println(String.format("    Map sizes %d and %d.", a.size(), b.size()));
        System.out.println("-------------------------------------------------------------------------");
        System.out.println("    Formula's name                        | result | processing time (ms)");
        System.out.println("==========================================|========|=====================");
        for (Formula formula : formulas) {
            TimeWatch watch = TimeWatch.start();
            double result = formula.compare(a, b);
            long passedTimeInMs = watch.time();
            System.out.println(String.format(" %-40s | %6.2f | %d ", formula.getName(), result, passedTimeInMs));
        }
        System.out.println(String.format(" Expected result         %25s |", set.expectedResult()));
        System.out.println();
    }

    public static void main(String[] args) {

        List<Formula> formulas = new LinkedList<Formula>();
        formulas.add(new IlyaSquare());
        formulas.add(new AntonRMSE());
        formulas.add(new AntonOriginal());

        List<DataSet> dataSets = new LinkedList<DataSet>();
        dataSets.add(new Big50());
        dataSets.add(new Medium50());
        dataSets.add(new OneFromTen());
        dataSets.add(new ScaledWeights());
        dataSets.add(new AllButOne());
        dataSets.add(new TotalRandom());
        dataSets.add(new RandomWeights());

        for (DataSet set : dataSets) {
            runSet(set, formulas);
        }
    }
}
