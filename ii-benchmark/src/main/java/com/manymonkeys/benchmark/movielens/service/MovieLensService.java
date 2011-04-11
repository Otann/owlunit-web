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
public class MovieLensService extends MovieService {

    public static final String USER_ID = MovieLensService.class.getName() + ".USER_ID";

    public static final double INITIAL_TAG_WEIGHT = 1D;
    public static final double ADDITIONAL_TAG_WEIGHT = 1D;

    public static final double INITIAL_FULL_MOVIE_WEIGHT = 1D;

    public static final double MAX_RATING = 5;
    public static final double MIN_RATING = 0;
    public static final double RATING_RANGE = MAX_RATING - MIN_RATING;

    public MovieLensService(Keyspace keyspace) {
        super(keyspace);
    }

    public InformationItem loadOrCreateUser(long id) {
        Collection<InformationItem> users = super.multigetByMeta(USER_ID, Long.toString(id));
        if (users.isEmpty()) {
            InformationItem user = super.createInformationItem();
            super.setMeta(user, USER_ID, Long.toString(id));
            return user;
        } else {
            return users.iterator().next();
        }
    }

    public boolean isUser(InformationItem item) {
        return item.getMeta(USER_ID) != null;
    }

    public boolean isMovie(InformationItem item) {
        return item.getMeta(MovieService.MOVIE_LENS_ID) != null;
    }

    public InformationItem scoreTag(InformationItem item, InformationItem tag) {
        Double weight = item.getComponentWeight(tag);
        if (weight == null) {
            super.setComponentWeight(item, tag, INITIAL_TAG_WEIGHT);
        } else {
            super.setComponentWeight(item, tag, weight + ADDITIONAL_TAG_WEIGHT);
        }

        return tag;
    }

    public void scoreMovieForUser(InformationItem user, InformationItem movie, double rating) throws NoSuchMovieException {

        if (rating > MAX_RATING) {
            rating = MAX_RATING;
        }
        if (rating < MIN_RATING) {
            rating = MIN_RATING;
        }
        double relativeRating = rating / RATING_RANGE;

        // Score movie
        super.setComponentWeight(user, movie, relativeRating * INITIAL_FULL_MOVIE_WEIGHT);

        // Diffuse
        for (Map.Entry<InformationItem, Double> entry : movie.getComponents().entrySet()) {
            InformationItem item = entry.getKey();
            Double weight = entry.getValue();
            super.setComponentWeight(user, item, relativeRating * weight);
        }

    }


    public static class NoSuchMovieException extends Exception {
        long movieId;
        public NoSuchMovieException(long movieId) {
            super("Can't load movie with id = " + movieId);
            this.movieId = movieId;
        }
    }
}
