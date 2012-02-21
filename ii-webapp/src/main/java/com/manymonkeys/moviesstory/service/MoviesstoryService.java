package com.manymonkeys.moviesstory.service;

import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.moviesstory.model.MoviesStoryUser;

import java.io.IOException;
import java.util.List;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public interface MoviesStoryService {

    List<Movie> importUserFacebookMovies(String userAccessToken) throws IOException;
    
    MoviesStoryUser importUserFacebookData(String userAcessToken) throws IOException;
}
