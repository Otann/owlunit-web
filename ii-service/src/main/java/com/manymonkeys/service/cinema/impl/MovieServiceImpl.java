package com.manymonkeys.service.cinema.impl;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.model.cinema.*;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.PersonService;
import com.manymonkeys.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.manymonkeys.service.cinema.util.Utils.itemWithMeta;

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

    @Autowired
    private Double initialKeywordWeight;
    @Autowired
    private Double initialGenreWeight;
    @Autowired
    private Double initialPersonWeight;

    private Map<Role, Double> initialRoleWeight = new HashMap<Role, Double>();

    private static final String CLASS_MARK_KEY = MovieServiceImpl.class.getName();
    private static final String CLASS_MARK_VALUE = "#";

    private static final String META_KEY_YEAR = CLASS_MARK_KEY + ".YEAR";
    private static final String META_KEY_NAME = CLASS_MARK_KEY + ".NAME";
    private static final String META_KEY_PLOT = CLASS_MARK_KEY + ".PLOT";
    private static final String META_SERVICE_KEY = CLASS_MARK_KEY + ".EXTERNAL_ID.";

    private static final String SIMPLE_NAME     = CLASS_MARK_KEY + ".SIMPLE_NAME";
    private final Pattern simplifyPatter = Pattern.compile("(a |the |, a|, the|,|\\.|\\s|'|\"|:|-|!|#|)");


    @Override
    public Movie createMovie(Movie movie) {
        Ii movieIi = dao.createInformationItem();
        dao.setUnindexedMeta(movieIi, CLASS_MARK_KEY, CLASS_MARK_VALUE);

        dao.setMeta(movieIi, META_KEY_NAME, movie.getName());
        dao.setUnindexedMeta(movieIi, META_KEY_YEAR, Long.toString(movie.getYear()));
        dao.setUnindexedMeta(movieIi, SIMPLE_NAME, simpleName(movie.getName(), movie.getYear()));
        return toMovie(movieIi);
    }

    @Override
    public Movie load(String name, Long year) throws NotFoundException {
        return toMovie(retrieve(name, year));
    }

    @Override
    public Movie load(String service, String externalId) throws NotFoundException {
        Collection<Ii> items = dao.load(META_SERVICE_KEY + service, externalId);
        if (items.isEmpty()) {
            throw new NotFoundException(String.format("%s@%s", externalId, service));
        } else {
            return toMovie(items.iterator().next());
        }
    }

    @Override
    public Map<Movie, Double> getMostLike(Movie movie) throws NotFoundException {
        return toMovie(recommender.getMostLike(movieToIi(movie), dao));
    }

    @Override
    public Movie setDescription(Movie movie, String description) throws NotFoundException {
        return toMovie(dao.setMeta(movieToIi(movie), META_KEY_PLOT, description));
    }


    @Override
    public Movie addPerson(Movie movie, Person person, Role role) throws NotFoundException {
        double weight;
        if (role == null || initialRoleWeight.containsKey(role)) {
            weight = initialPersonWeight;
        } else {
            weight = initialRoleWeight.get(role);
        }

        /* Note force type casting, not sure how to elegantly avoid this trick,
         * feel free to change */
        return toMovie(
                dao.setComponentWeight(movieToIi(movie),
                        ((PersonServiceImpl) personService).retrieve(person),
                        weight));
    }

    @Override
    public Boolean hasKeyword(Movie movie, Keyword keyword) throws NotFoundException {
        return movieToIi(movie).getComponentWeight(keywordToIi(keyword)) != null;
    }

    @Override
    public Movie addKeyword(Movie movie, Keyword keyword) throws NotFoundException {
        return toMovie(dao.setComponentWeight(movieToIi(movie), keywordToIi(keyword), initialKeywordWeight));
    }

    @Override
    public Movie addTagline(Movie movie, String tagline) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Movie setAkaName(Movie movie, String akaName, Boolean index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Movie setTranslateName(Movie movie, String translateName, Boolean index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Movie addGenre(Movie movie, Genre genre) throws NotFoundException {
        Ii item = dao.setComponentWeight(movieToIi(movie), genreToIi(genre), initialGenreWeight);
        return toMovie(item);
    }

    @Override
    public Movie setExternalId(Movie movie, String service, String externalId) throws NotFoundException {
        Ii item = dao.setUnindexedMeta(movieToIi(movie), META_SERVICE_KEY + service, externalId);
        return toMovie(item);
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \__________________*/

    private Ii movieToIi(Movie movie) throws NotFoundException {
        if (movie.getUuid() != null) {
            Ii item = dao.load(movie.getUuid());
            if (item != null) {
                return item;
            } else {
                throw new NotFoundException(String.format("%s (%s)", movie.getName(), movie.getYear()));
            }
        } else {
            return retrieve(movie.getName(), movie.getYear());
        }
    }

    private Ii retrieve(String name, Long year) throws NotFoundException {
        Collection<Ii> rawItems = dao.load(SIMPLE_NAME, simpleName(name, year));
        Collection<Ii> items = dao.loadMetadata(rawItems);
        if (items.isEmpty()) {
            throw new NotFoundException(String.format("%s (%s)", name, year));
        }
        for (Ii item : items) {
            if(Long.parseLong(item.getMeta(META_KEY_YEAR)) == year) {
                return item;
            }
        }
        throw new NotFoundException(String.format("%s (%s)", name, year));
    }

    private String simpleName(String name, Long year) {
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
        sb.append(year);
        return sb.toString();

    }

    private Movie toMovie(Ii movieIi) {
        Ii meta = itemWithMeta(dao, movieIi);
        return new Movie(movieIi.getUUID(),
                meta.getMeta(META_KEY_NAME),
                Long.parseLong(meta.getMeta(META_KEY_YEAR)),
                meta.getMeta(META_KEY_PLOT));
    }

    private Map<Movie, Double> toMovie(Map<Ii, Double> iiMap) {
        Map<Movie, Double> result = new HashMap<Movie, Double>();
        for (Ii key : iiMap.keySet()) {
            result.put(toMovie(key), iiMap.get(key));
        }
        return result;
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
