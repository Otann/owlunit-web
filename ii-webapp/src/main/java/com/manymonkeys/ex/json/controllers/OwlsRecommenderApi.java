package com.manymonkeys.ex.json.controllers;

import com.manymonkeys.model.cinema.Movie;

import java.util.Map;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface OwlsRecommenderApi {

    /**
     * Loads recommendations for user
     *
     * @param login      id from external OAuth provider
     * @param amount      amount of items to load
     * @param showReasons if true, user's Ii will be loaded with common sub-items
     * @return map with movie-rate pairs
     */
    Map<Movie, Double> getRecommendations(String login, Long amount, boolean showReasons);
}
