package com.manymonkeys.benchmark.movielens.service;

import com.manymonkeys.core.ii.InformationItem;

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

    public static final String ITEM_NAME = "ITEM_NAME";

    public static final double INITIAL_TAG_TO_USER_WEIGHT = 1d;
    public static final double ADDITIONAL_TAG_TO_USER_WEIGHT = 0.1d;

    public static final double INITIAL_TAG_TO_MOVIE_WEIGHT = 1d;
    public static final double ADDITIONAL_TAG_TO_MOVIE_WEIGHT = 0.1d;

    public static final double INITIAL_DIFFUSE_MUTLIPLIER = 0.5d;
    public static final double ADDITIONAL_DIFFUSE_MULTIPLIER = 0.5d;

    public InformationItem loadOrCreateUser(long id) {
        Collection<InformationItem> users = super.loadByMeta(USER_ID, Long.toString(id));
        if (users.isEmpty()) {
            InformationItem user = super.createInformationItem();
            super.setMeta(user, USER_ID, Long.toString(id));
            return user;
        } else {
            return users.iterator().next();
        }
    }

    public InformationItem loadOrCreateMovie(long externalId) {
        Collection<InformationItem> items = this.loadByMeta(MOVIE_ID, Long.toString(externalId));
        if (items.size() == 0) {
            InformationItem item = this.createInformationItem();
            this.setMeta(item, MOVIE_ID, Long.toString(externalId));
            return item;
        } else {
            return items.iterator().next();
        }
    }

    public InformationItem loadOrCreateTag(String name) {
        Collection<InformationItem> items = this.loadByMeta(ITEM_NAME, name);
        if (items.size() == 0) {
            InformationItem item = this.createInformationItem();
            this.setMeta(item, ITEM_NAME, name);
            return item;
        } else {
            return items.iterator().next();
        }
    }

    public InformationItem scoreTagForUser(InformationItem user, InformationItem tag) {
        Double weight = user.getComponentWeight(tag);
        if (weight == null) {
            super.setComponentWeight(user, tag, INITIAL_TAG_TO_USER_WEIGHT);
        } else {
            super.setComponentWeight(user, tag, weight + ADDITIONAL_TAG_TO_USER_WEIGHT);
        }
        return tag;
    }

    public InformationItem scoreTagForMovie(InformationItem movie, InformationItem tag) {
        Double weight = movie.getComponentWeight(tag);
        if (weight == null) {
            super.setComponentWeight(movie, tag, INITIAL_TAG_TO_MOVIE_WEIGHT);
        } else {
            super.setComponentWeight(movie, tag, weight + ADDITIONAL_TAG_TO_MOVIE_WEIGHT);
        }
        return tag;
    }

    public void scoreMovieForUser(InformationItem user, InformationItem movie, double rating) {
        for (Map.Entry<InformationItem, Double> entry : movie.getComponents().entrySet()) {
            InformationItem item = entry.getKey();
            Double weight = entry.getValue();

            Double userWeight = user.getComponentWeight(movie);
            if (userWeight ==  null) {
                super.setComponentWeight(user, movie, INITIAL_DIFFUSE_MUTLIPLIER * weight * rating / 5d);
            } else {
                super.setComponentWeight(user, item, userWeight + (ADDITIONAL_DIFFUSE_MULTIPLIER * weight * rating / 5d));
            }
        }
    }
}
