package com.manymonkeys.moviesstory.service;

import com.manymonkeys.moviesstory.model.FacebookMovies;

import java.io.IOException;
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

    @Override
    public FacebookMovies retrieveFacebookMovies(String userAccessToken) throws IOException {
        return FacebookMovies.deserialize(
                "{\n" +
                        "   \"data\": [\n" +
                        "      {\n" +
                        "         \"name\": \"The Duellists\",\n" +
                        "         \"category\": \"Movie\",\n" +
                        "         \"id\": \"104097032960518\",\n" +
                        "         \"created_time\": \"2012-02-18T23:19:30+0000\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"name\": \"The Wrestler\",\n" +
                        "         \"category\": \"Movie\",\n" +
                        "         \"id\": \"108417482513662\",\n" +
                        "         \"created_time\": \"2012-02-18T23:19:30+0000\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"name\": \"Breaking the Waves\",\n" +
                        "         \"category\": \"Movie\",\n" +
                        "         \"id\": \"109502099076243\",\n" +
                        "         \"created_time\": \"2012-02-18T23:19:30+0000\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"name\": \"The Limits of Control\",\n" +
                        "         \"category\": \"Movie\",\n" +
                        "         \"id\": \"72898589127\",\n" +
                        "         \"created_time\": \"2012-02-18T23:19:29+0000\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"name\": \"The Evil Dead\",\n" +
                        "         \"category\": \"Movie\",\n" +
                        "         \"id\": \"109619395730981\",\n" +
                        "         \"created_time\": \"2012-02-18T23:19:29+0000\"\n" +
                        "      }\n" +
                        "   ],\n" +
                        "   \"paging\": {\n" +
                        "      \"next\": \"https://graph.facebook.com/me/movies?access_token=AAAAAAITEghMBAIGeiDskYEDRZCigTTUDSZC2ZBpNsHyrC6h5qKF3z24fPDKz50ZBGcRzLt8FWGWam4YUH5p5WWFAwW0ZAPq3giWYQWd8ZA0Jsrhol8SFqD&limit=5000&offset=5000&__after_id=109619395730981\"\n" +
                        "   }\n" +
                        "}"
        );

    }

}
