package com.manymonkeys.benchmark.movielens.service;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.InformationItemDao;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class FastMinRecommender implements Recommender {

    Map<InformationItem, Double> overall = new HashMap<InformationItem, Double>();

    private static double f(double k) {
        return 50 / (1 + Math.exp(-k / 13)) - 25;
    }

    @Override
    public void diffuse(InformationItem item, InformationItem component, InformationItemDao dao) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double compareItems(InformationItem item_a, InformationItem item_b) {
        Map<InformationItem, Double> a = item_a.getComponents();
        Map<InformationItem, Double> b = item_b.getComponents();

        double a_overall = 0;
        if (overall.get(item_a) == null) {
            for (Double k : a.values()) {
                a_overall += f(k);
            }
            overall.put(item_a, a_overall);
        } else {
            a_overall = overall.get(item_a);
        }

        double b_overall = 0;
        if (overall.get(item_b) == null) {
            for (Double k : b.values()) {
                b_overall += f(k);
            }
            overall.put(item_b, b_overall);
        } else {
            b_overall = overall.get(item_b);
        }

        Set<InformationItem> union = new HashSet<InformationItem>();
        union.addAll(a.keySet());
        union.addAll(b.keySet());

        if (union.size() == 0) {
            return 0;
        }

        double min = 0;

        for (InformationItem item : union) {
            double a_w = a.get(item) == null ? 0 : a.get(item);
            double b_w = b.get(item) == null ? 0 : b.get(item);

            a_w = f(a_w) / a_overall;
            b_w = f(b_w) / b_overall;

            min += Math.min(a_w, b_w);
        }

        return min;
    }

    @Override
    public double compareItemsMaps(Map<InformationItem, Double> a, Map<InformationItem, Double> b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<InformationItem, Double> getMostLike(InformationItem item, InformationItemDao dao) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<InformationItem, Double> getMostLike(Map<InformationItem, Double> items, InformationItemDao dao) {
        throw new UnsupportedOperationException();
    }
}
