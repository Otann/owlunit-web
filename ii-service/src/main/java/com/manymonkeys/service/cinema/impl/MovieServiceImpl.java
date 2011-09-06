package com.manymonkeys.service.cinema.impl;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.model.cinema.*;
import com.manymonkeys.service.cinema.KeywordService;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import sun.java2d.loops.GeneralRenderer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public class MovieServiceImpl implements MovieService {

    @Autowired
    protected IiDao dao;

    @Autowired
    protected Recommender recommender;

    @Autowired
    protected PersonService personService;

    private static final String CLASS_MARK_KEY = MovieServiceImpl.class.getName();
    private static final String CLASS_MARK_VALUE = "#";

    private static final String META_KEY_YEAR = CLASS_MARK_KEY + ".YEAR";
    private static final String META_KEY_NAME = CLASS_MARK_KEY + ".NAME";
    private static final String META_KEY_PLOT = CLASS_MARK_KEY + ".PLOT";

    private static final String EXTERNAL_ID_KEY = CLASS_MARK_KEY + ".EXTERNAL_ID.";
    private static final String SIMPLE_NAME = MovieServiceImpl.class.getName() + ".SIMPLE_NAME";
    private final Pattern simplifyPatter = Pattern.compile("(a |the |, a|, the|,|\\.|\\s|'|\"|:|-|!|#|)");

    //Todo Anton Chebotaev - Move to configuration files (take a look at resources/ii-service.properties)
    private double initialKeywordWeight = 15;
    private double initialGenreWeight = 50;
    private double initialPersonWeight = 25;

    private Map<Role, Double> initialRoleWeight = new HashMap<Role, Double>();

    @Override
    public Movie loadByName(String name) {
        return toDomainClass(retrieveByName(name));
    }

    @Override
    public Movie createMovie(Movie movie) {
        Ii movieIi = dao.createInformationItem();
        dao.setUnindexedMeta(movieIi, CLASS_MARK_KEY, CLASS_MARK_VALUE);

        dao.setMeta(movieIi, META_KEY_NAME, movie.getName());
        dao.setMeta(movieIi, META_KEY_YEAR, Long.toString(movie.getYear()));
        dao.setUnindexedMeta(movieIi, SIMPLE_NAME, simplifyName(movie.getName()));
        return toDomainClass(movieIi);
    }

    @Override
    public Map<Movie, Double> getMostLike(Movie movie) {
        return toDomainClass(recommender.getMostLike(retrieve(movie), dao));
    }

    @Override
    public Movie createOrUpdateDescription(Movie movie, String description) {
        return toDomainClass(dao.setMeta(retrieve(movie), META_KEY_PLOT, description));
    }

    @Override
    public Movie loadByExternalId(String service, String externalId) {
        try {
            //Todo Anton Chebotaev - Method should be rewriteen to return "null" without catching exception
            //add "if" block with iterator.hasNext if necessary
            return toDomainClass(dao.load(EXTERNAL_ID_KEY + service, externalId).iterator().next());
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    @Override
    public Movie addPerson(Movie movie, Person person, Role role) {
        double weight;
        if (role == null || initialRoleWeight.containsKey(role)) {
            weight = initialPersonWeight;
        } else {
            weight = initialRoleWeight.get(role);
        }

        /* Note force type casting, not sure how to elegantly avoid this trick,
         * feel free to change */
        return toDomainClass(
                dao.setComponentWeight(retrieve(movie),
                        ((PersonServiceImpl) personService).retrieve(person),
                        weight));
    }

    @Override
    public Boolean hasKeyword(Movie movie, Keyword keyword) {
        return retrieve(movie).getComponentWeight(retrieve(keyword)) != null;
    }

    @Override
    public Movie addKeyword(Movie movie, Keyword keyword) {
        return toDomainClass(dao.setComponentWeight(retrieve(movie),
                retrieve(keyword),
                initialKeywordWeight));
    }

    @Override
    public Movie addTagline(Movie movie, String tagline) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Movie addAkaName(Movie movie, String akaName, Boolean index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Movie addTranslateName(Movie movie, String translateName, Boolean index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Genre genreKeyword(Keyword keyword){
        return new Genre(keyword.getName());
    }

    @Override
    public Movie addGenre(Movie movie, Genre genre) {
        return toDomainClass(
                dao.setComponentWeight(retrieve(movie),
                        retrieve(genre),
                        initialGenreWeight));
    }

    @Override
    public Movie addExternalId(Movie movie, String service, String externalId) {
        return toDomainClass(
                dao.setUnindexedMeta(
                        retrieve(movie),
                        EXTERNAL_ID_KEY + service,
                        externalId));
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \__________________*/

    private Ii retrieve(Movie movie) {
        return retrieveByName(movie.getName());
    }

    private Ii retrieve(Keyword keyword) {
        //Todo Impt Anton Chebotaev - Implemente
        return null;
    }

    private Ii retrieve(Genre genre) {
        //Todo Impt Anton Chebotaev - Implemente
        return null;
    }

    private Ii retrieveByName(String name) {
        Collection<Ii> blankItems = dao.load(SIMPLE_NAME, simplifyName(name));
        if (blankItems.isEmpty()) {
            return null;
        }
        return dao.loadMetadata(blankItems).iterator().next();
    }

    private String simplifyName(String name) {
        String unromanized = name
                //Todo Anton Chebotaev - put all this into configuration string, and parse later on on "create"
                //method invocation
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
        return sb.toString();

    }

    private Movie toDomainClass(Ii movieIi) {
        return new Movie(getName(movieIi),
                getYear(movieIi),
                getDescription(movieIi));
    }

    private Map<Movie, Double> toDomainClass(Map<Ii, Double> iiMap) {
        Map<Movie, Double> result = new HashMap<Movie, Double>();
        for (Ii key : iiMap.keySet()) {
            result.put(toDomainClass(key), iiMap.get(key));
        }
        return result;
    }

    private String getName(Ii movieIi) {
        //Todo Impt Anton Chebotaev - implemente
        return null;
    }

    private Long getYear(Ii movieIi) {
        //Todo Impt Anton Chebotaev - Implemente
        return null;
    }

    private String getDescription(Ii movieIi) {
        //Todo Impt Anton Chebotaev - Implemente
        return null;
    }

    /*-- - - - - - - - - - - - - - - - -\
    |   G E T T E R S  &  S E T T E R S |
    \_________________________________ */

    public void setDao(IiDao dao) {
        this.dao = dao;
    }

    public void setRecommender(Recommender recommender) {
        this.recommender = recommender;
    }

    public void setInitialKeywordWeight(double initialKeywordWeight) {
        this.initialKeywordWeight = initialKeywordWeight;
    }

    public void setInitialGenreWeight(double initialGenreWeight) {
        this.initialGenreWeight = initialGenreWeight;
    }

    public void setInitialPersonWeight(double initialPersonWeight) {
        this.initialPersonWeight = initialPersonWeight;
    }

    public void setInitialRoleWeight(String roleRaw, double weight) {
        Role role = Role.valueOf(roleRaw);
        this.initialRoleWeight.put(role, weight);
    }

}
