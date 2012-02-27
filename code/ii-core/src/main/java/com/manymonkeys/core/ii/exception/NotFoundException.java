package com.manymonkeys.core.ii.exception;

import com.manymonkeys.core.ii.Ii;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class NotFoundException extends DAOException {

    private Ii item;

    public Ii getItem() {
        return item;
    }

    public NotFoundException() {
    }

    public NotFoundException(Ii item, Throwable throwable) {
        super(throwable);
        this.item = item;
    }

    public NotFoundException(String s) {
        super(s);
    }

    public NotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public NotFoundException(Throwable throwable) {
        super(throwable);
    }
}
