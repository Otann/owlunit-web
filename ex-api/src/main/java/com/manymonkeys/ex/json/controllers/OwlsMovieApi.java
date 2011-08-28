package com.manymonkeys.ex.json.controllers;

import com.manymonkeys.core.ii.Ii;

import java.util.List;
import java.util.Map;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface OwlsMovieApi {

    public static class Person {
        private String name;
        private String surname;
        private Role role;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getSurname() {
            return surname;
        }

        public void setSurname(String surname) {
            this.surname = surname;
        }

        public Role getRole() {
            return role;
        }

        public void setRole(Role role) {
            this.role = role;
        }
    }

    public static enum Role {
        ACTOR,
        DIRECTOR,
        PRODUCER
    }

    /**
     * Adds movie object to the system
     *
     * @param name        of the movie
     * @param persons     list of persons associated with movie
     * @param description short description of the movie
     * @param other       any other metadata that can be processed later
     */
    /* Todo Anton Chebotaev - Remove "Map<String, String> other" from contract - discuss if necessary,
        no need for flexibility in contract; better solution is to introduce contract versions */
    void addMovie(String name, String year, String description, List<Person> persons, Map<String, String> other);

    /**
     * Loads similar movies
     *
     * @param userId      (may be null) id from external OAuth provider
     * @param movieName   name of the movie
     * @param amount      amount of items to load
     * @param showReasons if true, original Ii will be loaded with common sub-items
     * @return map with movie-rate pairs
     */
    Map<Ii, Double> getSimilarMovies(String userId, String movieName, Long amount, boolean showReasons);
}
