package com.owlunit.moviesstory.service;

import com.owlunit.moviesstory.model.FacebookMovies;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class FacebookIntegrationTests {

    @Test
    public void testFacebookMoviesSerialization() {
        assertTrue(
                FacebookMovies.deserialize("" +
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
                        "}").getData().size() == 5
        );
    }
}
