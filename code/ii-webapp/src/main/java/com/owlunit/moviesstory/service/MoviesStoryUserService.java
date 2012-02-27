package com.owlunit.moviesstory.service;

import com.owlunit.model.auth.User;
import com.owlunit.moviesstory.model.InvitedUser;
import com.owlunit.moviesstory.model.MoviesStoryUser;
import com.owlunit.service.auth.UserService;
import com.owlunit.service.exception.NotFoundException;

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
