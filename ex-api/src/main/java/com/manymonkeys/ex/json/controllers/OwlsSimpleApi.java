package com.manymonkeys.ex.json.controllers;

import com.manymonkeys.core.ii.InformationItem;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: anton
 * Date: 8/25/11
 * Time: 3:39 PM
 * To change this template use File | Settings | File Templates.
 */
public interface OwlsSimpleApi {

    /**
     * Loads similar movies
     * @param userId (may be null) id from external OAuth provider
     * @param movieName name of the movie
     * @param amount amount of items to load
     * @param showReasons if true, original InformationItem will be loaded with common sub-items
     * @return map with movie-rate pairs
     */
    Map<InformationItem, Double> getSimilarMovies(String userId, String movieName, Long amount, boolean showReasons);

    /**
     * Loads recommendations for user
     * @param userId id from external OAuth provider
     * @param amount amount of items to load
     * @param showReasons if true, user's InformationItem will be loaded with common sub-items
     * @return map with movie-rate pairs
     */
    Map<InformationItem, Double> getRecommendations(String userId, Long amount, boolean showReasons);

    /**
     * Tells the system that user rated movie
     * @param userId id from external OAuth provider
     * @param movieName name of the movie
     * @param rate in percents, i.e. 89
     */
    void rate(String userId, String movieName, Double rate);

    /**
     * Tells the system that user liked movie through external provider
     * @param userId id from external OAuth provider
     * @param movieName name of the movie
     * @param provider like provider (i.e. 'facebook' or 'google+')
     */
    void like(String userId, String movieName, String provider);

    /**
     * Tells the system that one user now follows another
     * @param followerId id of user that follows another
     * @param followedId is of user that is followed
     */
    void follow(String followerId, String followedId);

    /**
     * Tells the system that one user no longer follows another
     * @param followerId id of user that no longer follows another
     * @param followedId is of user that is no longer followed
     */
    void unfollow(String followerId, String followedId);

    public enum Role {
        ACTOR,
        DIRECTOR,
        PRODUCER
    }
    public class Person {
        String name;
        Role role;
    }

    /**
     * Adds movie object to the system
     * @param name of the movie
     * @param persons list of persons associated with movie
     * @param description short description of the movie
     * @param other any other metadata that can be processed later
     */
    void addMovie(String name, List<Person> persons, String description, Map<String, String> other);
}
