package com.manymonkeys.moviesstory.service;

import com.manymonkeys.model.auth.User;
import com.manymonkeys.moviesstory.model.InvitedUser;
import com.manymonkeys.service.auth.UserService;
import com.manymonkeys.service.exception.NotFoundException;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public interface InvitedUserService extends UserService {

    InvitedUser createInvitedUser(InvitedUser invitedUser);

    Boolean isInvitedUser(User user) throws NotFoundException;

    InvitedUser getInvitedUser(String login) throws NotFoundException;

}
