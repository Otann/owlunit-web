package com.manymonkeys.local.formula;

import com.manymonkeys.local.Formula;
import com.manymonkeys.local.InformationItem;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class AntonOriginal implements Formula {
    @Override
    public String getName() {
        return "Anton's (arithmetical mean)";
    }

    @Override
    public double compare(Map<InformationItem, Double> a, Map<InformationItem, Double> b) {
        double result = 0;

        Set<InformationItem> intersection = new HashSet<InformationItem>();
        intersection.addAll(a.keySet());
        intersection.retainAll(b.keySet());

        if (intersection.size() == 0) {
            return 0;
        }

        double a_overall = 0;
        for (Double k : a.values()) {
            a_overall += k;
        }

        double b_overall = 0;
        for (Double k : b.values()) {
            b_overall += k;
        }

        for (InformationItem item : intersection) {
            double a_w = a.get(item);
            double b_w = b.get(item);

            a_w = a_w * 100 / a_overall;
            b_w = b_w * 100 / b_overall;

            result += Math.abs(a_w + b_w) / 2;
        }

        return result;
    }
}
