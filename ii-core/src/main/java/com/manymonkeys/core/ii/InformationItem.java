package com.manymonkeys.core.ii;

import java.util.Map;

/**
 * Many Monkeys
 * @author Anton Chebotaev
 */
public interface InformationItem  {

    long getId();

    String getMeta(String name);
    double getComponentWeight(InformationItem ii);

    Map<InformationItem, Double> getComponentsWeightsMap();

    LazyResults<Map.Entry<InformationItem, Double>> getComponentsWeights();
    LazyResults<InformationItem> getComponents();
	LazyResults<InformationItem> getItemsContainingThis();

}
