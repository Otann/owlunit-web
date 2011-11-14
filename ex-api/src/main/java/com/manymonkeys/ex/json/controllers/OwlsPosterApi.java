package com.manymonkeys.ex.json.controllers;

import java.net.URL;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public interface OwlsPosterApi {

    /**
     * Constructs appropriate poster URL for movie identified with:
     *
     * @param movieName of the movie
     * @param movieYear of the movie
     */
    String getPoster(String movieName, Long movieYear);
}
