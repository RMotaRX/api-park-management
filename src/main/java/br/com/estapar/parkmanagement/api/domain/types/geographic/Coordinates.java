package br.com.estapar.parkmanagement.api.domain.types.geographic;

import static java.util.Objects.nonNull;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public record Coordinates(
    BigDecimal latitude,
    BigDecimal longitude
) {
  public Coordinates {
    if (!nonNull(latitude) || !nonNull(longitude)) { throw new IllegalArgumentException(); }
    if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
      throw new IllegalArgumentException();
    }
    if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
      throw new IllegalArgumentException();
    }
  }
}
