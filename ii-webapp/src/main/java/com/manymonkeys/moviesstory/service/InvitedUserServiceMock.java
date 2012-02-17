package com.manymonkeys.moviesstory.service;

import com.manymonkeys.model.auth.User;
import com.manymonkeys.moviesstory.model.InvitedUser;
import com.manymonkeys.service.exception.NotFoundException;
import com.manymonkeys.service.mock.UserServiceMock;

import java.util.Arrays;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class InvitedUserServiceMock extends UserServiceMock implements InvitedUserService {

    private static InvitedUser HEMINGWAY = new InvitedUser(
            666,
            "hemingway",
            "4th-infantry",
            new InvitedUser.InvitedUserExtendedData(
                    "DUMMYINVITEKEY",
                    Arrays.asList("Mark Twain", "Hunting", "Cuba", "Africa", "Corrida")
            )
    );

    @Override
    public InvitedUser createInvitedUser(InvitedUser invitedUser) {
        return HEMINGWAY;
    }

    @Override
    public Boolean isInvitedUser(User user) throws NotFoundException {
        // For some reasons you cannot use simply HEMINGWAY.getId(), even thou that method should've been overriden
        // But may be it is lack of language support. need to investigate this one
        return user.getId() == ((User) HEMINGWAY).getId()
                && user.getLogin().equals(((User) HEMINGWAY).getLogin());
    }

    @Override
    public InvitedUser getInvitedUser(String login) throws NotFoundException {
        return HEMINGWAY;
    }

}
