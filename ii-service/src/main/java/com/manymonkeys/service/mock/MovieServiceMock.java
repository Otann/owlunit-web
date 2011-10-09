package com.manymonkeys.service.mock;

import com.manymonkeys.model.cinema.Keyword;
import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.model.cinema.Person;
import com.manymonkeys.model.cinema.Role;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.exception.NotFoundException;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class MovieServiceMock implements MovieService {

    Movie BIG_LEBOWSKI_MOVIE = new Movie(new UUID(3, 3), "Big Lebowski", 1987, "From dood's to all the doods and doodeses");

    @Override
    public Movie createMovie(Movie movie) {
        return BIG_LEBOWSKI_MOVIE;
    }

    @Override
    public Movie loadByName(String name, Long year) throws NotFoundException {
        return BIG_LEBOWSKI_MOVIE;
    }

    @Override
    public Movie loadByExternalId(String service, String externalId) throws NotFoundException {
        return BIG_LEBOWSKI_MOVIE;
    }

    @Override
    public Map<Movie, Double> getMostLike(Movie movie) throws NotFoundException {
        return Collections.singletonMap(BIG_LEBOWSKI_MOVIE, 13d);
    }

    @Override
    public Movie setDescription(Movie movie, String description) throws NotFoundException {
        return BIG_LEBOWSKI_MOVIE;
    }

    @Override
    public Movie setTranslateName(Movie movie, String translateName) throws NotFoundException {
        return BIG_LEBOWSKI_MOVIE;
    }

    @Override
    public Movie setExternalId(Movie movie, String service, String externalId) throws NotFoundException {
        return BIG_LEBOWSKI_MOVIE;
    }

    @Override
    public Movie setAkaName(Movie movie, String akaName) throws NotFoundException {
        return BIG_LEBOWSKI_MOVIE;
    }

    @Override
    public Movie addPerson(Movie movie, Person person, Role role) throws NotFoundException {
        return BIG_LEBOWSKI_MOVIE;
    }

    @Override
    public Movie addKeyword(Movie movie, Keyword keyword) throws NotFoundException {
        return BIG_LEBOWSKI_MOVIE;
    }

    @Override
    public Boolean hasKeyword(Movie movie, Keyword keyword) throws NotFoundException {
        return true;
    }

    @Override
    public Movie addTagline(Movie movie, String tagline) throws NotFoundException {
        return BIG_LEBOWSKI_MOVIE;
    }
}
