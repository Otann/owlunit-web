package com.manymonkeys.web;

import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class IntroPage extends TemplatePage {

    public static final String WTF_MESSAGE = "What a Terrible Failure";

    public static final String PAGE_MODEL_MESSAGE = "message";
    public static final String PAGE_MODEL_TIME = "time";

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MovieService movieService;

    public String title = "Owl Intro Page";

    private Date time = new Date();

    public IntroPage() {
        addModel(PAGE_MODEL_TIME, time);
    }

    @Override
    public void onInit() {
        try {
            addModel(PAGE_MODEL_MESSAGE, movieService.getMostLike(null));
        } catch (NotFoundException e) {
            log.error(WTF_MESSAGE, e);
        }
    }

}