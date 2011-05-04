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

    public static final String PERSON_FIRST_NAME = PersonService.class.getName() + ".PERSON_FIRST_NAME";
    public static final String PERSON_LAST_NAME = PersonService.class.getName() + ".PERSON_LAST_NAME";

    private static final String FULL_NAME_FORMAT = "%s %s";

    public PersonService(Keyspace keyspace) {
        super(keyspace);
    }

    public InformationItem createPerson(String firstName, String lastName) {
        String fullPersonName = String.format(FULL_NAME_FORMAT, firstName, lastName);
        InformationItem person = createTag(fullPersonName); // note that indexes for first and last name created here already

        setMeta(person, PERSON_FIRST_NAME, firstName);
        setMeta(person, PERSON_LAST_NAME, lastName);

        return person;
    }

    // TODO: seriously, there can be more than one person with same name
    public InformationItem getPerson(String firstName, String lastName) {
        String fullPersonName = String.format(FULL_NAME_FORMAT, firstName, lastName);
        return super.getTag(fullPersonName);
    }



}
