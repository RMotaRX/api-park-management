package br.com.estapar.parkmanagement.api.domain.types.financial;

import static java.util.Objects.nonNull;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(
    @NotNull
    @DecimalMin(value = "0.0", message = "Amount must be positive")
    BigDecimal amount,

    @NotNull
    Currency currency
) {
  public Money(BigDecimal amount, Currency currency) {
    if (!nonNull(amount) || !nonNull(currency)) { throw new IllegalArgumentException(); }
    if (amount.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException(); }
    this.amount = amount.setScale(2, RoundingMode.HALF_UP);
    this.currency = currency;
  }

  public Money add(Money other) {
    if (!this.currency.equals(other.currency)) { throw new IllegalArgumentException(); }
    return new Money(this.amount.add(other.amount), this.currency);
  }

  public Money subtract(Money other) {
    if (!this.currency.equals(other.currency)) { throw new IllegalArgumentException(); }
    BigDecimal result = this.amount.subtract(other.amount);
    if (result.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException(); }

    return new Money(result, this.currency);
  }

  public Money multiply(BigDecimal factor) {
    if (factor.compareTo(BigDecimal.ZERO) < 0) { throw new IllegalArgumentException(); }
    return new Money(this.amount.multiply(factor), this.currency);
  }

  public Money applyDiscount(BigDecimal discountPercentage) {
    if (discountPercentage.compareTo(BigDecimal.ZERO) < 0 ||
        discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
      throw new IllegalArgumentException();
    }
    BigDecimal discountFactor = BigDecimal.ONE.subtract(
        discountPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
    );
    return multiply(discountFactor);
  }

  public Money applyIncrease(BigDecimal increasePercentage) {
    if (increasePercentage.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException();
    }
    BigDecimal increaseFactor = BigDecimal.ONE.add(
        increasePercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
    );
    return multiply(increaseFactor);
  }
}
