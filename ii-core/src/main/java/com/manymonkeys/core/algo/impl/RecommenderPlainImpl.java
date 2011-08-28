package com.manymonkeys.core.algo.impl;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.InformationItemDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
/* Todo Anton Chebotaev - Move this class to "ii-service" package;
    Algorithm and it's logic goes here, all the rest (like API for external calls) - to ii-service,
    that will be used by other components. */
public class RecommenderPlainImpl implements Recommender {

    final Logger logger = LoggerFactory.getLogger(RecommenderPlainImpl.class);

    private int componentsLimit = 20;
    private int parentsLimit = 20;
    private double weightThreshold = 1.5;

    @Override
    public void diffuse(Ii item, Ii component, Double rating, InformationItemDao dao) {
        // TODO: implement
    }

    /**
     * I do know about java code style, but this thing comes from my study research
     * and to be understandable with that research, i use short names and underscores here.
     * And I'm sorry for that
     */
    @Override
    public double compareItems(Ii a, Ii b) {
        return compareItemsMaps(a.getComponents(), b.getComponents());
    }

    @Override
    public double compareItemsMaps(Map<Ii, Double> a, Map<Ii, Double> b) {
        Set<Ii> union = new HashSet<Ii>();
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

        double min = 0;

        for (Ii item : union) {
            double a_w = a.get(item) == null ? 0 : a.get(item);
            double b_w = b.get(item) == null ? 0 : b.get(item);

            a_w = a_w / a_overall;
            b_w = b_w / b_overall;

            min += Math.min(a_w, b_w);
        }

        return min * 100;
    }

    @Override
    public Map<Ii, Double> getMostLike(Ii item, InformationItemDao dao) {
        Map<Ii, Double> result = getMostLike(item.getComponents(), dao);
        result.remove(item);
        return result;
    }

    @Override
    public Map<Ii, Double> getMostLike(Map<Ii, Double> items, InformationItemDao dao) {
        Map<Ii, Double> result = new HashMap<Ii, Double>();

        Collection<Ii> parents = dao.reloadParents(getValuableComponents(items).keySet());
        dao.reloadComponents(parents);

        for (Ii parent : parents) {
            result.put(parent, compareItemsMaps(items, parent.getComponents()));
        }

        return sortByValue(result, true);
    }

    public void setComponentsLimit(int componentsLimit) {
        this.componentsLimit = componentsLimit;
    }

    public void setParentsLimit(int parentsLimit) {
        this.parentsLimit = parentsLimit;
    }

    public void setWeightThreshold(double weightThreshold) {
        this.weightThreshold = weightThreshold;
    }

    private Map<Ii, Double> getValuableComponents(Map<Ii, Double> items) {
        Map<Ii, Double> result = new HashMap<Ii, Double>();

        int limit = componentsLimit;
        for(Map.Entry<Ii, Double> componentEntry : sortByValue(items, true).entrySet()) {
            if (limit-- <= 0)
                break;

            if (componentEntry.getValue() < weightThreshold)
                break;

            result.put(componentEntry.getKey(), componentEntry.getValue());
        }

        return result;
    }

    private static <K, V extends Comparable<V>> Map<K, V> sortByValue(Map<K, V> map, final boolean invertedSort) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return invertedSort
                        ? o2.getValue().compareTo((o1).getValue())
                        : o1.getValue().compareTo((o2).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
