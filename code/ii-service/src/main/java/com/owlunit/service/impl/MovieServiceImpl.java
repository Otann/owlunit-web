package com.owlunit.service.impl;

import com.owlunit.core.algo.Recommender;
import com.owlunit.core.ii.Ii;
import com.owlunit.core.ii.IiDao;
import com.owlunit.service.cinema.MovieService;
import com.owlunit.service.exception.NotFoundException;
import com.owlunit.model.cinema.Role;
import com.owlunit.service.impl.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
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

    private Double initialKeywordWeight;
    private Double initialPersonWeight;

    private Map<Role, Double> initialRoleWeight = new HashMap<Role, Double>();

    private static final String CLASS_MARK_KEY = MovieServiceImpl.class.getName();
    private static final String CLASS_MARK_VALUE = "#";

    private static final String META_KEY_YEAR           = CLASS_MARK_KEY + ".YEAR";
    private static final String META_KEY_NAME           = CLASS_MARK_KEY + ".NAME";
    private static final String META_KEY_PLOT           = CLASS_MARK_KEY + ".PLOT";
    private static final String META_KEY_AKA_NAME       = CLASS_MARK_KEY + ".AKA_NAME";
    private static final String META_KEY_TRANSLATE_NAME = CLASS_MARK_KEY + ".TRANSLATE_NAME";
    private static final String META_SERVICE_KEY        = CLASS_MARK_KEY + ".EXTERNAL_ID.";
    private static final String META_KEY_TAGLINES       = CLASS_MARK_KEY + ".TAGLINES";

    private static final String SIMPLE_NAME     = CLASS_MARK_KEY + ".SIMPLE_NAME";
    private final Pattern simplifyPatter = Pattern.compile("(a |the |, a|, the|,|\\.|\\s|'|\"|:|-|!|#|)");

    private static final String TAGLINES_DELIMETED = "###";

    static Movie iiToMovie(IiDao dao, Ii item) {
        Ii meta = Utils.itemWithMeta(dao, item);
        if (!isMovie(dao, meta)) {
            throw new IllegalArgumentException("This is not a movie");
        }
        return new Movie(
                item.getId(),
                meta.getMeta(META_KEY_NAME),
                Long.parseLong(meta.getMeta(META_KEY_YEAR)),
                meta.getMeta(META_KEY_PLOT)
        );
    }

    static Ii movieToIi(IiDao dao, Movie movie) throws NotFoundException {
        Ii item = dao.load(movie.getId());
        if (item != null) {
            return item;
        } else {
            throw new NotFoundException(String.format("%s (%s)", movie.getName(), movie.getYear()));
        }
    }

    @Override
    public Movie createMovie(Movie movie) {
        Ii movieIi = dao.createInformationItem();
        dao.setMetaUnindexed(movieIi, CLASS_MARK_KEY, CLASS_MARK_VALUE);
        dao.setMetaUnindexed(movieIi, META_KEY_YEAR, Long.toString(movie.getYear()));

        dao.setMeta(movieIi, SIMPLE_NAME, simpleName(movie.getName(), movie.getYear()));
        dao.setMeta(movieIi, META_KEY_NAME, movie.getName());
        return iiToMovie(dao, movieIi);
    }

    @Override
    public Movie loadByName(String name, Long year) throws NotFoundException {
        Ii item = retrieve(name, year);
        return iiToMovie(dao, item);
    }

    @Override
    public Movie loadByExternalId(String service, String externalId) throws NotFoundException {
        Collection<Ii> items = dao.load(META_SERVICE_KEY + service, externalId);
        if (items.isEmpty()) {
            throw new NotFoundException(String.format("%s@%s", externalId, service));
        } else {
            return iiToMovie(dao, items.iterator().next());
        }
    }

    @Override
    public Map<Movie, Double> getMostLike(Movie movie) throws NotFoundException {
        Ii item = movieToIi(dao, movie);
        Map<Movie, Double> result = new HashMap<Movie, Double>();
        Map<Ii, Double> rawResults = recommender.getMostLike(item, dao);
        for (Ii recommendation : rawResults.keySet()) {
            if (isMovie(dao, recommendation)) {
                result.put(iiToMovie(dao, recommendation), rawResults.get(recommendation));
            }
        }
        return result;
    }

    @Override
    public Movie setDescription(Movie movie, String description) throws NotFoundException {
        Ii item = movieToIi(dao, movie);
        item = dao.setMeta(item, META_KEY_PLOT, description);
        return iiToMovie(dao, item);
    }


    @Override
    public Movie addPerson(Movie movie, Person person, Role role) throws NotFoundException {
        double weight;
        if (role == null || !initialRoleWeight.containsKey(role)) {
            weight = initialPersonWeight;
        } else {
            weight = initialRoleWeight.get(role);
        }

        Ii movieIi = movieToIi(dao, movie);
        Ii personIi = PersonServiceImpl.personToIi(dao, person);
        movieIi = dao.setComponentWeight(movieIi, personIi, weight);

        return iiToMovie(dao, movieIi);
    }

    @Override
    public Boolean hasKeyword(Movie movie, Keyword keyword) throws NotFoundException {
        Ii movieIi = movieToIi(dao, movie);
        Ii keywordIi = KeywordServiceImpl.keywordToIi(dao, keyword);

        movieIi = dao.loadComponents(movieIi);
        return movieIi.getComponentWeight(keywordIi) != null;
    }

    @Override
    public Movie addKeyword(Movie movie, Keyword keyword) throws NotFoundException {
        Ii movieIi = movieToIi(dao, movie);
        Ii keywordIi = KeywordServiceImpl.keywordToIi(dao, keyword);

        movieIi = dao.setComponentWeight(movieIi, keywordIi, initialKeywordWeight);
        return iiToMovie(dao, movieIi);
    }

    @Override
    public Movie addTagline(Movie movie, String tagline) throws NotFoundException {
        Ii item = movieToIi(dao, movie);
        Ii meta = Utils.itemWithMeta(dao, item);

        Set<String> taglines = unpackTaglines(meta.getMeta(META_KEY_TAGLINES));
        if (taglines.contains(tagline)) {
            return iiToMovie(dao, meta);
        } else {
            taglines.add(tagline);
            meta = dao.setMeta(meta, META_KEY_TAGLINES, packTaglines(taglines));
            return iiToMovie(dao, meta);
        }
    }

    @Override
    public Movie setAkaName(Movie movie, String akaName) throws NotFoundException {
        Ii item = movieToIi(dao, movie);
        item = dao.setMeta(item, META_KEY_AKA_NAME, akaName);
        return iiToMovie(dao, item);
    }

    @Override
    public Movie setTranslateName(Movie movie, String translateName) throws NotFoundException {
        Ii item = movieToIi(dao, movie);
        item = dao.setMeta(item, META_KEY_TRANSLATE_NAME, translateName);
        return iiToMovie(dao, item);
    }

    @Override
    public Movie setExternalId(Movie movie, String service, String externalId) throws NotFoundException {
        Ii item = movieToIi(dao, movie);
        item = dao.setMeta(item, META_SERVICE_KEY + service, externalId);
        return iiToMovie(dao, item);
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \__________________*/

    private Ii retrieve(String name, Long year) throws NotFoundException {
        Collection<Ii> rawItems = dao.load(SIMPLE_NAME, simpleName(name, year));
        Collection<Ii> items = dao.loadMeta(rawItems);
        if (items.isEmpty()) {
            throw new NotFoundException(String.format("%s (%s)", name, year));
        }
        return items.iterator().next();
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

    private Map<Movie, Double> iisToMovies(Map<Ii, Double> iiMap) {
        Map<Movie, Double> result = new HashMap<Movie, Double>();
        for (Ii key : iiMap.keySet()) {
            result.put(iiToMovie(dao, key), iiMap.get(key));
        }
        return result;
    }

    private static boolean isMovie(IiDao dao, Ii item) {
        return Utils.itemWithMeta(dao, item).getMeta(CLASS_MARK_KEY) != null;
    }

    private static String packTaglines(Collection<String> taglines) {
        if (taglines == null || taglines.isEmpty()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        Iterator<String> iterator = taglines.iterator();
        while (iterator.hasNext()) {
            buffer.append(iterator.next());
            if (iterator.hasNext()) {
                buffer.append(TAGLINES_DELIMETED);
            }
        }
        return buffer.toString();
    }

    private static Set<String> unpackTaglines(String taglinesRaw) {
        if (taglinesRaw == null || taglinesRaw.equals("")) {
            return new HashSet<String>();
        }
        String[] taglines = taglinesRaw.split(TAGLINES_DELIMETED);
        return new HashSet<String>(Arrays.asList(taglines));
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

    public void setInitialPersonWeight(double initialPersonWeight) {
        this.initialPersonWeight = initialPersonWeight;
    }

    public void setInitialRoleWeight(String roleRaw, double weight) {
        Role role = Role.valueOf(roleRaw);
        this.initialRoleWeight.put(role, weight);
    }

}
