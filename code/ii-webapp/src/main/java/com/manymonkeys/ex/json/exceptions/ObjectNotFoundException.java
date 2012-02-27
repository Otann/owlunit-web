package com.manymonkeys.ex.json.exceptions;

import com.manymonkeys.service.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ObjectNotFoundException extends RuntimeException {

    public ObjectNotFoundException(NotFoundException exception) {
        super(exception);
    }
}
