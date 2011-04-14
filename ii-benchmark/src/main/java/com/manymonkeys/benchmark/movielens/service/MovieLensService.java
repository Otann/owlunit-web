package com.manymonkeys.benchmark.movielens.service;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.MovieService;
import me.prettyprint.hector.api.Keyspace;

import java.util.Collection;
import java.util.Map;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class MovieLensService extends InMemoryDao {

    public static final String USER_ID = "USER_ID";
    public static final String MOVIE_ID = "MOVIE_ID";

    public static final double INITIAL_TAG_WEIGHT = 1D;
    public static final double ADDITIONAL_TAG_WEIGHT = 0.1D;

    public static final String ITEM_NAME = "name";

    public InformationItem loadOrCreateUser(long id) {
        InformationItem user = super.getByMeta(USER_ID, Long.toString(id));
        if (user == null) {
            user = super.createInformationItem();
            super.setMeta(user, USER_ID, Long.toString(id));
            return user;
        } else {
            return user;
        }
    }

    public InformationItem scoreTag(InformationItem item, InformationItem tag) {
        Double weight = item.getComponentWeight(tag);
        if (weight == null) {
            super.setComponentWeight(item, tag, INITIAL_TAG_WEIGHT);
        } else {
            super.addComponentWeight(item, tag, weight + ADDITIONAL_TAG_WEIGHT);
        }

        return tag;
    }

    public void scoreMovieForUser(InformationItem user, InformationItem movie, double rating) throws NoSuchMovieException {

        // Diffuse
        for (Map.Entry<InformationItem, Double> entry : movie.getComponents().entrySet()) {
            InformationItem item = entry.getKey();
            Double weight = entry.getValue();
            super.addComponentWeight(user, item, weight * rating * 0.1);
        }

    }


    /*
     *
     * ALMOST OVERLOADED
     *
     */

    public InformationItem createMovie(String name, long year) {
        InformationItem item = createInformationItem();
        this.setMeta(item, ITEM_NAME, name);
        this.setMeta(item, "year", Long.toString(year));
        return item;
    }

    public InformationItem getTag(String name) {
        return this.getByMeta(ITEM_NAME, name);
    }

    public InformationItem createTag(String name) {
        InformationItem item = this.createInformationItem();
        this.setMeta(item, ITEM_NAME, name);
        return item;
    }


    public static class NoSuchMovieException extends Exception {
        long movieId;
        public NoSuchMovieException(long movieId) {
            super("Can't load movie with id = " + movieId);
            this.movieId = movieId;
        }
    }
}
