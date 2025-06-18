package br.com.estapar.parkmanagement.api.domain.entities.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ParkingSessionStatus {

  ENTRADA("ENTERED"),
  ESTACIONADO("PARKED"),
  SAIDA("EXITED");

  private static final String UNKNOWN_PARKING_SESSION_MESSAGE = "Unknown parking session: %s";

  private final String status;

  @JsonCreator
  public static ParkingSessionStatus fromStatus(String status) {
    return Arrays.stream(ParkingSessionStatus.values())
        .filter(pss -> pss.getStatus().equalsIgnoreCase(status))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(String.format(UNKNOWN_PARKING_SESSION_MESSAGE, status))
    );
  }
}
