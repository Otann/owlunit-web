package com.manymonkeys.benchmark.movielens.service;

import com.manymonkeys.benchmark.movielens.parsers.Constants;
import com.manymonkeys.core.ii.InformationItem;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class FastService extends InMemoryDao implements Serializable {

    public static final String SIMPLE_NAME = "SIMPLE_NAME";

    private final Pattern simplifyPatter = Pattern.compile("(a |the |, a|, the|,|\\.|\\s|'|\"|:|-|!|#|)");


    public static final String USER_ID = "USER_ID";
    public static final String MOVIE_ID = "MOVIE_ID";

    public static final String ITEM_NAME = "ITEM_NAME";

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


    public InformationItem loadMovie(long externalId) {
        Collection<InformationItem> items = this.loadByMeta(MOVIE_ID, Long.toString(externalId));
        if (items.size() == 0) {
            return null;
        } else {
            return items.iterator().next();
        }
    }

    public InformationItem createMovie(long externalId, String name) {
        Collection<InformationItem> items = this.loadByMeta(MOVIE_ID, Long.toString(externalId));
        if (items.size() == 0) {
            InformationItem item = this.createInformationItem();
            this.setMeta(item, MOVIE_ID, Long.toString(externalId));
            this.setMeta(item, SIMPLE_NAME, simplifyName(name), true);
            return item;
        } else {
            return items.iterator().next();
        }
    }

    public InformationItem getByNameSimplified(String name) {
        try {
            String simpleName = simplifyName(name);
            return loadByMeta(SIMPLE_NAME, simpleName).iterator().next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private String simplifyName(String name) {
        return name.toLowerCase()
                .replace("a ", "")
                .replace("the ", "")
                .replace(", the", "")
                .replace(", a", "")
                .replace(",", "")
                .replace(".", "")
                .replace("-", "")
                .replace("'", "")
                .replace("\"", "")
                .replace(" ", "")
                ;
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
            super.setComponentWeight(user, tag, Constants.INITIAL_TAG_TO_USER_WEIGHT);
        } else {
            super.setComponentWeight(user, tag, weight + Constants.ADDITIONAL_TAG_TO_USER_WEIGHT);
        }
        return tag;
    }

    public InformationItem scoreTagForMovie(InformationItem movie, InformationItem tag) {
        Double weight = movie.getComponentWeight(tag);
        if (weight == null) {
            super.setComponentWeight(movie, tag, Constants.INITIAL_TAG_TO_MOVIE_WEIGHT);
        } else {
            super.setComponentWeight(movie, tag, weight + Constants.ADDITIONAL_TAG_TO_MOVIE_WEIGHT);
        }
        return tag;
    }

    public void scoreMovieForUser(InformationItem user, InformationItem movie, double rating) {
        for (Map.Entry<InformationItem, Double> entry : movie.getComponents().entrySet()) {
            InformationItem item = entry.getKey();
            Double weight = entry.getValue();

            Double userWeight = user.getComponentWeight(movie);
            if (userWeight ==  null) {
                super.setComponentWeight(user, item, Constants.INITIAL_DIFFUSE_MUTLIPLIER * weight * rating / 5d);
            } else {
                super.setComponentWeight(user, item, userWeight + (Constants.ADDITIONAL_DIFFUSE_MULTIPLIER * weight * rating / 5d));
            }
        }
    }

    public InformationItem getPerson(String name, String lastName) {
        InformationItem p = createInformationItem();
        this.setMeta(p, "PERSON_NAME", String.format("%s#%s", name, lastName));
        return p;
    }

    public InformationItem createPerson(String name, String lastName) {
        Collection<InformationItem> ps = loadByMeta("PERSON_NAME", String.format("%s#%s", name, lastName));
        if (ps.size() == 0)
            return null;
        else
            return ps.iterator().next();
    }

}
