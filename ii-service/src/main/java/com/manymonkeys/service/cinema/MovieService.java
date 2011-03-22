package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.InformationItem;
import org.neo4j.graphdb.Transaction;

/**
 * Rocket Science Software
 *
 * @author Anton Chebotaev
 */
public class MovieService extends TagService {

    public static final String YEAR = MovieService.class.getName() + ".YEAR";
    public static final String MOVIE_LENS_ID = MovieService.class.getName() + ".MOVIE_LENS_ID";
    public static final String AKA_NAME = MovieService.class.getName() + ".AKA_NAME";

    public static final String MOVIE_CLASS_NAME = MovieService.class.getName() + ".MOVIE_CLASS";

    public InformationItem createMovie(String name, long year) {
        Transaction tx = beginTransaction();
        try {
            InformationItem movie = createTag(name);
            setItemClass(movie, MOVIE_CLASS_NAME);
            setMeta(movie, YEAR, Long.toString(year));
            tx.success();
            return movie;
        } finally {
            tx.finish();
        }
    }



}
