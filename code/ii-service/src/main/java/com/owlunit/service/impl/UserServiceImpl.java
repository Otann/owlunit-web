package com.owlunit.service.impl;

import com.owlunit.core.orthodoxal.ii.Ii;
import com.owlunit.core.orthodoxal.ii.IiDao;
import com.owlunit.model.auth.User;
import com.owlunit.model.cinema.Movie;
import com.owlunit.service.auth.UserService;
import com.owlunit.service.exception.NotFoundException;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import static com.owlunit.service.impl.MovieServiceImpl.movieToIi;
import static com.owlunit.service.impl.util.Utils.itemWithMeta;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 */
public class UserServiceImpl implements UserService {

    protected IiDao dao;

    public double defaultFollowerWeight = 10d;
    public double defaultLikeWeight = 10d;
    public double defaultRatingsMultiplicator = 5d;

    protected static final String CLASS_MARK_KEY = UserServiceImpl.class.getName();
    protected static final String CLASS_MARK_VALUE = "#";

    protected static final String META_KEY_LOGIN = CLASS_MARK_KEY + ".LOGIN";
    protected static final String META_KEY_PASSWORD = CLASS_MARK_KEY + ".PASSWORD";
    protected static final String SALT = "Humpty Dumpty sat on a wall, Humpty Dumpty had a great fall";

    //Todo Anton Chebotaev - Move to ii-service.properties
    public static final String HASH_ALGORITHM = "MD5";
    public static final int HASH_LENGTH = 16;

    protected static Ii userToIi(IiDao dao, User user) throws NotFoundException {
        Ii item = dao.load(user.getId());
        if (item == null) {
            throw new NotFoundException(String.format("User(%s)", user.getLogin()));
        } else {
            return item;
        }
    }

    protected static User iiToUser(IiDao dao, Ii item) {
        Ii meta = itemWithMeta(dao, item);
        return new User(
                item.getId(),
                meta.getMeta(META_KEY_LOGIN),
                meta.getMeta(META_KEY_PASSWORD)
        );
    }
    
    protected Ii createUserIi(User user){
        Ii userIi = dao.createInformationItem();
        dao.setMetaUnindexed(userIi, CLASS_MARK_KEY, CLASS_MARK_VALUE);
        dao.setMetaUnindexed(userIi, META_KEY_LOGIN, user.getLogin());
        dao.setMetaUnindexed(userIi, META_KEY_PASSWORD, getPasswordHash(user.getPassword()));
        return userIi;
    }
    

    @Override
    public User createUser(User user) {
        //TODO Ilya Pimenov - validate login name to be alphanumeric
        return iiToUser(dao, createUserIi(user));
    }

    @Override
    public User getUser(String login) throws NotFoundException {
        Collection<Ii> items = dao.load(META_KEY_LOGIN, login);
        if (items.isEmpty()) {
            throw new NotFoundException(String.format("User(%s)", login));
        } else {
            return iiToUser(dao, items.iterator().next());
        }
    }

    @Override
    public User setPassword(User user, String password) throws NotFoundException {
        Ii item = userToIi(dao, user);
        item = dao.setMeta(item, META_KEY_PASSWORD, getPasswordHash(password));
        return iiToUser(dao, item);
    }

    @Override
    public Boolean checkPassword(User user, String password) {
        return getPasswordHash(password).equals(user.getPassword());
    }

    public User follow(User follower, User followed) throws NotFoundException {
        Ii followerIi = userToIi(dao, follower);
        Ii followedIi = userToIi(dao, followed);
        followerIi = dao.setComponentWeight(followerIi, followedIi, defaultFollowerWeight);
        return iiToUser(dao, followerIi);
    }

    public User unfollow(User follower, User followed) throws NotFoundException {
        Ii followerIi = userToIi(dao, follower);
        Ii followedIi = userToIi(dao, followed);
        followerIi = dao.removeComponent(followerIi, followedIi);
        return iiToUser(dao, followerIi);
    }

    public User rate(User user, Movie movie, Double rate) throws NotFoundException {
        Ii userIi = userToIi(dao, user);
        Ii movieIi = movieToIi(dao, movie);
        userIi = dao.setComponentWeight(userIi, movieIi, rate * defaultRatingsMultiplicator);
        return iiToUser(dao, userIi);
    }

    @Override
    public User like(User user, Movie movie) throws NotFoundException {
        Ii userIi = userToIi(dao, user);
        Ii movieIi = movieToIi(dao, movie);
        userIi = dao.setComponentWeight(userIi, movieIi, defaultLikeWeight);
        return iiToUser(dao, userIi);
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \__________________*/

    private static String getPasswordHash(String password) {
        try {
            MessageDigest md5 = MessageDigest.getInstance(HASH_ALGORITHM);
            md5.reset();
            md5.update((password + SALT).getBytes());
            BigInteger hash = new BigInteger(1, md5.digest());
            return hash.toString(HASH_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            // Well, there is
            //TODO introduce specific "ApplicationException" class, with appropriate subclasses
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