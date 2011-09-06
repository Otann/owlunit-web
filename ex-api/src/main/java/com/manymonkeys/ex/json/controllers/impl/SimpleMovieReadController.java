package com.manymonkeys.ex.json.controllers.impl;

import com.manymonkeys.core.ii.Ii;
<<<<<<< HEAD
import com.manymonkeys.service.cinema.MovieService;
=======
>>>>>>> All pending changes
import com.manymonkeys.service.cinema.impl.MovieServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/movies")
public class SimpleMovieReadController {

    @Autowired
    MovieServiceImpl movieService;

    /**
     * Try by going to -
     * <p/>
     * http://localhost:8080/ex-api-1.0/movies/getbyname?movieName=Dead
     * <p/>
     * Result will be like -
     * {"components":{},
     *      "parents":{},
     *      "uuid":"eb928e84-a7be-11e0-95ac-53145286faee",
     *      "metaMap":{"CREATED BY":"com.manymonkeys.service.cinema.impl.MovieServiceImpl",
     *          "com.manymonkeys.service.cinema.impl.KeywordServiceImpl":"#",
     *          "com.manymonkeys.service.cinema.impl.MovieServiceImpl.TAGLINES":"A vast, merry, and uncommon tale of love.\n",
     *          "com.manymonkeys.service.cinema.impl.MovieServiceImpl.PLOT":"The film is an updated version of James Joyce's short story,
     *                  detailing a small house party at the Morkan sisters' residence, attended by friends and family.
     *                  Among the visiting attendees are the sisters' nephew Gabriel and his wife Gretta. The
     *                  evening's reminiscences bring up various interesting and a bit comedic conversations.
     *                  Before the end of the evening, melancholy events & memories arise. ",
     *          "com.manymonkeys.service.cinema.impl.MovieServiceImpl.SIMPLE_NAME":"dead",
     *          "com.manymonkeys.service.cinema.impl.MovieServiceImpl.YEAR":"1987",
     *          "com.manymonkeys.service.cinema.impl.KeywordServiceImpl.NAME":"Dead, The",
     *          "com.manymonkeys.crawlers.movielens.MovieLensMoviesParser.SERVICE_NAME":"4098"}}
     *
     * For more complex names, request can go like -
     * <p/>
     * http://localhost:8080/ex-api-1.0/movies/getbyname?movieName=Pulp Fiction
     *
     * @param movieName
     * @return
     */
    @RequestMapping(value = "/getbyname", method = RequestMethod.GET)
    public
    @ResponseBody
    Ii getByName(@RequestParam String movieName) {
        return movieService.getByNameSimplified(movieName);
    }
    /*
    @RequestMapping(method = RequestMethod.GET)
    public String getCreateForm(Model model) {
        model.addAttribute(new ExMovieModel(13L, 27L, "The Legends Of The Fall"));
        return "movies/crudForm";
    }

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, ? extends Object> create(@RequestBody ExMovieModel jsonMovie, HttpServletResponse response) {
        System.out.println("create method was called with " + jsonMovie);
        return Collections.singletonMap("movieFullName", "The Legends Of The Fall");
    }

    @RequestMapping(value = "{internalId}", method = RequestMethod.GET)
    public
    @ResponseBody
    ExMovieModel get(@PathVariable Long internalId) {
        System.out.println("get method was called with internalId = " + internalId);
        return new ExMovieModel(13L, 27L, "The Legends Of The Fall");
    }
    */
}
