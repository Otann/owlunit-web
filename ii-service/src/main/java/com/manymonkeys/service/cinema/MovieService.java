package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.InformationItem;
import me.prettyprint.hector.api.Keyspace;

import java.util.NoSuchElementException;

/**
 * Rocket Science Software
 *
 * @author Anton Chebotaev
 */
public class MovieService extends TagService {

    public static final String YEAR = MovieService.class.getName() + ".YEAR";
    public static final String TRANSLATE_NAME = MovieService.class.getName() + ".TRANSLATE_NAME";
    public static final String AKA_NAME = MovieService.class.getName() + ".AKA_NAME";

    public static final String SIMPLE_NAME = MovieService.class.getName() + "SIMPLE_NAME";

    public MovieService(Keyspace keyspace) {
        super(keyspace);
    }

    public InformationItem createMovie(String name, long year) {
        InformationItem movie = createTag(name);
        setMeta(movie, YEAR, Long.toString(year));
        setMeta(movie, SIMPLE_NAME, simplifyName(name), true);
        return movie;
    }

    public InformationItem getByNameSimplified(String name) {
        try {
            return multigetByMeta(SIMPLE_NAME, simplifyName(name)).iterator().next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private String simplifyName(String name) {
        return name.trim().toLowerCase()
                .replace(",", "")
                .replace(".", "")
                .replace(" ", "")
                .replace("'", "")
                .replace("\"", "");
    }

}
