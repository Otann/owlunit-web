package com.manymonkeys.ex.json.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class InformationItemNotFoundException extends RuntimeException {

    private Long resourceId;

    public InformationItemNotFoundException(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getResourceId() {
        return resourceId;
    }

}
