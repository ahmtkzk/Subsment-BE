package com.reything.subsmentbe.domain.enums;

import java.math.BigDecimal;

public enum Currency {
    TRY("₺", new BigDecimal("1")),
    USD("$", new BigDecimal("38.5")),
    EUR("€", new BigDecimal("42.0")),
    GBP("£", new BigDecimal("49.0"));

    private final String symbol;
    private final BigDecimal tryRate;

    Currency(String symbol, BigDecimal tryRate) {
        this.symbol = symbol;
        this.tryRate = tryRate;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getTryRate() {
        return tryRate;
    }
}
