package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.Ii;
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

    private static final String YEAR = MovieService.class.getName() + ".YEAR";
    private static final String TAGLINES = MovieService.class.getName() + ".TAGLINES";
    private static final String PLOT = MovieService.class.getName() + ".PLOT";

    private static final String TRANSLATE_NAME = MovieService.class.getName() + ".TRANSLATE_NAME";
    private static final String AKA_NAME = MovieService.class.getName() + ".AKA_NAME";

    private static final String SIMPLE_NAME = MovieService.class.getName() + ".SIMPLE_NAME";

    private final Pattern simplifyPatter = Pattern.compile("(a |the |, a|, the|,|\\.|\\s|'|\"|:|-|!|#|)");

    public MovieService(Keyspace keyspace) {
        super(keyspace);
    }

    public Ii createMovie(String name, String year) {
        Ii movie = createTag(name);
        setMeta(movie, YEAR, year);
        setMeta(movie, SIMPLE_NAME, simplifyName(name), true);
        return movie;
    }

    public void createOrUpdateDescription(Ii movie, String description) {
        setMeta(movie, MovieService.PLOT, description);
    }

    public void addPerson(Ii movie, Ii person, Double weight) {
        this.setComponentWeight(movie, person, weight);
    }

    public void addKeyword(Ii movie, Ii keyword, Double weight) {
        this.setComponentWeight(movie, keyword, weight);
    }

    public void addTagline(Ii movie, String tagline) {
        this.setMeta(movie, MovieService.TAGLINES, tagline);
    }

    public void addAkaName(Ii movie, String akaName, Boolean index) {
        this.setMeta(movie, MovieService.AKA_NAME, akaName, index);
    }

    public void addTranslateName(Ii movie, String translateName, Boolean index) {
        this.setMeta(movie, MovieService.TRANSLATE_NAME, translateName, index);
    }

    public void addGenre(Ii movie, Ii genre, Double weight) {
        this.setComponentWeight(movie, genre, weight);
    }

    public void addExternalId(Ii movie, String storeKey, String externalId, Boolean index){
        this.setMeta(movie, storeKey, externalId, index);
    }

    public Ii getByNameSimplified(String name) {
        try {
            return loadByMeta(SIMPLE_NAME, simplifyName(name)).iterator().next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    private String simplifyName(String name) {

        String unromanized = name
                .replace(" I", " 1")
                .replace(" II", " 2")
                .replace(" III", " 3")
                .replace(" IV", " 4")
                .replace(" V", " 5")
                .replace(" VI", " 6")
                .replace(" VII", " 7")
                .replace(" VIII", " 8")
                .replace(" IX", " 9");

        // delete unnecessary sequences
        Matcher matcher = simplifyPatter.matcher(unromanized.toLowerCase());
        StringBuffer sb = new StringBuffer();
        while (matcher.find())
            matcher.appendReplacement(sb, "");
        matcher.appendTail(sb);

        // replace roman characters
        return sb.toString();

    }

}
