package com.owlunit.core.algo;

import com.owlunit.core.ii.Ii;
import com.owlunit.core.ii.IiDao;

import java.util.Map;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public interface Recommender {

    /**
     * Compares two items based on a secret metric
     * Actually calls {@see compareItemsMaps(Map<InformationItem, Double> a, Map<InformationItem, Double> b)}
     *
     * @param left  item
     * @param right item
     * @return value of metric
     */
    double compareItems(Ii left, Ii right);

    /**
     * Compares two maps of components as if it would be InformationItems based on secret metric
     *
     * @param a map
     * @param b map
     * @return value of metric
     */
    double compareItemsMaps(Map<Ii, Double> a, Map<Ii, Double> b);

    /**
     * Searches similar items to provided one. Based on a comparation metric
     * Actually calls {@see getMostLike(Map<InformationItem, Double> items)}
     *
     * @param item that used as a query
     * @param dao  to perform fast loads
     * @return map os similar items with comparation results sorted by value of comparation
     */
    Map<Ii, Double> getMostLike(Ii item, IiDao dao);

    /**
     * Searches similar items to map of components as if it would be components of Ii
     * Based on a comparation metric
     *
     * @param items used as a query
     * @param dao   to perform fast loads
     * @return map os similar items with comparation results sorted by value of comparation
     */
    Map<Ii, Double> getMostLike(Map<Ii, Double> items, IiDao dao);
}
