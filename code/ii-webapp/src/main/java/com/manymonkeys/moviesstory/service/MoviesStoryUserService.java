package com.manymonkeys.moviesstory.service;

import com.manymonkeys.model.auth.User;
import com.manymonkeys.moviesstory.model.InvitedUser;
import com.manymonkeys.moviesstory.model.MoviesStoryUser;
import com.manymonkeys.service.auth.UserService;
import com.manymonkeys.service.exception.NotFoundException;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public interface MoviesStoryUserService extends UserService {

    InvitedUser createInvitedUser(InvitedUser invitedUser);

    Boolean isInvitedUser(User user) throws NotFoundException;

    InvitedUser getInvitedUser(String login) throws NotFoundException;

    MoviesStoryUser createMoviesStoryUser(MoviesStoryUser moviesStoryUser);

    Boolean isMoviesStoryUser(User user) throws NotFoundException;

    MoviesStoryUser getMoviesStoryUser(String login) throws NotFoundException;

}
