package com.manymonkeys.local;

import com.manymonkeys.local.mock.InformationItem;

import java.util.Map;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public interface DataSet {

    String getName();

    String expectedResult();

    Map<InformationItem, Double> getA();

    Map<InformationItem, Double> getB();
}
