package com.manymonkeys.moviesstory.service;

import com.manymonkeys.moviesstory.model.FacebookMovies;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public interface FacebookIntegrationService {

    String constructApplicationAuthenticaionUrl() throws UnsupportedEncodingException;

    String retrieveAccessToken(String code) throws Exception;

    FacebookMovies retrieveFacebookMovies(String userAccessToken) throws IOException;

}
