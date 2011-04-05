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
public class OneFromTen implements DataSet {

    Map<InformationItem, Double> a = new TreeMap<InformationItem, Double>();
    Map<InformationItem, Double> b = new TreeMap<InformationItem, Double>();

    public OneFromTen() {

        InformationItem item = new InformationItem("common");
        a.put(item, 1D);
        b.put(item, 1D);

        for (int i = 1; i < 10; ++i) {
            InformationItem item_a = new InformationItem("a" + i);
            InformationItem item_b = new InformationItem("b" + i);
            a.put(item_a, 1D);
            b.put(item_b, 1D);
        }

    }

    @Override
    public String getName() {
        return String.format("One item is similar\n\ta = %s\n\tb = %s", a, b);
    }

    @Override
    public String expectedResult() {
        return "not much";
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
