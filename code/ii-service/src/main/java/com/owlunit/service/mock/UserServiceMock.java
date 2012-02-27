package com.owlunit.service.mock;

import com.owlunit.model.auth.User;
import com.owlunit.model.cinema.Movie;
import com.owlunit.service.auth.UserService;
import com.owlunit.service.exception.NotFoundException;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class UserServiceMock implements UserService {

    User USER_BOB = new User(201, "bob", "bobspassword");

    @Override
    public User createUser(User user) {
        return USER_BOB;
    }

    @Override
    public User getUser(String login) throws NotFoundException {
        return USER_BOB;
    }

    @Override
    public User setPassword(User user, String password) throws NotFoundException {
        return USER_BOB;
    }

    @Override
    public Boolean checkPassword(User user, String password) {
        return true;
    }

    @Override
    public User like(User user, Movie movie) throws NotFoundException {
        return USER_BOB;
    }

    @Override
    public User follow(User follower, User followed) throws NotFoundException {
        return USER_BOB;
    }

    @Override
    public User unfollow(User follower, User followed) throws NotFoundException {
        return USER_BOB;
    }

    @Override
    public User rate(User user, Movie movie, Double rate) throws NotFoundException {
        return USER_BOB;
    }
}
