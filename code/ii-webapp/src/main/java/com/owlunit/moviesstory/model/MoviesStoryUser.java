package com.owlunit.moviesstory.model;

import com.google.gson.Gson;
import com.owlunit.model.auth.User;

import java.util.List;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class MoviesStoryUser extends User {

    private FacebookUserExtendedData facebookUserExtendedData;
    private MoviesStoryUserExtendedData moviesStoryUserExtendedData;

    public MoviesStoryUser(long id, String login, String password, FacebookUserExtendedData facebookUserExtendedData, MoviesStoryUserExtendedData moviesStoryUserExtendedData) {
        super(id, login, password);
        this.facebookUserExtendedData = facebookUserExtendedData;
        this.moviesStoryUserExtendedData = moviesStoryUserExtendedData;
    }

    public FacebookUserExtendedData getFacebookUserExtendedData() {
        return facebookUserExtendedData;
    }

    public void setFacebookUserExtendedData(FacebookUserExtendedData facebookUserExtendedData) {
        this.facebookUserExtendedData = facebookUserExtendedData;
    }

    public MoviesStoryUserExtendedData getMoviesStoryUserExtendedData() {
        return moviesStoryUserExtendedData;
    }

    public void setMoviesStoryUserExtendedData(MoviesStoryUserExtendedData moviesStoryUserExtendedData) {
        this.moviesStoryUserExtendedData = moviesStoryUserExtendedData;
    }

    //Can be moved to a separate class, independent from Moviesstory, but as
    //we store all extended data with it's full qualified class name key,
    //it should not be a problem later on
    public static class FacebookUserExtendedData {
        private String userPicture;
        private String backgroundPicture;
        private String facebookProfileLink;
        private String biography;

        public FacebookUserExtendedData(String userPicture, String backgroundPicture, String facebookProfileLink, String biography) {
            this.userPicture = userPicture;
            this.backgroundPicture = backgroundPicture;
            this.facebookProfileLink = facebookProfileLink;
            this.biography = biography;
        }

        public FacebookUserExtendedData() {
        }

        public String getUserPicture() {
            return userPicture;
        }

        public void setUserPicture(String userPicture) {
            this.userPicture = userPicture;
        }

        public String getBackgroundPicture() {
            return backgroundPicture;
        }

        public void setBackgroundPicture(String backgroundPicture) {
            this.backgroundPicture = backgroundPicture;
        }

        public String getFacebookProfileLink() {
            return facebookProfileLink;
        }

        public void setFacebookProfileLink(String facebookProfileLink) {
            this.facebookProfileLink = facebookProfileLink;
        }

        public String getBiography() {
            return biography;
        }

        public void setBiography(String biography) {
            this.biography = biography;
        }

        public static FacebookUserExtendedData deserialize(String value) {
            return new Gson().fromJson(value, FacebookUserExtendedData.class);
        }

        public static String serialize(FacebookUserExtendedData value) {
            return new Gson().toJson(value);
        }

        public String serialize() {
            return serialize(this);
        }
    }

    public static class MoviesStoryUserExtendedData {
        List<Long> watchlistMovieIds;

        public MoviesStoryUserExtendedData(List<Long> watchlistMovieIds) {
            this.watchlistMovieIds = watchlistMovieIds;
        }

        public MoviesStoryUserExtendedData() {
        }

        public List<Long> getWatchlistMovieIds() {
            return watchlistMovieIds;
        }

        public void setWatchlistMovieIds(List<Long> watchlistMovieIds) {
            this.watchlistMovieIds = watchlistMovieIds;
        }

        public static MoviesStoryUserExtendedData deserialize(String value) {
            return new Gson().fromJson(value, MoviesStoryUserExtendedData.class);
        }

        public static String serialize(MoviesStoryUserExtendedData value) {
            return new Gson().toJson(value);
        }

        public String serialize() {
            return serialize(this);
        }
    }


}
