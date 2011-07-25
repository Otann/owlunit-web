package com.manymonkeys.security.shiro;

import org.apache.shiro.ShiroException;

/**
 * Owls
 *
 * @author Ilya Pimenov
 */
public class OwledMethodException extends ShiroException {

    public OwledMethodException() {
        super();
    }

    public OwledMethodException(String message) {
        super(message);
    }

    public OwledMethodException(Throwable cause) {
        super(cause);
    }

    public OwledMethodException(String message, Throwable cause) {
        super(message, cause);
    }
}
