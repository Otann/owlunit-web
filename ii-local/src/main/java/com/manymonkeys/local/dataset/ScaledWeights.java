package com.manymonkeys.local.dataset;

import com.manymonkeys.local.DataSet;
import com.manymonkeys.local.mock.InformationItem;

import java.util.Map;
import java.util.TreeMap;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class ScaledWeights implements DataSet {

    Map<InformationItem, Double> a = new TreeMap<InformationItem, Double>();
    Map<InformationItem, Double> b = new TreeMap<InformationItem, Double>();

    public ScaledWeights() {

        for (int i = 1; i < 6; ++i) {
            InformationItem item_a = new InformationItem(Integer.toString(i));
            InformationItem item_b = new InformationItem(Integer.toString(i));
            a.put(item_a, (double) i);
            b.put(item_b, (double) i * 100);
        }

    }

    @Override
    public String getName() {
        return String.format("B has scaled weight of A\n\ta = %s\n\tb = %s", a, b);
    }

    @Override
    public String expectedResult() {
        return "100,00";
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
