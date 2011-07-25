package com.manymonkeys.security.shiro;

import org.apache.shiro.ShiroException;

/**
 * Owles
 *
 * @author Ilya Pimenov
 */
public class OwledArgumentException extends ShiroException {

    public OwledArgumentException() {
        super();
    }

    public OwledArgumentException(String message) {
        super(message);
    }

    public OwledArgumentException(Throwable cause) {
        super(cause);
    }

    public OwledArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
