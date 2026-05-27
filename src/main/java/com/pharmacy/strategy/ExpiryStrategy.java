package com.pharmacy.strategy;

public interface ExpiryStrategy {

    boolean isApplicable(long daysRemaining);

    String evaluate(long daysRemaining);
}
