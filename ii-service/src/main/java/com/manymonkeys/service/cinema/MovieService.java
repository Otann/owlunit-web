package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.InformationItem;
import me.prettyprint.hector.api.Keyspace;

/**
 * Rocket Science Software
 *
 * @author Anton Chebotaev
 */
public class MovieService extends TagService {

    public static final String YEAR = MovieService.class.getName() + ".YEAR";
    public static final String MOVIE_LENS_ID = MovieService.class.getName() + ".MOVIE_LENS_ID";
    public static final String AKA_NAME = MovieService.class.getName() + ".AKA_NAME";

    public MovieService(Keyspace keyspace) {
        super(keyspace);
    }

    public InformationItem createMovie(String name, long year) {
        InformationItem movie = createTag(name);
        setMeta(movie, YEAR, Long.toString(year));
        return movie;
    }

}
