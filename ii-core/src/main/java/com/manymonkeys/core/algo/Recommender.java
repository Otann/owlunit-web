package com.manymonkeys.core.algo;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.InformationItemDao;

import java.util.Map;

/**
 * Many Mokeys
 *
 * @author Anton Chebotaev
 */
public interface Recommender {

    /**
     * Performs item diffusion. Items exchanges information about their components.
     *
     * @param item      which component added to
     * @param component that was added
     * @param dao       to perform add operations
     */
    void diffuse(InformationItem item, InformationItem component, InformationItemDao dao);

    /**
     * Compares two items based on a secret metric
     * Actually calls {@see compareItemsMaps(Map<InformationItem, Double> a, Map<InformationItem, Double> b)}
     *
     * @param left  item
     * @param right item
     * @return value of metric
     */
    double compareItems(InformationItem left, InformationItem right);

    /**
     * Compares two maps of components as if it would be InformationItems based on secret metric
     *
     * @param a map
     * @param b map
     * @return value of metric
     */
    double compareItemsMaps(Map<InformationItem, Double> a, Map<InformationItem, Double> b);

    /**
     * Searches similar items to provided one. Based on a comparation metric
     * Actually calls {@see getMostLike(Map<InformationItem, Double> items)}
     *
     * @param item that used as a query
     * @param dao  to perform fast loads
     * @return map os similar items with comparation results sorted by value of comparation
     */
    Map<InformationItem, Double> getMostLike(InformationItem item, InformationItemDao dao);

    /**
     * Searches similar items to map of components as if it would be components of InformationItem
     * Based on a comparation metric
     *
     * @param items used as a query
     * @param dao   to perform fast loads
     * @return map os similar items with comparation results sorted by value of comparation
     */
    Map<InformationItem, Double> getMostLike(Map<InformationItem, Double> items, InformationItemDao dao);
}
