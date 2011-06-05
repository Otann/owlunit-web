package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.InformationItem;
import me.prettyprint.hector.api.Keyspace;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class PersonService extends TagService {

    public static final String CLASS_MARK_KEY = PersonService.class.getName();
    public static final String CLASS_MARK_VALUE = ".#"; // This indicates that item was created through PersonService class

    public static final String PERSON_FIRST_NAME = CLASS_MARK_KEY + ".PERSON_FIRST_NAME";
    public static final String PERSON_LAST_NAME = CLASS_MARK_KEY + ".PERSON_LAST_NAME";

    public static final String SEARCH_KEY = PersonService.class.getName();
    public static final String SEARCH_FORMAT = "%s#%s"; // This indicates that item was created through PersonService class

    private static final String FULL_NAME_FORMAT = "%s %s";

    public PersonService(Keyspace keyspace) {
        super(keyspace);
    }

    public InformationItem createPerson(String firstName, String lastName) {
        InformationItem person = createTag(String.format(FULL_NAME_FORMAT, firstName, lastName));

        setMeta(person, CLASS_MARK_KEY, CLASS_MARK_VALUE);
        setMeta(person, SEARCH_KEY, String.format(SEARCH_FORMAT, firstName, lastName));
        setMeta(person, PERSON_FIRST_NAME, firstName);
        setMeta(person, PERSON_LAST_NAME, lastName);

        return person;
    }

    public InformationItem getPerson(String firstName, String lastName) {
        try {
            return loadByMeta(SEARCH_KEY, String.format(SEARCH_FORMAT, firstName, lastName)).iterator().next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

}
