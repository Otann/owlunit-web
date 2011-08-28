package com.manymonkeys.ex.json.controllers;

import com.manymonkeys.core.ii.Ii;

import java.util.Map;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface OwlsRecommenderApi {

    /**
     * Loads recommendations for user
     *
     * @param userId      id from external OAuth provider
     * @param amount      amount of items to load
     * @param showReasons if true, user's Ii will be loaded with common sub-items
     * @return map with movie-rate pairs
     */
    Map<Ii, Double> getRecommendations(String userId, Long amount, boolean showReasons);
}
