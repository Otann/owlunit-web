package com.owlunit.service.exception;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class NotFoundException extends Exception {

    private final String id;

    public NotFoundException(String id) {
        super(String.format("Requested object %s was not found", id));
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
