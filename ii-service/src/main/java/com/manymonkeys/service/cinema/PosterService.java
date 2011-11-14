package com.manymonkeys.service.cinema;

import com.manymonkeys.model.cinema.Movie;

import java.net.URL;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public interface PosterService {

    URL getPosterUrl(Movie movie);
}
