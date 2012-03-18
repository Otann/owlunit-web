package com.owlunit.core.orthodoxal.exception;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class CoreException extends RuntimeException {

    public CoreException() {
    }

    public CoreException(String s) {
        super(s);
    }

    public CoreException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public CoreException(Throwable throwable) {
        super(throwable);
    }

}
