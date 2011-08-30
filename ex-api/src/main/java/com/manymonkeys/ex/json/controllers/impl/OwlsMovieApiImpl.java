package com.manymonkeys.ex.json.controllers.impl;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.ex.json.controllers.OwlsMovieApi;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
@Controller
@RequestMapping(value = "/movie")
public class OwlsMovieApiImpl implements OwlsMovieApi {

    @Autowired
    MovieService movieService;

    @Autowired
    PersonService personService;

    @Override
    @RequestMapping(value = "/addmovie", method = RequestMethod.POST)
    public void addMovie(@RequestParam("name") String name,
                         @RequestParam("year") String year,
                         @RequestParam("description") String description,
                         //Todo Ilya Pimenov - Move "persons" to @RequestBody
                         @RequestParam("persons") List<Person> persons) {
        Ii movie = movieService.createMovie(name, Long.parseLong(year));
        movieService.createOrUpdateDescription(movie, description);
        for (Person person : persons) {
            movieService.addPerson(
                    movie,
                    personService.findOrCreate(
                            person.getName() + " " + person.getSurname(),
                            PersonService.Role.valueOf(person.getRole().name())),
                    PersonService.Role.valueOf(person.getRole().name()));
        }
    }

    @Override
    public Map<Ii, Double> getSimilarMovies(String userId, String movieName, Long amount, boolean showReasons) {
        Ii movie = movieService.getByNameSimplified(movieName);
        movieService.getMostLike(movie);
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
