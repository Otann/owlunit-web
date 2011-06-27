package com.manymonkeys.benchmark.movielens.service;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.InformationItemDao;

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class FastMinRecommender implements Recommender {

    Map<InformationItem, Double> overall = new HashMap<InformationItem, Double>();
    Map<UUID, InformationItem> items = new HashMap<UUID, InformationItem>();



    private static double f(double k) {
        // http://www.wolframalpha.com/input/?i=Plot%5B50+%2F+%281+%2B+exp%28-x+%2F+13%29%29+-+25%2C+{x%2C0%2C50}%5D
        return 50 / (1 + Math.exp(-k / 13)) - 25;
    }

    @Override
    public void diffuse(InformationItem item, InformationItem component, Double rating, InformationItemDao dao) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public double overallWeight(InformationItem item) {
        Double cached = overall.get(item);
        if (cached == null) {
            double weight = 0;
            for (Double k : item.getComponents().values()) {
                weight += k;
            }
            overall.put(item, weight);
            return weight;
        } else {
            return cached;
        }
    }

    public void prepareData(InMemoryDao service) {
        for (InformationItem item : service.items.values()) {
            double overall = overallWeight(item);
            for (Map.Entry<InformationItem, Double> entry : item.getComponents().entrySet()) {
                service.setComponentWeight(item, entry.getKey(), f(entry.getValue()) / overall );
            }

        }
    }

    @Override
    public double compareItems(InformationItem item_a, InformationItem item_b) {
        Map<InformationItem, Double> a = item_a.getComponents();
        Map<InformationItem, Double> b = item_b.getComponents();

        double a_overall = overallWeight(item_a);
        double b_overall = overallWeight(item_b);

        Set<InformationItem> union = new HashSet<InformationItem>();
        union.addAll(a.keySet());
        union.addAll(b.keySet());

        if (union.size() == 0)
            return 0;

        double min = 0;
        for (InformationItem item : union) {
            double a_w = a.get(item) == null ? 0 : a.get(item);
            double b_w = b.get(item) == null ? 0 : b.get(item);
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
