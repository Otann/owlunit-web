package com.manymonkeys.ex.json.controllers;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface OwlsUserApi {

    /**
     * Tells the system that user rated movie
     *
     * @param userId    id from external OAuth provider
     * @param movieName name of the movie
     * @param rate      in percents, i.e. 89
     */
    void rate(String userId, String movieName, Double rate);

    /**
     * Tells the system that user liked movie through external provider
     *
     * @param userId    id from external OAuth provider
     * @param movieName name of the movie
     * @param provider  like provider (i.e. 'facebook' or 'google+')
     */
    void like(String userId, String movieName, String provider);

    /**
     * Tells the system that one user now follows another
     *
     * @param followerId id of user that follows another
     * @param followedId is of user that is followed
     */
    void follow(String followerId, String followedId);

    /**
     * Tells the system that one user no longer follows another
     *
     * @param followerId id of user that no longer follows another
     * @param followedId is of user that is no longer followed
     */
    void unfollow(String followerId, String followedId);
}

