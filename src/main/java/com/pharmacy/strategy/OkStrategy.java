package com.pharmacy.strategy;

import org.springframework.stereotype.Component;

@Component
public class OkStrategy implements ExpiryStrategy {

    @Override
    public boolean isApplicable(long daysRemaining) {
        return daysRemaining > 30;
    }

    @Override
    public String evaluate(long daysRemaining) {
        return "OK";
    }
}
