package br.com.estapar.parkmanagement.api.domain.entities;

import static java.util.Objects.nonNull;

import br.com.estapar.parkmanagement.api.domain.types.financial.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "setor")
public class Sector {

  @Id
  @Column(length = 10)
  private String id;

  @NotNull
  @Column(name = "preco_base", precision = 10, scale = 2, nullable = false)
  private Money basePrice;

  @NotNull
  @Min(value = 1, message = "Max capacity must be at least 1")
  @Column(name = "capacidade_maxima", nullable = false)
  private Integer maxCapacity;

  @NotNull
  @Column(name = "hora_abertura", nullable = false)
  private LocalTime openHour;

  @NotNull
  @Column(name = "hora_fechamento", nullable = false)
  private LocalTime closeHour;

  @NotNull
  @Min(value = 1, message = "Duration limit must be at least 1 minute")
  @Max(value = 1440, message = "Duration limit cannot exceed 24 hours")
  @Column(name = "limite_de_permanencia", nullable = false)
  private Integer durationLimitMinutes;

  @Column(name = "ocupacao_atual", nullable = false)
  private Integer currentOccupancy = 0;

  @Column(name = "aberto", nullable = false)
  private Boolean isOpen = true;

  public Sector(String id, Money basePrice, Integer maxCapacity, LocalTime openHour, LocalTime closeHour, Integer durationLimitMinutes) {
    if (!nonNull(id) || id.trim().isEmpty()) { throw new IllegalArgumentException(); }
    if (!nonNull(basePrice)) { throw new IllegalArgumentException(); }
    if (!nonNull(maxCapacity) || maxCapacity < 1) { throw new IllegalArgumentException(); }
    if (!nonNull(openHour) || !nonNull(closeHour)) { throw new IllegalArgumentException(); }
    if (!nonNull(durationLimitMinutes) || durationLimitMinutes < 1) { throw new IllegalArgumentException(); }

    this.id = id.toUpperCase().trim();
    this.basePrice = basePrice;
    this.maxCapacity = maxCapacity;
    this.openHour = openHour;
    this.closeHour = closeHour;
    this.durationLimitMinutes = durationLimitMinutes;
    this.currentOccupancy = 0;
    this.isOpen = true;
  }

  public Double getOccupancyPercentage() {
    if (maxCapacity == 0) return 0.0;
    return currentOccupancy / maxCapacity * 100.0;
  }

  public Boolean isFull() { return currentOccupancy >= maxCapacity; }
  public Boolean canAcceptVehicle() { return isOpen && !isFull(); }

  public void incrementOccupancy() {
    if (isFull()) { throw new IllegalArgumentException(); }
    this.currentOccupancy++;
    if (isFull()) { this.isOpen = false; }
  }

  public void decrementOccupancy() {
    if (currentOccupancy <= 0) { throw new IllegalArgumentException(); }
    this.currentOccupancy--;
    if (!isOpen && !isFull()) { this.isOpen = true; }
  }

  public Money calculateDynamicPrice() {
    Double occupancyPercentage = getOccupancyPercentage();

    if (occupancyPercentage < 25.0) { return basePrice.applyDiscount(BigDecimal.valueOf(10)); }
    else if (occupancyPercentage < 50.0) { return basePrice; }
    else if (occupancyPercentage < 75.0) { return basePrice.applyIncrease(BigDecimal.valueOf(10)); }
    else { return basePrice.applyIncrease(BigDecimal.valueOf(25)); }
  }
}
