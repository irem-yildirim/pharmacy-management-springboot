package com.pharmacy.strategy;

import org.springframework.stereotype.Component;

@Component
public class ExpiredStrategy implements ExpiryStrategy {

    @Override
    public boolean isApplicable(long daysRemaining) {
        return daysRemaining <= 0;
    }

    @Override
    public String evaluate(long daysRemaining) {
        return "EXPIRED";
    }
}
