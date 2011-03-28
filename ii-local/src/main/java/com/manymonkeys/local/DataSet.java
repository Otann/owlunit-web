package com.manymonkeys.local;

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
