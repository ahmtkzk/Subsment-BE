package com.reything.subsmentbe.service;

import com.reything.subsmentbe.domain.enums.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CurrencyConversionService {

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (amount == null || from == null || to == null) return BigDecimal.ZERO;
        if (from == to) return amount.setScale(4, RoundingMode.HALF_UP);
        BigDecimal inTry = amount.multiply(from.getTryRate());
        return inTry.divide(to.getTryRate(), 4, RoundingMode.HALF_UP);
    }
}
