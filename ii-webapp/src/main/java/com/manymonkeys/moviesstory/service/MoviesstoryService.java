package com.manymonkeys.moviesstory.service;

import com.manymonkeys.model.cinema.Movie;

import java.io.IOException;
import java.util.List;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public interface MoviesstoryService {

    List<Movie> importUserFacebookMovies(String userAccessToken) throws IOException;
}
