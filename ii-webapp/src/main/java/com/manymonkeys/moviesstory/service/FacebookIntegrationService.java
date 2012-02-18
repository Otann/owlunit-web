package com.manymonkeys.moviesstory.service;

import java.io.UnsupportedEncodingException;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public interface FacebookIntegrationService {

    String constructApplicationAuthenticaionUrl() throws UnsupportedEncodingException;

    String retrieveAccessToken(String code) throws Exception;

}
