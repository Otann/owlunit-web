package com.manymonkeys.core.ii.exception;

import com.manymonkeys.core.exception.CoreException;

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
