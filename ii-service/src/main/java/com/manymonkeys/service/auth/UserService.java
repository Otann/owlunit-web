package com.manymonkeys.service.auth;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.impl.cassandra.CassandraInformationItemDaoImpl;
import me.prettyprint.hector.api.Keyspace;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class UserService extends CassandraInformationItemDaoImpl {

    public static final String LOGIN = UserService.class.getName() + ".LOGIN";
    private static final String PASSWORD = UserService.class.getName() + ".PASSWORD";

    public static final String USER_CLASS_NAME = UserService.class.getName() + ".USER";

    private static final String SALT = "Humpty Dumpty sat on a wall, Humpty Dumpty had a great fall";

    public UserService(Keyspace keyspace) {
        super(keyspace);
    }

    public InformationItem createUser(String login, String password) {
        //TODO: validate login name to be alphanumeric
        InformationItem user = createInformationItem();
        setMeta(user, LOGIN, login);
        setMeta(user, PASSWORD, getPasswordHash(password));
        return user;
    }

    public InformationItem getUser(String login) {
        try {
            return multigetByMeta(LOGIN, login).iterator().next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void setPassword(InformationItem item, String password) {
        setMeta(item, PASSWORD, getPasswordHash(password));
    }

    public boolean checkPassword(InformationItem user, String password) {
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

}
