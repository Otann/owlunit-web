package com.owlunit.service.mock;

import com.owlunit.model.cinema.Person;
import com.owlunit.service.cinema.PersonService;
import com.owlunit.service.exception.NotFoundException;

import java.util.Collections;

/**
 * @author Ilya Pimenov
 *         Xaton Proprietary
 */
public class PersonServiceMock implements PersonService {

    Person PERSON_DOOD = new Person(101, "Dood", "Lebowski", Collections.singleton(Role.ACTOR));

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
}
