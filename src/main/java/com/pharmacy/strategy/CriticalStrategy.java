package com.pharmacy.strategy;

import org.springframework.stereotype.Component;

@Component
public class CriticalStrategy implements ExpiryStrategy {

    @Override
    public String evaluate(long daysRemaining) {
        return "CRITICAL";
    }
}
