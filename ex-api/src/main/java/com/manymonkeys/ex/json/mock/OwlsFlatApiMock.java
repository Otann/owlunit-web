package com.manymonkeys.ex.json.mock;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.ex.json.controllers.OwlsFlatApi;
import com.manymonkeys.ex.json.controllers.OwlsMovieApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public class OwlsFlatApiMock implements OwlsFlatApi {

    @Override
    public Map<Ii, Double> getSimilarMovies(String userId, String movieName, Long amount, boolean showReasons) {
        Map<Ii, Double> result = new HashMap<Ii, Double>();

        if (showReasons) {
            result.put(IiMock.getSampleMovie("User", 5), 0d);
            result.put(IiMock.getSampleMovie(movieName, 20), 0d);
        }

        for (int i = 0; i < amount; ++i) {
            result.put(IiMock.getSampleMovie(String.format("Recommendation #%d", i), i), (double) i);
        }

        return result;
    }

    @Override
    public Map<Ii, Double> getRecommendations(String userId, Long amount, boolean showReasons) {
        Map<Ii, Double> result = new HashMap<Ii, Double>();

        if (showReasons) {
            result.put(IiMock.getSampleMovie("User", 5), 0d);
        }

        for (int i = 0; i < amount; ++i) {
            result.put(IiMock.getSampleMovie(String.format("Recommendation #%d", i), i), (double) i);
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
    public void addMovie(String name, String year, String description, List<Person> persons) {
        // do nothing
    }
}
