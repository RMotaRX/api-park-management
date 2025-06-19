package br.com.estapar.parkmanagement.api.domain.entities;

import static java.util.Objects.nonNull;

import br.com.estapar.parkmanagement.api.domain.entities.enums.ParkingSessionStatus;
import br.com.estapar.parkmanagement.api.domain.types.financial.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor
@Table(name = "sessoes_de_estacionamento")
public class ParkingSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "placa_veiculo", nullable = false)
  private Vehicle vehicle;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_vaga")
  private Spot spot;

  @NotNull
  @Column(name = "hora_entrada", nullable = false)
  private LocalDateTime entryTime;

  @Column(name = "tempo_estacionado")
  private LocalDateTime parkedTime;

  @Column(name = "hora_saida")
  private LocalDateTime exitTime;

  @NotNull
  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "amount", column = @Column(name = "preco_entrada", precision = 10, scale = 2)),
      @AttributeOverride(name = "currency", column = @Column(name = "moeda_entrada", length = 3))
  })
  private Money entryPrice;

  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "amount", column = @Column(name = "preco_final", precision = 10, scale = 2)),
      @AttributeOverride(name = "currency", column = @Column(name = "moeda_final", length = 3))
  })
  private Money finalPrice;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ParkingSessionStatus status;

  public ParkingSession(Vehicle vehicle, LocalDateTime entryTime, Money entryPrice) {
    if (!nonNull(vehicle)) { throw new IllegalArgumentException(); }
    if (!nonNull(entryTime)) { throw new IllegalArgumentException(); }
    if (!nonNull(entryPrice)) { throw new IllegalArgumentException(); }

    this.vehicle = vehicle;
    this.entryTime = entryTime;
    this.entryPrice = entryPrice;
    this.status = ParkingSessionStatus.ENTRADA;
  }

  public Boolean isActive() {
    return status == ParkingSessionStatus.ENTRADA || status == ParkingSessionStatus.ESTACIONADO;
  }

  public void parkAtSpot(Spot spot, LocalDateTime parkedTime) {
    if (!nonNull(spot)) { throw new IllegalArgumentException(); }
    if (!nonNull(parkedTime)) {throw new IllegalArgumentException(); }
    if (status != ParkingSessionStatus.ENTRADA) { throw new IllegalArgumentException(); }
    if (parkedTime.isBefore(entryTime)) { throw new IllegalArgumentException(); }

    this.spot = spot;
    this.parkedTime = parkedTime;
    this.status = ParkingSessionStatus.ESTACIONADO;
    spot.occupy();
  }

  public void exit(LocalDateTime exitTime) {
    if (!nonNull(exitTime)) { throw new IllegalArgumentException(); }
    if (!isActive()) { throw new IllegalArgumentException(); }
    if (exitTime.isBefore(entryTime)) { throw new IllegalArgumentException(); }

    this.exitTime = exitTime;
    this.status = ParkingSessionStatus.SAIDA;

    if (nonNull(spot)) { spot.vacate(); }

    calculateFinalPrice();
  }

  public Long getParkedDurationMinutes() {
    if (!nonNull(parkedTime)) return 0L;

    LocalDateTime endTime = nonNull(exitTime) ? exitTime: LocalDateTime.now();
    return ChronoUnit.MINUTES.between(parkedTime, endTime);
  }

  public Long getTotalDurationMinutes() {
    LocalDateTime endTime = nonNull(exitTime) ? exitTime : LocalDateTime.now();
    return ChronoUnit.MINUTES.between(parkedTime, endTime);
  }

  private Money calculateCurrentPrice() {
    if (status == ParkingSessionStatus.SAIDA && nonNull(finalPrice)) { return finalPrice; }

    Long durationMinutes = getTotalDurationMinutes();
    if (durationMinutes <= 0) { return  entryPrice; }

    long hours = (durationMinutes + 59) / 60;

    return entryPrice.multiply(BigDecimal.valueOf(hours));
  }

  private void calculateFinalPrice() { this.finalPrice = calculateCurrentPrice(); }
}
