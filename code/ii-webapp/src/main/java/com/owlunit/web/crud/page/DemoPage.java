package com.owlunit.web.crud.page;

import com.owlunit.core.ii.Ii;
import com.owlunit.core.ii.IiDao;
import com.owlunit.model.cinema.Movie;
import com.owlunit.service.cinema.MovieService;
import com.owlunit.service.exception.NotFoundException;
import org.apache.click.control.ActionLink;
import org.apache.click.control.Column;
import org.apache.click.control.Table;
import org.apache.click.util.Bindable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DemoPage extends Template {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String WTF_MESSAGE = "What a Terrible Failure";

    @Autowired
    MovieService movieService;

    @Autowired
    IiDao dao;

    private ActionLink createIiLink = new ActionLink("createIiLink", this, "onCreateIiClick");

    @Bindable
    protected Table table = new Table();


    public String title = "Owl Intro Page";
    public Date time = new Date();

    public DemoPage() {
        addControl(createIiLink);

        table.setClass("ii-table");

        table.addColumn(new Column("uuid"));
        table.addColumn(new Column("name"));
        table.addColumn(new Column("year"));
        table.addColumn(new Column("description"));
    }

    @Override
    public void onInit() {
    }

    @Override
    public void onRender() {
        List<Movie> movies = new ArrayList<Movie>();
        try {
            movies.addAll(movieService.getMostLike(null).keySet());
        } catch (NotFoundException e) {
            log.error(WTF_MESSAGE, e);
        }

        table.setRowList(movies);
    }

    public boolean onCreateIiClick() {
        Ii item = dao.createInformationItem();
        addModel("item", item);
        addModel("uuid", item.getId());
        return true;
    }

}