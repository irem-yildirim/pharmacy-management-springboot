package com.pharmacy.strategy;

import org.springframework.stereotype.Component;

@Component
public class CriticalStrategy implements ExpiryStrategy {

    @Override
    public boolean isApplicable(long daysRemaining) {
        return daysRemaining > 0 && daysRemaining <= 30;
    }

    @Override
    public String evaluate(long daysRemaining) {
        return "CRITICAL";
    }
}
