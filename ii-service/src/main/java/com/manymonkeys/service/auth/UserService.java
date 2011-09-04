package com.manymonkeys.service.auth;

import com.manymonkeys.model.user.User;

/**
 * @author Ilya Pimenov
 *         Xaton Proprietary
 */
public interface UserService {

    User createUser(User user);

    User getUser(String login);

    User setPassword(User user, String password);

    Boolean checkPassword(User user, String password);
}
