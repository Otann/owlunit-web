package com.owlunit.service.cinema;

import com.owlunit.model.cinema.Person;
import com.owlunit.model.cinema.Role;
import com.owlunit.service.exception.NotFoundException;

/**
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public interface PersonService {

    Person createPerson(Person person);

    Person addRole(Person person, Role role) throws NotFoundException;

    Person findOrCreate(Person person);
}
