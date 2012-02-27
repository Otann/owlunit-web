package com.owlunit.service.impl;

import com.owlunit.model.cinema.Movie;
import com.owlunit.service.cinema.PosterService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class PosterServiceImpl implements PosterService {
    @Override
    public URL getPosterUrl(Movie movie) {
        try {
            return new URL("http://placehold.it/200x300");
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
