package com.manymonkeys.service.mock;

import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.service.cinema.PosterService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static com.manymonkeys.util.MapUtils.asMap;
import static com.manymonkeys.util.MapUtils.e;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class PosterServiceMock implements PosterService {

    private String protocol;
    private String host;
    private Integer port;
    private String contextPath;

    private static Map<String, String> MOVIE_NAME_2_POSTER_RELATIVE_PATH = asMap(
            e(MovieServiceMock.BIG_LEBOWSKI_MOVIE.getName(), "/resources/mock-posters/thebiglebowski.jpg"),
            e(MovieServiceMock.R_N_G_A_D_MOVIE.getName(), "/resources/mock-posters/ragad.png"),
            e(MovieServiceMock.THE_DUELLISTS.getName(), "/resources/mock-posters/theduellist.jpg"),
            e(MovieServiceMock.WITHNAIL_N_I_MOVIE.getName(), "/resources/mock-posters/withnailni.jpg")
    );

    public PosterServiceMock(String protocol, String host, Integer port, String contextPath) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.contextPath = contextPath;
    }

    @Override
    public URL getPosterUrl(Movie movie) {
        try {
            return new URL(protocol, host, port, contextPath + MOVIE_NAME_2_POSTER_RELATIVE_PATH.get(movie.getName()));
        } catch (MalformedURLException e) {
            //intentionally ignored here. it's just a mock afterall
            return null;
        }
    }

}
