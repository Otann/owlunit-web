package com.manymonkeys.service.impl;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.model.cinema.Person;
import com.manymonkeys.model.cinema.Role;
import com.manymonkeys.service.cinema.PersonService;
import com.manymonkeys.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.manymonkeys.service.impl.util.Utils.itemWithMeta;

/**
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public class PersonServiceImpl implements PersonService {

    @Autowired
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
        Ii meta = itemWithMeta(dao, item);
        return new Person(
                item.getUUID(),
                meta.getMeta(META_KEY_NAME),
                meta.getMeta(META_KEY_SURNAME),
                unpackRoles(meta.getMeta(META_KEY_ROLES))
        );
    }

    static Ii personToIi(IiDao dao, Person person) throws NotFoundException {
        assert(person.getUuid() != null);
        Ii item = dao.load(person.getUuid());
        if (item == null) {
            throw new NotFoundException(String.format("Person(%s)", person.getUuid().toString()));
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
        try {
            item = personToIi(dao, person);
        } catch (NotFoundException e) {
            item = create(person);
        }
        return iiToPerson(dao, item);
    }

    @Override
    public Person findOrCreate(Person person, Role role) {
        Person existingPerson = findOrCreate(person);
        if (existingPerson.getRoles().contains(role)) {
            return existingPerson;
        } else {
            Ii item = dao.load(existingPerson.getUuid());
            item = addRole(item, role);
            return iiToPerson(dao, item);
        }
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \__________________*/

    public Ii create(Person person) {
        Ii personIi = dao.createInformationItem();
        dao.setMetaUnindexed(personIi, CLASS_MARK_KEY, CLASS_MARK_VALUE);
        dao.setMetaUnindexed(personIi, META_KEY_NAME_ID, nameId(person.getName(), person.getSurname()));

        dao.setMeta(personIi, META_KEY_NAME, person.getName());
        dao.setMeta(personIi, META_KEY_SURNAME, person.getSurname());
        dao.setMeta(personIi, META_KEY_ROLES, packRoles(person.getRoles()));
        return personIi;
    }

    private Ii addRole(Ii item, Role role) {
        Ii meta = itemWithMeta(dao, item);
        Set<Role> roles = unpackRoles(meta.getMeta(META_KEY_ROLES));
        if (roles.contains(role)) {
            return meta;
        } else {
            roles.add(role);
            return dao.setMeta(meta, META_KEY_ROLES, packRoles(roles));
        }
    }

    private static String packRoles(Collection<Role> roles) {
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
