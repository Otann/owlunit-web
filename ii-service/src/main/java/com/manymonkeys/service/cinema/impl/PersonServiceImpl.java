package com.manymonkeys.service.cinema.impl;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.model.cinema.Person;
import com.manymonkeys.model.cinema.Role;
import com.manymonkeys.service.cinema.PersonService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.manymonkeys.service.cinema.util.Utils.itemWithMeta;

/**
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public class PersonServiceImpl implements PersonService {

    public static final String FULLNAME_DELIMITER = " ";
    @Autowired
    private IiDao dao;

    private static final String CLASS_MARK_KEY = PersonServiceImpl.class.getName();
    private static final String CLASS_MARK_VALUE = "#";

    private static final String META_KEY_FULL_NAME = CLASS_MARK_KEY + ".FULL_NAME";
    private static final String META_KEY_ROLES = CLASS_MARK_KEY + ".ROLES";
    private static final String ROLES_DELIMITER = "#";

    public Person createPerson(Person person) {
        return toDomainClass(create(person));
    }

    public Boolean isPerson(Person person) {
        return itemWithMeta(dao, retrieve(person)).getMeta(CLASS_MARK_KEY) != null;
    }

    public Person addRole(Person person, Role role) {
        Ii personIi = retrieve(person);
        Set<Role> roles = getRoles(personIi);
        if (roles.contains(role)) {
            return toDomainClass(personIi);
        } else {
            roles.add(role);
            return toDomainClass(dao.setMeta(personIi, META_KEY_ROLES, rolesToString(roles)));
        }
    }

    public Person findOrCreate(Person person) {
        Collection<Ii> persons = this.getPersons(fullName(person));
        Ii personIi;
        if (persons.isEmpty()) {
            personIi = create(person);
        } else {
            personIi = persons.iterator().next();
        }
        return toDomainClass(personIi);
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \__________________*/

    public Ii create(Person person) {
        Ii personIi = dao.createInformationItem();
        dao.setUnindexedMeta(personIi, CLASS_MARK_KEY, CLASS_MARK_VALUE);

        dao.setMeta(personIi, META_KEY_FULL_NAME, fullName(person));
        dao.setMeta(personIi, META_KEY_ROLES, rolesToString(person.getRoles()));
        return personIi;
    }

    /* Ilya Pimenov - special hack with package visibility to use it later in MovieService */
    Ii retrieve(Person person) {
        Collection<Ii> persons = this.getPersons(fullName(person));
        if (persons.isEmpty()) {
            return null;
        } else {
            return persons.iterator().next();
        }
    }

    private Collection<Ii> getPersons(String fullName) {
        Collection<Ii> blankItems = dao.load(META_KEY_FULL_NAME, fullName);
        if (blankItems.isEmpty()) {
            return null;
        }
        return dao.loadMetadata(blankItems);
    }

    private Person toDomainClass(Ii personIi) {
        String[] name = getName(personIi);
        return new Person(name[0], name[1], getRoles(personIi));
    }

    private String[] getName(Ii personIi) {
        return itemWithMeta(dao, personIi).getMeta(META_KEY_FULL_NAME).split(FULLNAME_DELIMITER);
    }

    private Set<Role> getRoles(Ii person) {
        String rolesRaw = itemWithMeta(dao, person).getMeta(META_KEY_ROLES);
        return rolesFromString(rolesRaw);
    }

    private String rolesToString(Collection<Role> roles) {
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

    private Set<Role> rolesFromString(String rolesRaw) {
        String[] roles = rolesRaw.split(ROLES_DELIMITER);
        Set<Role> result = new HashSet<Role>();
        for (String role : roles) {
            result.add(Role.valueOf(role));
        }
        return result;
    }

    private String fullName(Person person) {
        return person.getName() + FULLNAME_DELIMITER + person.getSurname();
    }

    /*-- - - - - - - - - - - - - - - - -\
   |   G E T T E R S  &  S E T T E R S |
   \_________________________________ */

    public void setDao(IiDao dao) {
        this.dao = dao;
    }

}
