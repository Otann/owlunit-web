package com.manymonkeys.service.cinema.impl;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.model.Person;
import com.manymonkeys.model.Role;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static com.manymonkeys.service.Utils.itemWithMeta;

/**
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 *         Owls Proprietary
 */
public class PersonServiceImpl {

    @Autowired
    private IiDao dao;

    private static final String CLASS_MARK_KEY = PersonServiceImpl.class.getName();
    private static final String CLASS_MARK_VALUE = "#";

    private static final String META_KEY_FULL_NAME = CLASS_MARK_KEY + ".FULL_NAME";
    private static final String META_KEY_ROLES = CLASS_MARK_KEY + ".ROLES";
    private static final String ROLES_DELIMITER = "#";

    public Ii createPerson(Person person) {
        Ii personIi = dao.createInformationItem();
        dao.setUnindexedMeta(personIi, CLASS_MARK_KEY, CLASS_MARK_VALUE);

        dao.setMeta(personIi, META_KEY_FULL_NAME, fullName(person));
        dao.setMeta(personIi, META_KEY_ROLES, rolesToString(Collections.singleton(person.getRole())));
        return personIi;
    }

    public Collection<Ii> getPersons(String fullName) {
        Collection<Ii> blankItems = dao.load(META_KEY_FULL_NAME, fullName);
        if (blankItems.isEmpty()) {
            return null;
        }
        return dao.loadMetadata(blankItems);
    }

    public boolean isPerson(Ii item) {
        return itemWithMeta(dao, item).getMeta(CLASS_MARK_KEY) != null;
    }

    public Set<Role> getRoles(Ii person) {
        String rolesRaw = itemWithMeta(dao, person).getMeta(META_KEY_ROLES);
        return rolesFromString(rolesRaw);
    }

    public Ii addRole(Ii person, Role role) {
        Set<Role> roles = getRoles(person);
        if (roles.contains(role)) {
            return person;
        } else {
            roles.add(role);
            return dao.setMeta(person, META_KEY_ROLES, rolesToString(roles));
        }
    }

    public Ii findOrCreate(Person person) {
        Collection<Ii> persons = this.getPersons(fullName(person));
        Ii personIi;
        if (persons.isEmpty()) {
            personIi = createPerson(person);
        } else {
            personIi = persons.iterator().next();
        }
        return personIi;
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \__________________*/

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
        return person.getName() + " " + person.getSurname();
    }

    public void setDao(IiDao dao) {
        this.dao = dao;
    }
}