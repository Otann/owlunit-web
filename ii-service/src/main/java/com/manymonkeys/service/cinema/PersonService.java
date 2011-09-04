package com.manymonkeys.service.cinema;

import com.manymonkeys.model.cinema.Person;
import com.manymonkeys.model.cinema.Role;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface PersonService {

    Person createPerson(Person person);

    Boolean isPerson(Person person);

    Person addRole(Person person, Role role);

    Person findOrCreate(Person person);
}
