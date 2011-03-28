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
public class IlyaSquare implements Formula {

    @Override
    public String getName() {
        return "Ilya's (Square)";
    }

    public double compare(Map<InformationItem, Double> a, Map<InformationItem, Double> b) {
        Set<InformationItem> union = new HashSet<InformationItem>();
        union.addAll(a.keySet());
        union.addAll(b.keySet());

        if (union.size() == 0) {
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

        double intersection = 0;

        for (InformationItem item : union) {
            double a_w = a.get(item) == null ? 0 : a.get(item);
            double b_w = b.get(item) == null ? 0 : b.get(item);

            a_w = a_w * 100 / a_overall;
            b_w = b_w * 100 / b_overall;

            intersection += Math.min(a_w, b_w);
        }

        return intersection;
    }
}

