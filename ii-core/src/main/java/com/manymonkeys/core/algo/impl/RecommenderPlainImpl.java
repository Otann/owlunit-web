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

    private Integer componentsLimit = 20;
    private Integer parentsLimit = 20;

    public Integer getComponentsLimit() {
        return componentsLimit;
    }

    public void setComponentsLimit(Integer componentsLimit) {
        this.componentsLimit = componentsLimit;
    }

    public Integer getParentsLimit() {
        return parentsLimit;
    }

    public void setParentsLimit(Integer parentsLimit) {
        this.parentsLimit = parentsLimit;
    }

    @Override
    public void diffuse(InformationItem item, InformationItem component, InformationItemDao dao) {
        //To change body of implemented methods use File | Settings | File Templates.
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

        // Get most heavy components
        int limit = componentsLimit;
        Set<InformationItem> componentsFiltered = new HashSet<InformationItem>();
        Iterator<InformationItem> iterator = sortByValue(items, true).keySet().iterator();
        while (limit > 0 && iterator.hasNext()) {
            limit--;
            componentsFiltered.add(iterator.next());
        }

        // Fast load parents
        Collection<InformationItem> parents = dao.multigetParents(items.keySet());

        // Fast load components for parents
        dao.multigetComponents(parents);

        for (InformationItem parent : parents) {
            result.put(parent, compareItemsMaps(items, parent.getComponents()));
        }

        return sortByValue(result, true);
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
