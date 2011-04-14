package com.manymonkeys.app.auth;

import com.vaadin.data.validator.CompositeValidator;
import com.vaadin.data.validator.RegexpValidator;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class PasswordValidator extends CompositeValidator {

    public static PasswordValidator instance;

    public static PasswordValidator getValidator() {
        if (instance == null) {
            instance = new PasswordValidator();
        }
        return instance;
    }

    public PasswordValidator() {
        addValidator(new RegexpValidator("^[a-zA-Z0-9_-]*$", "Only alphanumeric allowed in password"));
        addValidator(new RegexpValidator("^.*[a-zA-Z].*$", "Letters should be in password"));
        addValidator(new RegexpValidator("^.*[0-9].*$", "Number should be in password"));
    }


}
