package com.manymonkeys.core.algo.impl;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.algo.Recommender;

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class RecommenderPlainImpl implements Recommender {

	/**
	 * I do know about java code style, but this thing comes from my study research
	 * and to be understandable with that research, i use short names and underscores here.
	 * And I'm sorry for that
	 */
	public double compareItems(InformationItem a, InformationItem b) {
		return compareItemsMaps(a.getComponentsWeightsMap(), b.getComponentsWeightsMap());
	}

	public double compareItemsMaps(Map<InformationItem, Double> a, Map<InformationItem, Double> b) {
		double result = 0;

		Set<InformationItem> union = new HashSet<InformationItem>();
		union.addAll(a.keySet());
		union.addAll(b.keySet());

        Set<InformationItem> intersection = new HashSet<InformationItem>();
        intersection.addAll(a.keySet());
        intersection.retainAll(b.keySet());

        if (union.size() == 0) {
            return 0;
        }

		double a_overall = 0;
		for (Double k: a.values()) {
			a_overall += k;
		}

		double b_overall = 0;
		for (Double k: b.values()) {
			b_overall += k;
		}

		for(InformationItem item : union) {
			double a_w = a.get(item) == null ? 0 : a.get(item);
			double b_w = b.get(item) == null ? 0 : b.get(item);

			a_w = a_w * 100 / a_overall;
			b_w = b_w * 100 / b_overall;

			result += Math.pow((a_w - b_w), 2);
		}

		result = 100 - Math.sqrt(result / union.size()); // 100% - RMSE

		return result;
	}

	public double calculateInitialWeight(InformationItem current, InformationItem component) {
             		//TODO: discuss "1" as default value, real values as comparator results
		//return Math.max(1, compareInformationItems(current, component));
		return 1;
	}

	public Map<InformationItem, Double> getMostLike(InformationItem item) {
		Map<InformationItem, Double> result = getMostLike(item.getComponentsWeightsMap());
		result.remove(item);
		return result;
	}

	public Map<InformationItem, Double> getMostLike(Map<InformationItem, Double> items) {
		Map<InformationItem, Double> result = new HashMap<InformationItem, Double>();
		for (InformationItem component : items.keySet()) {
			for (InformationItem parent : component.getItemsContainingThis()) {
				result.put(parent, compareItemsMaps(items, parent.getComponentsWeightsMap()));
			}
		}
		return invertedSortByValue(result);
	}

	private static <K, V extends Comparable<V>> Map<K, V> invertedSortByValue(Map<K, V> map) {
		List<Map.Entry<K,V>> list = new LinkedList<Map.Entry<K,V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K,V>>() {
			public int compare(Map.Entry<K,V> o1, Map.Entry<K,V> o2) {
				return o2.getValue().compareTo((o1).getValue());
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
		return result;
	}

}
