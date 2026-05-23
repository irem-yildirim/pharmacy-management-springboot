package com.pharmacy.strategy;

public interface ExpiryStrategy {

    String evaluate(long daysRemaining);
}
