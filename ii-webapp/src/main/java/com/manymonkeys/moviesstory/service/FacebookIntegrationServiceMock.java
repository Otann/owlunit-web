package com.manymonkeys.moviesstory.service;

import java.io.UnsupportedEncodingException;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class FacebookIntegrationServiceMock implements FacebookIntegrationService {

    @Override
    public String constructApplicationAuthenticaionUrl() throws UnsupportedEncodingException {
        /*
        This will allow to get back at invite page, in the same manner as facebook does, suplying "code" in response arguments
         */
        return "http://localhost:8080/ii-weapp/application/page/invite-landing-page.htm?code=CORRECTMOCKCODE";
    }

    @Override
    public String retrieveAccessToken(String code) throws Exception {
        return "CORRECTUSERTOKENMOCK";
    }

}
