package com.example.catalogueservice.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Currency;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Money {
    private long amount;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    public Money plus(Money m) {
        if (this.currency != m.currency) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount + m.amount, this.currency);
    }
}