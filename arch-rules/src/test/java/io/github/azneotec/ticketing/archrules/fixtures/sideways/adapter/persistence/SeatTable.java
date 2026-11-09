package io.github.azneotec.ticketing.archrules.fixtures.sideways.adapter.persistence;

import io.github.azneotec.ticketing.archrules.fixtures.sideways.domain.Seat;

public class SeatTable {
  public Seat load(String id) {
    return new Seat(id);
  }
}
