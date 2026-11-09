package io.github.azneotec.ticketing.archrules.fixtures.sideways.adapter.web;

import io.github.azneotec.ticketing.archrules.fixtures.sideways.adapter.persistence.SeatTable;
import io.github.azneotec.ticketing.archrules.fixtures.sideways.domain.Seat;

public class SeatController {
  public Seat get(String id) {
    return new SeatTable().load(id);
  }
}
