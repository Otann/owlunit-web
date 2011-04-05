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
public class Big50 implements DataSet {

    Map<InformationItem, Double> a = new TreeMap<InformationItem, Double>();
    Map<InformationItem, Double> b = new TreeMap<InformationItem, Double>();

    public Big50() {

        for (int i = 1; i <= 5000; ++i) {
            InformationItem item = new InformationItem();
            a.put(item, 1D);
            b.put(item, 1D);
        }
        for (int i = 5001; i <= 10000; ++i) {
            InformationItem item_a = new InformationItem();
            InformationItem item_b = new InformationItem();
            a.put(item_a, 1D);
            b.put(item_b, 1D);
        }

    }

    @Override
    public String getName() {
        return "Large set with 50% similar items";
    }

    @Override
    public String expectedResult() {
        return "50,00";
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
