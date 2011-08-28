package com.manymonkeys.ex.json.mock;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.ex.json.controllers.OwlsFlatApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: anton
 * Date: 8/25/11
 * Time: 4:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class OwlsFlatApiMock implements OwlsFlatApi {

    @Override
    public Map<InformationItem, Double> getSimilarMovies(String userId, String movieName, Long amount, boolean showReasons) {
        Map<InformationItem, Double> result = new HashMap<InformationItem, Double>();

        if (showReasons) {
            result.put(InformationItemMock.getSampleMovie("User", 5), 0d);
            result.put(InformationItemMock.getSampleMovie(movieName, 20), 0d);
        }

        for(int i = 0; i < amount; ++i) {
            result.put(InformationItemMock.getSampleMovie(String.format("Recommendation #%d", i), i), (double) i);
        }

        return result;
    }

    @Override
    public Map<InformationItem, Double> getRecommendations(String userId, Long amount, boolean showReasons) {
        Map<InformationItem, Double> result = new HashMap<InformationItem, Double>();

        if (showReasons) {
            result.put(InformationItemMock.getSampleMovie("User", 5), 0d);
        }

        for(int i = 0; i < amount; ++i) {
            result.put(InformationItemMock.getSampleMovie(String.format("Recommendation #%d", i), i), (double) i);
        }

        return result;
    }

    @Override
    public void rate(String userId, String movieName, Double rate) {
        // do nothing
    }

    @Override
    public void like(String userId, String movieName, String provider) {
        // do nothing
    }

    @Override
    public void follow(String followerId, String followedId) {
        // do nothing
    }

    @Override
    public void unfollow(String followerId, String followedId) {
        // do nothing
    }

    @Override
    public void addMovie(String name, List<Person> persons, String description, Map<String, String> other) {
        // do nothing
    }
}
