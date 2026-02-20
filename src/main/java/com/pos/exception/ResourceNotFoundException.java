package com.pos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private final ErrorCode errorCode;

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ResourceNotFoundException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage() + ": " + detail);
        this.errorCode = errorCode;
    }

    /** Legacy — used where no typed code is available yet. */
    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = null;
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " not found with id: " + id);
        this.errorCode = null;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
