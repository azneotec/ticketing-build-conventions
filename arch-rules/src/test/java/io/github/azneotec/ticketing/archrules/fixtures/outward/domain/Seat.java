package io.github.azneotec.ticketing.archrules.fixtures.outward.domain;

import io.github.azneotec.ticketing.archrules.fixtures.outward.adapter.web.SeatDto;

public record Seat(String id) {
  public SeatDto toDto() {
    return new SeatDto(id);
  }
}
