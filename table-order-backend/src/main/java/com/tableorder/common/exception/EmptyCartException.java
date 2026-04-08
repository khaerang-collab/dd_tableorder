package com.tableorder.common.exception;

public class EmptyCartException extends BusinessException {
    public EmptyCartException() {
        super("EMPTY_CART", "장바구니가 비어 있습니다");
    }
}
