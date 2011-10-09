package com.manymonkeys.service.mock;

import com.manymonkeys.model.cinema.Person;
import com.manymonkeys.model.cinema.Role;
import com.manymonkeys.service.cinema.PersonService;
import com.manymonkeys.service.exception.NotFoundException;

import java.util.Collections;
import java.util.UUID;

/**
 * @author Ilya Pimenov
 *         Xaton Proprietary
 */
public class PersonServiceMock implements PersonService{

    Person PERSON_DOOD = new Person(new UUID(3, 3), "Dood", "Lebowski", Collections.singleton(Role.ACTOR));

    @Override
    public Person createPerson(Person person) {
        return PERSON_DOOD;
    }

    @Override
    public Person addRole(Person person, Role role) throws NotFoundException {
        return PERSON_DOOD;
    }

    @Override
    public Person findOrCreate(Person person) {
        return PERSON_DOOD;
    }

    @Override
    public Person findOrCreate(Person person, Role role) {
        return PERSON_DOOD;
    }
}
