package com.manymonkeys.local.formula;

import com.manymonkeys.local.Formula;
import com.manymonkeys.local.mock.InformationItem;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class AntonRMSE implements Formula {
    @Override
    public String getName() {
        return "Anton's (RMSE)";
    }

    @Override
    public double compare(Map<InformationItem, Double> a, Map<InformationItem, Double> b) {
        double result = 0;

        Set<InformationItem> union = new HashSet<InformationItem>();
        union.addAll(a.keySet());
        union.addAll(b.keySet());

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

        for (InformationItem item : union) {
            double a_w = a.get(item) == null ? 0 : a.get(item);
            double b_w = b.get(item) == null ? 0 : b.get(item);

            a_w = a_w / a_overall;
            b_w = b_w / b_overall;

            double delta = 100 * Math.abs((a_w - b_w)) / Math.max(a_w, b_w);
            result += delta * delta;
        }

        result = 100 - Math.sqrt(result / union.size()); // 100% - RMSE

        return result;
    }
}
