package com.manymonkeys.service.auth;

import com.manymonkeys.model.auth.User;
import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.service.exception.NotFoundException;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface UserService {

    User createUser(User user);

    User getUser(String login) throws NotFoundException;

    User setPassword(User user, String password) throws NotFoundException;

    Boolean checkPassword(User user, String password);

    User like(User user, Movie movie) throws NotFoundException;

    User follow(User follower, User followed) throws NotFoundException;

    User unfollow(User follower, User followed) throws NotFoundException;

    User rate(User user, Movie movie, Double rate) throws NotFoundException;

}