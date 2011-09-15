package com.manymonkeys.ex.json.controllers.impl;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.ex.json.controllers.OwlsMovieApi;
import com.manymonkeys.ex.json.exceptions.ObjectNotFoundException;
import com.manymonkeys.model.auth.User;
import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.model.cinema.Person;
import com.manymonkeys.model.cinema.Role;
import com.manymonkeys.service.auth.UserService;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.exception.NotFoundException;
import com.manymonkeys.service.impl.MovieServiceImpl;
import com.manymonkeys.service.impl.PersonServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
@Controller
@RequestMapping(value = "/movie")
public class OwlsMovieApiImpl implements OwlsMovieApi {

    @Autowired
    MovieServiceImpl movieService;

    @Autowired
    PersonServiceImpl personService;

    @Override
    @RequestMapping(value = "/addmovie", method = RequestMethod.POST)
    public void addMovie(@RequestParam("name") String name,
                         @RequestParam("year") Long year,
                         @RequestParam("description") String description,
                         //Todo Ilya Pimenov - Move "persons" to @RequestBody
                         @RequestParam("persons") List<Person> persons) {

        Movie movie = new Movie(null, name, year, null);
        try {
            movie = movieService.createMovie(movie);
            movieService.setDescription(movie, description);
            for (Person person : persons) {
                for (Role role : person.getRoles()) {
                    movieService.addPerson(movie, personService.findOrCreate(person), role);
                }
            }
        } catch (NotFoundException e) {
            throw new ObjectNotFoundException(e);
        }
    }

    @Override
    @RequestMapping(value = "/getsimilar", method = RequestMethod.GET)
    public Map<Movie, Double> getSimilarMovies(@RequestParam("login") String login,
                                               @RequestParam("movieName") String movieName,
                                               @RequestParam("movieName") Long year,
                                               @RequestParam("userId") Long amount,
                                               @RequestParam("userId") boolean showReasons) {

        try {
            Movie movie = movieService.loadByName(movieName, year);
            return movieService.getMostLike(movie);
        } catch (NotFoundException e) {
            throw new ObjectNotFoundException(e);
        }

    }
}
