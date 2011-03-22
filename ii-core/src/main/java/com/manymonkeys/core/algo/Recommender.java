package com.manymonkeys.core.algo;

import com.manymonkeys.core.ii.InformationItem;

import java.util.Map;

/**
 * Many Mokeys
 *
 * @author Anton Chebotaev
 */
public interface Recommender {

	double calculateInitialWeight(InformationItem item, InformationItem component);
    double compareItems(InformationItem left, InformationItem right);
	double compareItemsMaps(Map<InformationItem, Double> a, Map<InformationItem, Double> b);

    Map<InformationItem, Double> getMostLike(InformationItem item);
	Map<InformationItem, Double> getMostLike(Map<InformationItem, Double> items);
}
