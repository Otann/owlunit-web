package com.manymonkeys.ex.json.controllers;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.model.cinema.Person;
import com.manymonkeys.service.cinema.MovieService;

import java.util.List;
import java.util.Map;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface OwlsMovieApi {

    /**
     * Adds movie object to the system
     *
     * @param name        of the movie
     * @param persons     list of persons associated with movie
     * @param description short description of the movie
     */
    void addMovie(String name, Long year, String description, List<Person> persons);

    /**
     * Loads similar movies
     *
     * @param userId      (may be null) id from external OAuth provider
     * @param movieName   name of the movie
     * @param year        year of the movie
     * @param amount      amount of items to load
     * @param showReasons if true, original Ii will be loaded with common sub-items
     * @return map with movie-rate pairs
     */
    Map<Movie, Double> getSimilarMovies(String userId, String movieName, Long year, Long amount, boolean showReasons);
}
