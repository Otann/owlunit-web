package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import org.springframework.beans.factory.annotation.Autowired;
import static com.manymonkeys.service.cinema.Utils.itemWithMeta;

import java.util.*;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public class PersonService {

    public enum Role {
        ACTOR, DIRECTOR, PRODUCER
    }

    @Autowired
    private IiDao dao;

    private static final String CLASS_MARK_KEY = PersonService.class.getName();
    private static final String CLASS_MARK_VALUE = "#";

    private static final String META_KEY_FULL_NAME = CLASS_MARK_KEY + ".FULL_NAME";
    private static final String META_KEY_ROLES = CLASS_MARK_KEY + ".ROLES";
    private static final String ROLES_DELIMITER = "#";

    public Ii createPerson(String fullName) {
        Ii person = dao.createInformationItem();
        dao.setUnindexedMeta(person, CLASS_MARK_KEY, CLASS_MARK_VALUE);

        dao.setMeta(person, META_KEY_FULL_NAME, fullName);
        dao.setMeta(person, META_KEY_ROLES, "");
        return person;
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

    public Ii findOrCreate(String fullName, Role role) {
        Collection<Ii> persons = this.getPersons(fullName);
        Ii person;
        if (persons.isEmpty()) {
            person = createPerson(fullName);
        } else {
            person = persons.iterator().next();
        }
        return addRole(person, role);
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

}
