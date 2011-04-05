package com.manymonkeys.local;

import com.manymonkeys.local.mock.InformationItem;

import java.util.Map;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public interface Formula {

    String getName();

    double compare(Map<InformationItem, Double> a, Map<InformationItem, Double> b);

}
