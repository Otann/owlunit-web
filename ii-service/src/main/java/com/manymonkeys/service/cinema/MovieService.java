package com.manymonkeys.service.cinema;

import com.manymonkeys.model.cinema.*;

import java.util.Map;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface MovieService {

    Movie createMovie(Movie movie);

    Map<Movie, Double> getMostLike(Movie movie);

    Movie createOrUpdateDescription(Movie movie, String description);

    Movie loadByExternalId(String service, String externalId);

    Movie addPerson(Movie movie, Person person, Role role);

    Movie addKeyword(Movie movie, Keyword keyword);

    Movie addTagline(Movie movie, String tagline);

    Movie addAkaName(Movie movie, String akaName, Boolean index);

    Movie addTranslateName(Movie movie, String translateName, Boolean index);

    Movie addGenre(Movie movie, Genre genre);

    Movie addExternalId(Movie movie, String service, String externalId);
}
