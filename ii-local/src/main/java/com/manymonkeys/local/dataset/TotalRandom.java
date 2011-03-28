package com.manymonkeys.local.dataset;

import com.manymonkeys.local.DataSet;
import com.manymonkeys.local.InformationItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class TotalRandom implements DataSet {

    Random generator = new Random(UUID.randomUUID().getLeastSignificantBits());
    private static final int SIZELIMIT = 100000;

    public Map<InformationItem, Double> generateMap() {
        int size = generator.nextInt(SIZELIMIT);
        Map<InformationItem, Double> result = new HashMap<InformationItem, Double>();

        for (int i = 0; i < size; i++) {
            result.put(new InformationItem(), generator.nextDouble());
        }

        return result;
    }

    Map<InformationItem, Double> a;
    Map<InformationItem, Double> b;

    public TotalRandom() {
        a = generateMap();
        b = generateMap();
    }

    @Override
    public String getName() {
        return "Absolutely random with sizelimit " + SIZELIMIT;
    }

    @Override
    public String expectedResult() {
        return "around 0,00";
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
