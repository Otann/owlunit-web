package com.owlunit.moviesstory.model;

import com.google.gson.Gson;

import java.util.List;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class FacebookMovies {

    private List<FacebookMovie> data;

    private Paging paging;

    public static FacebookMovies deserialize(String facebookMovieListString) {
        return new Gson().<FacebookMovies>fromJson(facebookMovieListString, FacebookMovies.class);
    }

    public List<FacebookMovie> getData() {
        return data;
    }

    public void setData(List<FacebookMovie> data) {
        this.data = data;
    }

    public Paging getPaging() {
        return paging;
    }

    public void setPaging(Paging paging) {
        this.paging = paging;
    }

    public static class Paging {
        private String next;

        public Paging() {
        }

        public String getNext() {
            return next;
        }

        public void setNext(String next) {
            this.next = next;
        }
    }

    public static class FacebookMovie {
        private String name;
        private String category;
        private String id;
        private String created_time;

        public FacebookMovie() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getCreated_time() {
            return created_time;
        }

        public void setCreated_time(String created_time) {
            this.created_time = created_time;
        }
    }
}
