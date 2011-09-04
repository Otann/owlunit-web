package com.manymonkeys.ex.json.controllers.impl;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.ex.json.controllers.OwlsFlatApi;
import com.manymonkeys.ex.json.controllers.OwlsMovieApi;
import com.manymonkeys.model.cinema.Person;

import java.util.List;
import java.util.Map;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public class OwlsFlatApiJsonImpl implements OwlsFlatApi {
    @Override
    public Map<Ii, Double> getSimilarMovies(String userId, String movieName, Long amount, boolean showReasons) {
        //movie service
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<Ii, Double> getRecommendations(String userId, Long amount, boolean showReasons) {
        //recommender serivce
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void rate(String userId, String movieName, Double rate) {
        //user service
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void like(String userId, String movieName, String provider) {
        //user service
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void follow(String followerId, String followedId) {
        //user service
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void unfollow(String followerId, String followedId) {
        //user service
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addMovie(String name, String year, String description, List<Person> persons) {
        //movie service
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
