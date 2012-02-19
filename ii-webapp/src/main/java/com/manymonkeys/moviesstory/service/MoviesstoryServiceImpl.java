package com.manymonkeys.moviesstory.service;

import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.moviesstory.model.FacebookMovies;
import com.manymonkeys.moviesstory.model.FacebookMovies.FacebookMovie;
import com.manymonkeys.service.cinema.MovieService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class MoviesstoryServiceImpl implements MoviesstoryService {

    private FacebookIntegrationService facebookIntegrationService;

    private MovieService movieService;

    public MoviesstoryServiceImpl(FacebookIntegrationService facebookIntegrationService, MovieService movieService) {
        this.facebookIntegrationService = facebookIntegrationService;
        this.movieService = movieService;
    }

    public MoviesstoryServiceImpl(FacebookIntegrationServiceMock facebookIntegrationServiceMock) {
    }

    public List<Movie> importUserFacebookMovies(String userAccessToken) throws IOException {
        FacebookMovies facebookMovies = facebookIntegrationService.retrieveFacebookMovies(userAccessToken);

        List<Movie> result = new ArrayList<Movie>();
        for (FacebookMovie facebookMovie : facebookMovies.getData()) {
            //Todo IMPT This actually should search for the correct "movie" items in the database
            result.add(new Movie(1l, facebookMovie.getName(), 1937l, "Dummy Movie Description"));
        }

        //Todo And we should update user with the list of movies, imported from his facebook

        return result;
    }
}
