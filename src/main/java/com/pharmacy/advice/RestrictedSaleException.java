package com.pharmacy.advice;

public class RestrictedSaleException extends RuntimeException {

    public RestrictedSaleException(String message) {
        super(message);
    }
}
