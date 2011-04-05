package com.manymonkeys.local.dataset;

import com.manymonkeys.local.DataSet;
import com.manymonkeys.local.mock.InformationItem;

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class RandomWeights implements DataSet {

    Random generator = new Random(UUID.randomUUID().getLeastSignificantBits());
    private static final int SIZE = 10000;

    Map<InformationItem, Double> a;
    Map<InformationItem, Double> b;

    public RandomWeights() {
        List<InformationItem> randomItems = new LinkedList<InformationItem>();
        a = new HashMap<InformationItem, Double>();
        b = new HashMap<InformationItem, Double>();

        for (int i = 0; i < SIZE; i++) {
            randomItems.add(new InformationItem());
        }

        for (InformationItem item : randomItems) {
            a.put(item, (double) generator.nextInt(999) + 1);
            b.put(item, (double) generator.nextInt(999) + 1);
        }
    }


    @Override
    public String getName() {
//        return String.format("Similar items, but random weights\n\ta = %s\n\tb = %s", a, b);
        return "Similar items, but random weights";
    }

    @Override
    public String expectedResult() {
        return "random";
    }

    @Override
    public Map<InformationItem, Double> getA() {
        return a;
    }

    @Override
    public Map<InformationItem, Double> getB() {
        return b;
    }
}
