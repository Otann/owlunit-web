package com.owlunit.service.cinema;

import com.owlunit.model.cinema.Movie;

import java.net.URL;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public interface PosterService {

    URL getPosterUrl(Movie movie);

}
