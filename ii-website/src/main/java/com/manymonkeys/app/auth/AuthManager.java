package com.manymonkeys.app.auth;

import com.manymonkeys.app.MainAppLevelWindow;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.auth.UserService;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.browsercookies.BrowserCookies;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Configurable(preConstruction = true)
public class AuthManager {

    @Autowired
    private UserService service;

    private BrowserCookies cookies;
    private final String COOKIE_NAME_LOGIN = "uuid";
    private final String COOKIE_NAME_CHECK = "check";
    private final String COOKIE_SALT = "Humpty Dumpty sat on a wall, Humpty Dumpty had a great fall";

    public AuthManager(BrowserCookies cookies) {
        this.cookies = cookies;
    }

    public static AuthManager getCurrent(Component component) {
        Window window = component.getApplication().getMainWindow();
        if (window instanceof MainAppLevelWindow) {
            return ((MainAppLevelWindow) window).getAuthManager();
        } else {
            return null;
        }
    }

    public InformationItem authenticate(String login, String password) throws AuthException {
        InformationItem user = service.getUser(login);
        if (user == null) {
            throw new AuthException(String.format("User %s not found", login));
        }

        if (!service.checkPassword(user, password)) {
            throw new AuthException("Password don't match");
        }

        cookies.setCookie(COOKIE_NAME_LOGIN, login);
        cookies.setCookie(COOKIE_NAME_CHECK, getSalty(login));

        return user;
    }

    public InformationItem getCurrentUser() throws AuthException {
        String login = cookies.getCookie(COOKIE_NAME_LOGIN);
        if (login == null) {
            return null;
        }

        InformationItem user = service.getUser(login);

        if (user == null) {
            throw new AuthException("Can't load current user, try to clean cookies");
        }

        String check = cookies.getCookie(COOKIE_NAME_CHECK);
        if (!getSalty(login).equals(check)) {
            throw new AuthException("Malicious login detected, try to clean cookies");
        }

        return user;
    }

    public void deathenticate() {
        Date now = new Date();
        cookies.setCookie(COOKIE_NAME_LOGIN, "", now);
        cookies.setCookie(COOKIE_NAME_CHECK, "", now);
    }

    public InformationItem createUser(String login, String password) throws AuthException {
        if (service.getUser(login) != null) {
            throw new AuthException("User with this login already exists");
        }
        service.createUser(login, password);
        return authenticate(login, password);
    }

    public static class AuthException extends Exception {
        public AuthException(String message, Throwable cause) {
            super(message, cause);
        }
        public AuthException(String message) {
            super(message);
        }
    }

    private String getSalty(String s) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update((s + COOKIE_SALT).getBytes());
            BigInteger hash = new BigInteger(1, md5.digest());
            return hash.toString(16);
        } catch (NoSuchAlgorithmException e) {
            // Well, there is
            return null;
        }
    }
}
