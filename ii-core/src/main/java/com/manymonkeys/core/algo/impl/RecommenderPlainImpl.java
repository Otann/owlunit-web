package com.manymonkeys.core.algo.impl;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.InformationItemDao;

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class RecommenderPlainImpl implements Recommender {

    private int componentsLimit = 20;
    private int parentsLimit = 20;
    private double weightThreshold = 1.5;

    @Override
    public void diffuse(InformationItem item, InformationItem component, Double rating, InformationItemDao dao) {
        // TODO: implement
    }

    /**
     * I do know about java code style, but this thing comes from my study research
     * and to be understandable with that research, i use short names and underscores here.
     * And I'm sorry for that
     */
    @Override
    public double compareItems(InformationItem a, InformationItem b) {
        return compareItemsMaps(a.getComponents(), b.getComponents());
    }

    @Override
    public double compareItemsMaps(Map<InformationItem, Double> a, Map<InformationItem, Double> b) {
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

        double min = 0;

        for (InformationItem item : union) {
            double a_w = a.get(item) == null ? 0 : a.get(item);
            double b_w = b.get(item) == null ? 0 : b.get(item);

            a_w = a_w / a_overall;
            b_w = b_w / b_overall;

            min += Math.min(a_w, b_w);
        }

        return min * 100;
    }

    @Override
    public Map<InformationItem, Double> getMostLike(InformationItem item, InformationItemDao dao) {
        Map<InformationItem, Double> result = getMostLike(item.getComponents(), dao);
        result.remove(item);
        return result;
    }

    @Override
    public Map<InformationItem, Double> getMostLike(Map<InformationItem, Double> items, InformationItemDao dao) {
        Map<InformationItem, Double> result = new HashMap<InformationItem, Double>();

        Collection<InformationItem> parents = dao.reloadParents(getValuableComponents(items).keySet());
        dao.reloadComponents(parents);

        for (InformationItem parent : parents) {
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

    private Map<InformationItem, Double> getValuableComponents(Map<InformationItem, Double> items) {
        Map<InformationItem, Double> result = new HashMap<InformationItem, Double>();

        int limit = componentsLimit;
        for(Map.Entry<InformationItem, Double> componentEntry : sortByValue(items, true).entrySet()) {
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
