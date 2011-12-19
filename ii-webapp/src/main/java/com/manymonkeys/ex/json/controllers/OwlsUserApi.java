package com.manymonkeys.ex.json.controllers;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface OwlsUserApi {

    /**
     * Tells the system that user rated movie
     *
     * @param login    id from external OAuth provider
     * @param movieName name of the movie
     * @param rate      in percents, i.e. 89
     */
    void rate(String login, String movieName, Long movieYear, Double rate);

    /**
     * Tells the system that user liked movie through external provider
     *
     * @param login    id from external OAuth provider
     * @param movieName name of the movie
     * @param movieYear year of the movie
     * @param provider  like provider (i.e. 'facebook' or 'google+')
     */
    void like(String login, String movieName, Long movieYear, String provider);

    /**
     * Tells the system that one user now follows another
     *
     * @param followerLogin id of user that follows another
     * @param followedLogin is of user that is followed
     */
    void follow(String followerLogin, String followedLogin);

    /**
     * Tells the system that one user no longer follows another
     *
     * @param followerLogin id of user that no longer follows another
     * @param followedLogin is of user that is no longer followed
     */
    void unfollow(String followerLogin, String followedLogin);
}

