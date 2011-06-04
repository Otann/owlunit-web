package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.InformationItem;
import me.prettyprint.hector.api.Keyspace;

import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rocket Science Software
 *
 * @author Anton Chebotaev
 */
public class MovieService extends TagService {

    public static final String YEAR = MovieService.class.getName() + ".YEAR";
    public static final String TRANSLATE_NAME = MovieService.class.getName() + ".TRANSLATE_NAME";
    public static final String AKA_NAME = MovieService.class.getName() + ".AKA_NAME";

    public static final String SIMPLE_NAME = MovieService.class.getName() + ".SIMPLE_NAME";

    private final Pattern simplifyPatter = Pattern.compile("(a |the |, a|, the|,|\\.|\\s|'|\"|:|-|!|#|)");

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
            return loadByMeta(SIMPLE_NAME, simplifyName(name)).iterator().next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private String simplifyName(String name) {

        String unromanized = name
                .replace(" I",    " 1")
                .replace(" II",   " 2")
                .replace(" III",  " 3")
                .replace(" IV",   " 4")
                .replace(" V",    " 5")
                .replace(" VI",   " 6")
                .replace(" VII",  " 7")
                .replace(" VIII", " 8")
                .replace(" IX",   " 9");

        // delete unnecessary sequences
        Matcher matcher = simplifyPatter.matcher(unromanized);
        StringBuffer sb = new StringBuffer();
        while (matcher.find())
            matcher.appendReplacement(sb, "");
        matcher.appendTail(sb);

        // replace roman characters
        return sb.toString();

    }

}
