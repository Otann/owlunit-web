package com.manymonkeys.service.mock;

import com.manymonkeys.model.cinema.Keyword;
import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.model.cinema.Person;
import com.manymonkeys.model.cinema.Role;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.exception.NotFoundException;

import java.util.Map;
import java.util.UUID;

import static com.manymonkeys.util.MapUtils.asMap;
import static com.manymonkeys.util.MapUtils.e;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class MovieServiceMock implements MovieService {


    //package visibility is important to use in PosterServiceMock class
    static Movie BIG_LEBOWSKI_MOVIE = new Movie(new UUID(3, 3),
            "Big Lebowski",
            1998,
            "From dood's to all the doods and doodeses");
    static Movie R_N_G_A_D_MOVIE = new Movie(new UUID(3, 3),
            "Rosencrantz And Guildenstern Are Dead",
            1990,
            "Directed by Tom Stoppard himself, a different look at classic Shakespeare play");
    static Movie WITHNAIL_N_I_MOVIE = new Movie(new UUID(3, 3),
            "Withnail & I",
            1986,
            "Written and directed by Bruce Robinson and is based on his life in London in the late 1960s");
    static Movie THE_DUELLISTS = new Movie(new UUID(3, 3),
            "The Duellists",
            1977,
            "Historical drama film that was Ridley Scott's first feature film as a director");

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
        return asMap(e(BIG_LEBOWSKI_MOVIE, 13d),
                e(R_N_G_A_D_MOVIE, 17d),
                e(WITHNAIL_N_I_MOVIE, 21d),
                e(THE_DUELLISTS, 23d));
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
