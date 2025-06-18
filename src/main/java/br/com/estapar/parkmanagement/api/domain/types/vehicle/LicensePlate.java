package br.com.estapar.parkmanagement.api.domain.types.vehicle;

import static java.util.Objects.nonNull;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LicensePlate(
    @NotBlank
    @Pattern(
        regexp = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$",
        message = "Licence plate must follow Brazilian format"
    )
    String value
) {
  public LicensePlate(String value) {
    if (!nonNull(value) || value.trim().isEmpty()) { throw new IllegalArgumentException(); }
    this.value = value.toUpperCase().trim();
  }
}
