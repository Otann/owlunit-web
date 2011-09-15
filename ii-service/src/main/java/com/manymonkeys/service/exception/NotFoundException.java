package com.manymonkeys.service.exception;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class NotFoundException extends Exception {

    public NotFoundException(String s) {
        super(String.format("Requested object %s was not found", s));
    }
}
