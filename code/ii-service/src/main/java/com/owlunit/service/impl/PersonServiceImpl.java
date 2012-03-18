package com.owlunit.service.impl;

import com.owlunit.core.orthodoxal.ii.Ii;
import com.owlunit.core.orthodoxal.ii.IiDao;
import com.owlunit.model.cinema.Person;
import com.owlunit.service.cinema.PersonService;
import com.owlunit.service.exception.NotFoundException;
import com.owlunit.service.impl.util.Utils;

import java.util.*;

/**
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public class PersonServiceImpl implements PersonService {

    private IiDao dao;

    private static final String CLASS_MARK_KEY = PersonServiceImpl.class.getName();
    private static final String CLASS_MARK_VALUE = "#";

    private static final String META_KEY_NAME_ID = CLASS_MARK_KEY + ".NAME_ID";
    private static final String META_KEY_NAME = CLASS_MARK_KEY + ".NAME";
    private static final String META_KEY_SURNAME = CLASS_MARK_KEY + ".SURNAME";
    private static final String META_KEY_ROLES = CLASS_MARK_KEY + ".ROLES";

    private static final String ROLES_DELIMITER = "#";
    private static final String NAME_ID_DELIMITER = "###";

    static Person iiToPerson(IiDao dao, Ii item) {
        if (!isPerson(dao, item)) {
            throw new IllegalArgumentException("This is not a person");
        }
        Ii meta = Utils.itemWithMeta(dao, item);
        return new Person(
                item.getId(),
                meta.getMeta(META_KEY_NAME),
                meta.getMeta(META_KEY_SURNAME),
                unpackRoles(meta.getMeta(META_KEY_ROLES))
        );
    }

    static Ii personToIi(IiDao dao, Person person) throws NotFoundException {
        Ii item = dao.load(person.getId());
        if (item == null) {
            throw new NotFoundException(String.format("Person(%d)", person.getId()));
        } else {
            return item;
        }
    }

    @Override
    public Person createPerson(Person person) {
        return iiToPerson(dao, create(person));
    }

    @Override
    public Person addRole(Person person, Role role) throws NotFoundException {
        Ii item = personToIi(dao, person);
        Ii meta = addRole(item, role);
        return iiToPerson(dao, meta);
    }

    @Override
    public Person findOrCreate(Person person) {
        Ii item;
        if (person.getId() == 0) {
            item = create(person);
        } else {
            try {
                item = loadByName(person.getName(), person.getSurname());
            } catch (NotFoundException e) {
                item = create(person);
            }
        }
        return iiToPerson(dao, item);
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \__________________*/

    private Ii create(Person person) {
        Ii personIi = dao.createInformationItem();
        dao.setMetaUnindexed(personIi, CLASS_MARK_KEY, CLASS_MARK_VALUE);
        dao.setMetaUnindexed(personIi, META_KEY_NAME_ID, nameId(person.getName(), person.getSurname()));
        dao.setMetaUnindexed(personIi, META_KEY_ROLES, packRoles(person.getRoles()));

        dao.setMeta(personIi, META_KEY_NAME, person.getName());
        dao.setMeta(personIi, META_KEY_SURNAME, person.getSurname());
        return personIi;
    }
    
    private Ii loadByName(String name, String surname) throws NotFoundException {
        if (name == null || name.equals("") || surname == null || surname.equals("")) {
            throw new IllegalArgumentException();
        }

        Collection<Ii> persons = dao.load(META_KEY_NAME_ID, nameId(name, surname));
        if (persons.isEmpty()) {
            throw new NotFoundException(name + " " + surname);
        } else {
            return persons.iterator().next();
        }
    }

    private Ii addRole(Ii item, Role role) {
        Ii meta = Utils.itemWithMeta(dao, item);
        Set<Role> roles = unpackRoles(meta.getMeta(META_KEY_ROLES));
        if (roles.contains(role)) {
            return meta;
        } else {
            roles.add(role);
            return dao.setMeta(meta, META_KEY_ROLES, packRoles(roles));
        }
    }

    private static boolean isPerson(IiDao dao, Ii item) {
        return Utils.itemWithMeta(dao, item).getMeta(CLASS_MARK_KEY) != null;
    }

    private static String packRoles(Collection<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        Iterator<Role> iterator = roles.iterator();
        while (iterator.hasNext()) {
            Role role = iterator.next();
            buffer.append(role.name());
            if (iterator.hasNext()) {
                buffer.append(ROLES_DELIMITER);
            }
        }
        return buffer.toString();
    }

    private static Set<Role> unpackRoles(String rolesRaw) {
        if (rolesRaw == null || rolesRaw.equals("")) {
            return new HashSet<Role>();
        }
        String[] roles = rolesRaw.split(ROLES_DELIMITER);
        Set<Role> result = new HashSet<Role>();
        for (String role : roles) {
            result.add(Role.valueOf(role));
        }
        return result;
    }

    private static String nameId(String name, String surname) {
        return name + NAME_ID_DELIMITER + surname;
    }

    /*-- - - - - - - - - - - - - - - - -\
   |   G E T T E R S  &  S E T T E R S |
   \_________________________________ */

    public void setDao(IiDao dao) {
        this.dao = dao;
    }

}
