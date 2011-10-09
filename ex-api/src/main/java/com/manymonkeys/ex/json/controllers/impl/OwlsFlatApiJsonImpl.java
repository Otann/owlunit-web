package com.manymonkeys.ex.json.controllers.impl;

import com.manymonkeys.ex.json.controllers.OwlsFlatApi;
import com.manymonkeys.ex.json.exceptions.ObjectNotFoundException;
import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.model.cinema.Person;
import com.manymonkeys.model.cinema.Role;
import com.manymonkeys.service.auth.UserService;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.PersonService;
import com.manymonkeys.service.exception.NotFoundException;
import com.manymonkeys.service.impl.MovieServiceImpl;
import com.manymonkeys.service.impl.PersonServiceImpl;
import com.manymonkeys.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */

@Controller
@RequestMapping(value = "/flatapi")
public class OwlsFlatApiJsonImpl implements OwlsFlatApi {

    public static final String VERSION = "V1";

    @Autowired
    UserService userService;

    @Autowired
    MovieService movieService;

    @Autowired
    PersonService personService;

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
    @RequestMapping(value = "/getsimilarmovie", method = RequestMethod.GET)
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

    @Override
    @RequestMapping(value = "/getrecommendations", method = RequestMethod.GET)
    public Map<Movie, Double> getRecommendations(@RequestParam("login") String login,
                                                 @RequestParam("amount") Long amount,
                                                 @RequestParam("showreasons") boolean showReasons) {
        //Todo This is stub imp
        return Collections.singletonMap(new Movie(null, "Big Lebowski", 1987, "White russian"), 666.0d);
    }

    @Override
    @RequestMapping(value = "/rate", method = RequestMethod.GET)
    public void rate(@RequestParam("login") String login,
                     @RequestParam("movieName") String movieName,
                     @RequestParam("movieYear") Long movieYear,
                     @RequestParam("rate") Double rate) {
        try {
            userService.rate(userService.getUser(login),
                    movieService.loadByName(movieName, movieYear),
                    rate);
        } catch (NotFoundException e) {
            throw new ObjectNotFoundException(e);
        }
    }

    @Override
    @RequestMapping(value = "/like", method = RequestMethod.GET)
    public void like(@RequestParam("login") String login,
                     @RequestParam("movieName") String movieName,
                     @RequestParam("movieYear") Long movieYear,
                     @RequestParam("provider") String provider) {
        try {
            userService.like(userService.getUser(login),
                    movieService.loadByName(movieName, movieYear));
            //Todo at the moment "provider" field is not taken in the consideration
        } catch (NotFoundException e) {
            throw new ObjectNotFoundException(e);
        }
    }

    @Override
    @RequestMapping(value = "/follow", method = RequestMethod.GET)
    public void follow(@RequestParam("followerlogin") String followerLogin,
                       @RequestParam("followedlogin") String followedLogin) {
        try {
            userService.follow(userService.getUser(followerLogin),
                    userService.getUser(followedLogin));
        } catch (NotFoundException e) {
            throw new ObjectNotFoundException(e);
        }
    }

    @Override
    @RequestMapping(value = "/unfollow", method = RequestMethod.GET)
    public void unfollow(@RequestParam("followerlogin") String followerLogin,
                         @RequestParam("followedlogin") String followedLogin) {
        try {
            userService.unfollow(userService.getUser(followerLogin),
                    userService.getUser(followedLogin));
        } catch (NotFoundException e) {
            throw new ObjectNotFoundException(e);
        }
    }

    @Override
    @RequestMapping(value = "/version", method = RequestMethod.GET)
    public String version() {
        return VERSION;
    }

}
