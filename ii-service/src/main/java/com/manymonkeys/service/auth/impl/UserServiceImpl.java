package com.manymonkeys.service.auth.impl;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import org.springframework.beans.factory.annotation.Autowired;
import static com.manymonkeys.service.cinema.util.Utils.itemWithMeta;


import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class UserServiceImpl {

    @Autowired
    IiDao dao;

    public static final String LOGIN = UserServiceImpl.class.getName() + ".LOGIN";
    private static final String PASSWORD = UserServiceImpl.class.getName() + ".PASSWORD";

    private static final String SALT = "Humpty Dumpty sat on a wall, Humpty Dumpty had a great fall";

    public double followerWeight = 10d;
    public double ratingsMultiplicator = 5d;

    public Ii createUser(String login, String password) {
        //TODO Ilya Pimenov - validate login name to be alphanumeric
        Ii user = dao.createInformationItem();
        dao.setUnindexedMeta(user, LOGIN, login);
        dao.setUnindexedMeta(user, PASSWORD, getPasswordHash(password));
        return user;
    }

    public Ii getUser(String login) {
        try {
            Ii blankUser = dao.load(LOGIN, login).iterator().next();
            return dao.loadMetadata(blankUser);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void follow(Ii follower, Ii followed) {
        dao.setComponentWeight(follower, followed, followerWeight);
    }

    public void unfollow(Ii follower, Ii followed) {
        dao.removeComponent(follower, followed);
    }

    public void rate(Ii user, Ii movie, Double rate) {
        dao.setComponentWeight(user, movie, rate * ratingsMultiplicator);
    }

    public String getLogin(Ii user) {
        return itemWithMeta(dao, user).getMeta(LOGIN);
    }

    public void setPassword(Ii item, String password) {
        dao.setMeta(item, PASSWORD, getPasswordHash(password));
    }

    public boolean checkPassword(Ii user, String password) {
        return getPasswordHash(password).equals(user.getMeta(PASSWORD));
    }

    private static String getPasswordHash(String password) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update((password + SALT).getBytes());
            BigInteger hash = new BigInteger(1, md5.digest());
            return hash.toString(16);
        } catch (NoSuchAlgorithmException e) {
            // Well, there is
            return null;
        }
    }

    public void setFollowerWeight(double followerWeight) {
        this.followerWeight = followerWeight;
    }
}
