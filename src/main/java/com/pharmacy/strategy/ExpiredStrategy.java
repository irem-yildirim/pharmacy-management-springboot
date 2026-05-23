package com.pharmacy.strategy;

import org.springframework.stereotype.Component;

@Component
public class ExpiredStrategy implements ExpiryStrategy {

    @Override
    public String evaluate(long daysRemaining) {
        return "EXPIRED";
    }
}
