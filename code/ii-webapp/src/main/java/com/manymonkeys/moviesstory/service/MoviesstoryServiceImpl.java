package com.manymonkeys.moviesstory.service;

import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.moviesstory.model.FacebookMovies;
import com.manymonkeys.moviesstory.model.FacebookMovies.FacebookMovie;
import com.manymonkeys.moviesstory.model.MoviesStoryUser;
import com.manymonkeys.service.cinema.MovieService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class MoviesStoryServiceImpl implements MoviesStoryService {

    private FacebookIntegrationService facebookIntegrationService;

    private MovieService movieService;

    private MoviesStoryUserService moviesStoryUserService;

    public MoviesStoryServiceImpl(FacebookIntegrationService facebookIntegrationService, MovieService movieService, MoviesStoryUserService moviesStoryUserService) {
        this.facebookIntegrationService = facebookIntegrationService;
        this.movieService = movieService;
        this.moviesStoryUserService = moviesStoryUserService;
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

    @Override
    public MoviesStoryUser importUserFacebookData(String userAcessToken) throws IOException {
        MoviesStoryUser moviesStoryUser = null;

        /*Todo implement import & save of the rest users's information (e.g. as in importUserFacebookMovies
          we do export "movies information") and save it via MovieStoryUserService */

        return moviesStoryUserService.createMoviesStoryUser(moviesStoryUser);
    }
}
