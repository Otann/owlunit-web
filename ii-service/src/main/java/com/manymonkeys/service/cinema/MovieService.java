package com.manymonkeys.service.cinema;

import com.manymonkeys.model.cinema.*;
import com.manymonkeys.service.exception.NotFoundException;

import java.util.Map;

/**
 * @author Ilya Pimenov
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public interface MovieService {

    Movie createMovie(Movie movie);

    Movie loadByName(String name, Long year) throws NotFoundException;

    Movie loadByExternalId(String service, String externalId) throws NotFoundException;

    Map<Movie, Double> getMostLike(Movie movie) throws NotFoundException;

    Movie setDescription(Movie movie, String description) throws NotFoundException;

    Movie setTranslateName(Movie movie, String translateName) throws NotFoundException;

    Movie setExternalId(Movie movie, String service, String externalId) throws NotFoundException;

    Movie setAkaName(Movie movie, String akaName) throws NotFoundException;

    Movie addPerson(Movie movie, Person person, Role role) throws NotFoundException;

    Movie addKeyword(Movie movie, Keyword keyword) throws NotFoundException;

    Boolean hasKeyword(Movie movie, Keyword keyword) throws NotFoundException;

    Movie addTagline(Movie movie, String tagline) throws NotFoundException;

}
