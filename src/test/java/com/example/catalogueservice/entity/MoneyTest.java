package com.example.catalogueservice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldAddAmountsWhenCurrenciesMatch() {
        Money firstPrice = new Money(15_000L, Currency.USD);
        Money secondPrice = new Money(2_500L, Currency.USD);

        Money total = firstPrice.plus(secondPrice);

        assertThat(total.getAmount()).isEqualTo(17_500L);
        assertThat(total.getCurrency()).isEqualTo(Currency.USD);
    }

    @Test
    void shouldRejectAdditionWhenCurrenciesDiffer() {
        Money usdPrice = new Money(15_000L, Currency.USD);
        Money zwlPrice = new Money(2_500L, Currency.ZWL);

        assertThatThrownBy(() -> usdPrice.plus(zwlPrice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Currency mismatch");
    }
}
