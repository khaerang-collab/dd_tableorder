package com.tableorder.common.exception;

public class InvalidStatusTransitionException extends BusinessException {
    public InvalidStatusTransitionException(String message) {
        super("INVALID_STATUS_TRANSITION", message);
    }
}
