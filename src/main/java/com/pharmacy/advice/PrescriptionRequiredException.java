package com.pharmacy.advice;

public class PrescriptionRequiredException extends RuntimeException {

    public PrescriptionRequiredException(String message) {
        super(message);
    }
}
