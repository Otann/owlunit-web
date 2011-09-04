package com.manymonkeys.service.auth.impl;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.model.user.User;
import com.manymonkeys.service.auth.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

import static com.manymonkeys.service.cinema.util.Utils.itemWithMeta;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 */
public class UserServiceImpl implements UserService {

    //Todo Anton Chebotaev - Move to ii-service.properties
    public static final String HASH_ALGORITHM = "MD5";
    public static final int HASH_LENGTH = 16;
    @Autowired
    private IiDao dao;

    public static final String LOGIN = UserServiceImpl.class.getName() + ".LOGIN";
    private static final String PASSWORD = UserServiceImpl.class.getName() + ".PASSWORD";

    private static final String SALT = "Humpty Dumpty sat on a wall, Humpty Dumpty had a great fall";

    public double followerWeight = 10d;
    public double ratingsMultiplicator = 5d;

    public User createUser(User user) {
        //TODO Ilya Pimenov - validate login name to be alphanumeric
        Ii userIi = dao.createInformationItem();
        dao.setUnindexedMeta(userIi, LOGIN, user.getLogin());
        dao.setUnindexedMeta(userIi, PASSWORD, getPasswordHash(user.getPassword()));
        return toDomainClass(userIi);
    }

    public User getUser(String login) {
        try {
            return toDomainClass(retrieve(login));
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    //Todo Anton Chebotaev - Remove "Ii" type from contract
    public void follow(Ii follower, Ii followed) {
        dao.setComponentWeight(follower, followed, followerWeight);
    }

    //Todo Anton Chebotaev - Remove "Ii" type from contract
    public void unfollow(Ii follower, Ii followed) {
        dao.removeComponent(follower, followed);
    }

    public void rate(Ii user, Ii movie, Double rate) {
        dao.setComponentWeight(user, movie, rate * ratingsMultiplicator);
    }

    public User setPassword(User user, String password) {
        return toDomainClass(dao.setMeta(retrieve(user), PASSWORD, getPasswordHash(password)));
    }

    public Boolean checkPassword(User user, String password) {
        return getPasswordHash(password).equals(user.getPassword());
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \__________________*/

    private Ii retrieve(User user) {
        return retrieve(user.getLogin());
    }

    private Ii retrieve(String login) {
        Ii blankUser = dao.load(LOGIN, login).iterator().next();
        return dao.loadMetadata(blankUser);
    }

    private User toDomainClass(Ii userIi) {
        return new User(getLogin(userIi), getPassword(userIi));
    }

    private String getLogin(Ii user) {
        return itemWithMeta(dao, user).getMeta(LOGIN);
    }

    private String getPassword(Ii user) {
        return user.getMeta(PASSWORD);
    }

    private static String getPasswordHash(String password) {
        try {
            MessageDigest md5 = MessageDigest.getInstance(HASH_ALGORITHM);
            md5.reset();
            md5.update((password + SALT).getBytes());
            BigInteger hash = new BigInteger(1, md5.digest());
            return hash.toString(HASH_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            // Well, there is

            //Todo Anton Chebotaev - "well, there is" what, my ass? Add exception handling here
            //introduce specific "ApplicationException" class, with appropriate subclasses
            return null;
        }
    }

    /*-- - - - - - - - - - - - - - - - -\
    |   G E T T E R S  &  S E T T E R S |
    \_________________________________ */

    public IiDao getDao() {
        return dao;
    }

    public void setDao(IiDao dao) {
        this.dao = dao;
    }
}
