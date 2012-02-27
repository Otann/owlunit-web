package com.owlunit.web.application.page;

import com.owlunit.model.cinema.Movie;
import com.owlunit.service.cinema.MovieService;
import com.owlunit.service.cinema.PosterService;
import com.owlunit.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class IntroPage extends BorderTemplate {

    public static final String WTF_MESSAGE = "What a Terrible Failure";

    public static final String PAGE_MODEL_TIME = "time";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MovieService movieService;

    @Autowired
    PosterService posterService;

    public String title = "Owl Intro Page";

    private Date time = new Date();

    public IntroPage() {
        addModel(PAGE_MODEL_TIME, time);
    }

    @Override
    public void onInit() {
        try {

            if (log.isDebugEnabled()) {
                log.debug(String.format("Going to make a call to movieService from %s", this.getClass()));
            }

            Map<Movie, Double> mostLikeMoviesToNull = movieService.getMostLike(null);

            List<Movie> movies = new ArrayList<Movie>();
            List<URL> posters = new ArrayList<URL>();

            for (Movie movie : mostLikeMoviesToNull.keySet()) {
                movies.add(movie);
                posters.add(posterService.getPosterUrl(movie));
            }

            addModel("movies", movies);

            //posters are down, cause they led to tytoalba.ru
            addModel("posters", posters);

        } catch (NotFoundException e) {
            log.error(WTF_MESSAGE, e);
        }
    }

}