package com.owlunit.core.orthodoxal.ii.exception;

import com.owlunit.core.orthodoxal.exception.CoreException;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class DAOException extends CoreException {

    public DAOException() {
    }

    public DAOException(String s) {
        super(s);
    }

    public DAOException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DAOException(Throwable throwable) {
        super(throwable);
    }

}
