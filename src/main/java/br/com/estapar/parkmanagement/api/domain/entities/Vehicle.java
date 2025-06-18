package br.com.estapar.parkmanagement.api.domain.entities;

import static java.util.Objects.nonNull;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "veiculos")
public class Vehicle {

  @Id
  @Column(name = "placa_veiculo", length = 8)
  private String licensePlate;

  public Vehicle(String licencePlate) {
    if (!nonNull(licencePlate) || licencePlate.trim().isEmpty()) { throw new IllegalArgumentException(); }

    String cleanPlate = licencePlate.toUpperCase().trim();
    if (!isValidLicensePlate(cleanPlate)) { throw new IllegalArgumentException(); }

    this.licensePlate = licencePlate;
  }

  public static boolean isValidLicensePlate(String plate) {
    if (!nonNull(plate)) return false;

    Pattern pattern = Pattern.compile("^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$");
    return pattern.matcher(plate).matches();
  }

  public Boolean isMercosulFormat() {
    Pattern mercosul = Pattern.compile("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");
    return mercosul.matcher(licensePlate).matches();
  }

  public Boolean isOldFormat() {
    Pattern old = Pattern.compile("^[A-Z]{3}[0-9]{4}$");
    return old.matcher(licensePlate).matches();
  }

  public String getFormattedLicensePlate() {
    if (isMercosulFormat()) {
      return licensePlate.substring(0,3) + "-" + licensePlate.substring(3);
    } else if (isOldFormat()) {
      return licensePlate.substring(0,3) + "-" + licensePlate.substring(3);
    }

    return licensePlate;
  }

  public Boolean canPark() { return isValidLicensePlate(licensePlate); }
}
