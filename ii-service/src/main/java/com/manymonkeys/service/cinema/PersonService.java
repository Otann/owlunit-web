package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.InformationItem;
import me.prettyprint.hector.api.Keyspace;

import java.util.Collection;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class PersonService extends TagService {

    public static final String CLASS_MARK_KEY = PersonService.class.getName();
    public static final String CLASS_MARK_VALUE = "#"; // This indicates that item was created through PersonService class

    public static final String PERSON_FIRST_NAME = PersonService.class.getName() + ".PERSON_FIRST_NAME";
    public static final String PERSON_LAST_NAME = PersonService.class.getName() + ".PERSON_LAST_NAME";

    private static final String FULL_NAME_FORMAT = "%s %s";

    public PersonService(Keyspace keyspace) {
        super(keyspace);
    }

    public InformationItem createPerson(String firstName, String lastName) {
        InformationItem person = createTag(getFullPersonName(firstName, lastName));

        setMeta(person, CLASS_MARK_KEY, CLASS_MARK_VALUE);
        setMeta(person, PERSON_FIRST_NAME, firstName);
        setMeta(person, PERSON_LAST_NAME, lastName);

        return person;
    }

    // TODO: seriously, there can be more than one person with same name
    public InformationItem getPerson(String firstName, String lastName) {
        return super.getTag(getFullPersonName(firstName, lastName));
    }

    private String getFullPersonName(String firstName, String lastName) {
        return String.format(FULL_NAME_FORMAT, firstName, lastName);
    }



}