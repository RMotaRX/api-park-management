package br.com.estapar.parkmanagement.api.domain.entities;

import static java.util.Objects.nonNull;

import br.com.estapar.parkmanagement.api.domain.types.geographic.Coordinates;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "vaga")
public class Spot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Embedded
  @AttributeOverrides({
      @AttributeOverride(name = "latitude", column = @Column(name = "latitude", precision = 10, scale = 8)),
      @AttributeOverride(name = "longitude", column = @Column(name = "longitude", precision = 11, scale = 8))
  })
  private Coordinates coordinates;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id_setor", nullable = false)
  private Sector sector;

  @Column(name = "ocupada", nullable = false)
  private Boolean isOccupied = false;

  public Spot(Coordinates coordinates, Sector sector) {
    if (!nonNull(coordinates) || !nonNull(sector)) { throw  new IllegalArgumentException(); }
    this.coordinates = coordinates;
    this.sector = sector;
    this.isOccupied = false;
  }

  public Boolean isAvailable() {return !isOccupied && sector.canAcceptVehicle(); }

  public void occupy() {
    if (isOccupied) { throw  new IllegalArgumentException(); }
    if (!sector.canAcceptVehicle()) { throw new IllegalArgumentException(); }

    this.isOccupied = true;
    sector.incrementOccupancy();
  }

  public void vacate() {
    if (!isOccupied) { throw new IllegalArgumentException(); }
    this.isOccupied = false;
    sector.decrementOccupancy();
  }

  public Double distanceTo(Spot otherSpot) {
    if (!nonNull(otherSpot) || !nonNull(otherSpot.coordinates)) {
      throw new IllegalArgumentException();
    }

    return calculateDistance(
        this.coordinates.latitude().doubleValue(),
        this.coordinates.longitude().doubleValue(),
        otherSpot.coordinates.latitude().doubleValue(),
        otherSpot.coordinates.longitude().doubleValue()
    );
  }

  public Boolean isInSameSector(Spot otherSpot) {
    if (!nonNull(otherSpot)) return false;
    return this.sector.equals(otherSpot.sector);
  }

  public Boolean isNearLocation(Coordinates location, Double radiusInMeters) {
    if (!nonNull(location)) return false;

    Double distance = calculateDistance(
        this.coordinates.latitude().doubleValue(),
        this.coordinates.longitude().doubleValue(),
        location.latitude().doubleValue(),
        location.longitude().doubleValue()
    );

    return distance <= radiusInMeters;
  }

  private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
    final Integer EARTH_RADIUS = 6371000;

    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double haversineFormula = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                              Math.cos(Math.toRadians(lat1) * Math.cos(Math.toRadians(lat2)) +
                              Math.sin(dLon / 2) * Math.sin(dLon / 2));

    Double angularDistance = 2 * Math.atan2(Math.sqrt(haversineFormula), Math.sqrt(1 - haversineFormula));

    return EARTH_RADIUS * angularDistance;
  }
}
