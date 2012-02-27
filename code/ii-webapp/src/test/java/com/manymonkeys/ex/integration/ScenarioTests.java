package com.manymonkeys.ex.integration;

import com.manymonkeys.ex.json.controllers.OwlsFlatApi;
import com.manymonkeys.model.cinema.Movie;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static junit.framework.Assert.assertNotNull;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        locations = {
                "/context.xml"
        }
)
public class ScenarioTests {

    @Autowired
    OwlsFlatApi service;

    @Before
    public void before() {
        //
    }

    @Test
    public void testVersionMethod() {
        String version = service.version();

        System.out.println(String.format("Ex-Api protocol version is %s", version));

        assertNotNull(service.version());
    }

    @Test
    public void scenario1GetSimiliarMovies() {
        String login = "login";
        String movieName = "movieName";
        Long movieYear = 1988l;
        Long amount = 1l;
        Boolean showReson = true;

        Map<Movie, Double> similiarMovies = service.getSimilarMovies(login, movieName, movieYear, amount, showReson);

        System.out.println(String.format("Got similiar movies %s", similiarMovies));

        assertNotNull(similiarMovies);
    }
}
