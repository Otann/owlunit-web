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
public class AllButOne implements DataSet {

    Map<InformationItem, Double> a = new TreeMap<InformationItem, Double>();
    Map<InformationItem, Double> b = new TreeMap<InformationItem, Double>();

    public AllButOne() {

        InformationItem item_a = new InformationItem("uncommon-a");
        a.put(item_a, 1D);
        InformationItem item_b = new InformationItem("uncommon-b");
        b.put(item_b, 1D);

        for (int i = 1; i < 5; ++i) {
            InformationItem item = new InformationItem(Integer.toString(i));
            a.put(item, 1D);
            b.put(item, 1D);
        }

    }

    @Override
    public String getName() {
        return String.format("Only one item is uncommon\n\ta = %s\n\tb = %s", a, b);
    }

    @Override
    public String expectedResult() {
        return "around 80,00";
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
