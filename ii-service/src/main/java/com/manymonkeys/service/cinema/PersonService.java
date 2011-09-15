package com.manymonkeys.service.cinema;

import com.manymonkeys.model.cinema.Person;
import com.manymonkeys.model.cinema.Role;
import com.manymonkeys.service.exception.NotFoundException;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface PersonService {

    Person createPerson(Person person);

    Person addRole(Person person, Role role) throws NotFoundException;

    Person findOrCreate(Person person);

    Person findOrCreate(Person person, Role role);
}
