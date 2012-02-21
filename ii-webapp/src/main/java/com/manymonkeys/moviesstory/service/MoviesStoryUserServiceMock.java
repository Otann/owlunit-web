package com.manymonkeys.moviesstory.service;

import com.manymonkeys.model.auth.User;
import com.manymonkeys.moviesstory.model.InvitedUser;
import com.manymonkeys.moviesstory.model.MoviesStoryUser;
import com.manymonkeys.service.exception.NotFoundException;
import com.manymonkeys.service.mock.UserServiceMock;

import java.util.Arrays;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class MoviesStoryUserServiceMock extends UserServiceMock implements MoviesStoryUserService {

    private static InvitedUser INVITED_HEMINGWAY = new InvitedUser(
            666,
            "hemingway",
            "4th-infantry",
            new InvitedUser.InvitedUserExtendedData(
                    "DUMMYINVITEKEY",
                    Arrays.asList("Mark Twain", "Hunting", "Cuba", "Africa", "Corrida")
            )
    );

    private static MoviesStoryUser MOVIESSTORY_HEMINGWAY = new MoviesStoryUser(
            666,
            "hemingway",
            "4th-infantry",
            new MoviesStoryUser.FacebookUserExtendedData("mockedUserPicturePath", "mockedBackgroundPath", "mockedFacebookProfileLink", "mockedBiography"),
            new MoviesStoryUser.MoviesStoryUserExtendedData(Arrays.asList(666l, 667l, 668l, 669l, 670l))
    );

    @Override
    public InvitedUser createInvitedUser(InvitedUser invitedUser) {
        return INVITED_HEMINGWAY;
    }

    @Override
    public Boolean isInvitedUser(User user) throws NotFoundException {
        // For some reasons you cannot use simply INVITED_HEMINGWAY.getId(), even thou that method should've been overriden
        // But may be it is lack of language support. need to investigate this one
        return user.getId() == ((User) INVITED_HEMINGWAY).getId()
                && user.getLogin().equals(((User) INVITED_HEMINGWAY).getLogin());
    }

    @Override
    public InvitedUser getInvitedUser(String login) throws NotFoundException {
        return INVITED_HEMINGWAY;
    }

    @Override
    public MoviesStoryUser createMoviesStoryUser(MoviesStoryUser moviesStoryUser) {
        return MOVIESSTORY_HEMINGWAY;
    }

    @Override
    public Boolean isMoviesStoryUser(User user) throws NotFoundException {
        return true;
    }

    @Override
    public MoviesStoryUser getMoviesStoryUser(String login) throws NotFoundException {
        return MOVIESSTORY_HEMINGWAY;
    }

}
