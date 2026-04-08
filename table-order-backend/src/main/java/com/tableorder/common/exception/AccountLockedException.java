package com.tableorder.common.exception;

public class AccountLockedException extends BusinessException {
    public AccountLockedException(String message) {
        super("ACCOUNT_LOCKED", message);
    }
}
