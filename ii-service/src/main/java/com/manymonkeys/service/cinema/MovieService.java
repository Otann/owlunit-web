package com.manymonkeys.service.cinema;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public class MovieService {

    @Autowired
    protected IiDao dao;

    @Autowired
    protected Recommender recommender;

    private static final String CLASS_MARK_KEY = MovieService.class.getName();
    private static final String CLASS_MARK_VALUE = "#";

    private static final String META_KEY_YEAR = CLASS_MARK_KEY + ".YEAR";
    private static final String META_KEY_NAME = CLASS_MARK_KEY + ".NAME";
    private static final String META_KEY_PLOT = CLASS_MARK_KEY + ".PLOT";

    private static final String EXTERNAL_ID_KEY = CLASS_MARK_KEY + ".EXTERNAL_ID.";
    private static final String SIMPLE_NAME = MovieService.class.getName() + ".SIMPLE_NAME";
    private final Pattern simplifyPatter = Pattern.compile("(a |the |, a|, the|,|\\.|\\s|'|\"|:|-|!|#|)");

    private double initialKeywordWeight = 15;
    private double initialGenreWeight = 50;
    private double initialPersonWeight = 25;
    private Map<PersonService.Role, Double> initialRoleWeight = new HashMap<PersonService.Role, Double>();

    public Ii createMovie(String name, long year) {
        Ii movie = dao.createInformationItem();
        dao.setUnindexedMeta(movie, CLASS_MARK_KEY, CLASS_MARK_VALUE);

        dao.setMeta(movie, META_KEY_NAME, name);
        dao.setMeta(movie, META_KEY_YEAR, Long.toString(year));
        dao.setUnindexedMeta(movie, SIMPLE_NAME, simplifyName(name));
        return movie;
    }

    public Map<Ii, Double> getMostLike(Ii movie) {
        return recommender.getMostLike(movie, dao);
    }

    public Ii createOrUpdateDescription(Ii movie, String description) {
        return dao.setMeta(movie, META_KEY_PLOT, description);
    }

    public Ii loadByExternalId(String service, String id) {
        try {
            return dao.load(EXTERNAL_ID_KEY + service, id).iterator().next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public Ii addPerson(Ii movie, Ii person, PersonService.Role role) {
        double weight;
        if (role == null || initialRoleWeight.containsKey(role)) {
            weight = initialPersonWeight;
        } else {
            weight = initialRoleWeight.get(role);
        }
        return dao.setComponentWeight(movie, person, weight);
    }

    public Ii addKeyword(Ii movie, Ii keyword) {
        return dao.setComponentWeight(movie, keyword, initialKeywordWeight);
    }

    public Ii addTagline(Ii movie, String tagline) {
        throw new UnsupportedOperationException();
    }

    public Ii addAkaName(Ii movie, String akaName, Boolean index) {
        throw new UnsupportedOperationException();
    }

    public Ii addTranslateName(Ii movie, String translateName, Boolean index) {
        throw new UnsupportedOperationException();
    }

    public Ii addGenre(Ii movie, Ii genre) {
        return dao.setComponentWeight(movie, genre, initialGenreWeight);
    }

    public Ii addExternalId(Ii movie, String service, String externalId){
        return dao.setUnindexedMeta(movie, EXTERNAL_ID_KEY + service, externalId);
    }

    public Ii getByNameSimplified(String name) {
        Collection<Ii> blankItems = dao.load(SIMPLE_NAME, simplifyName(name));
        if (blankItems.isEmpty()) {
            return null;
        }
        return dao.loadMetadata(blankItems).iterator().next();
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
        return sb.toString();

    }

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
        PersonService.Role role = PersonService.Role.valueOf(roleRaw);
        this.initialRoleWeight.put(role, weight);
    }

}
