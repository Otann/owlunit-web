package com.manymonkeys.service.auth;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.impl.neo4j.Neo4jInformationItemDaoImpl;
import org.neo4j.graphdb.Transaction;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class UserService extends Neo4jInformationItemDaoImpl {

    public static final String LOGIN = UserService.class.getName() + ".LOGIN";
    public static final String PASSWORD = UserService.class.getName() + ".PASSWORD";

    public static final String USER_CLASS_NAME = UserService.class.getName() + ".USER";

    private static final String SALT = "Humpty Dumpty sat on a wall, Humpty Dumpty had a great fall";

    public InformationItem createUser(String login, String password) {
        //TODO: validate login name to be alphanumeric
        Transaction tx = beginTransaction();
        try {

            InformationItem user = createInformationItem();
            setItemClass(user, USER_CLASS_NAME);
            setMeta(user, LOGIN, login);
            setMeta(user, PASSWORD, getPasswordHash(password));

            tx.success();
            return user;
        } finally {
            tx.finish();
        }
    }

    public InformationItem getUser(String login) {
        try {
            return getByMeta(LOGIN, login).iterator().next();
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
