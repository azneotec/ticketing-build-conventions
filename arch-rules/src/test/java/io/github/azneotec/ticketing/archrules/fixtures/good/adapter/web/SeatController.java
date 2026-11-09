package io.github.azneotec.ticketing.archrules.fixtures.good.adapter.web;

import io.github.azneotec.ticketing.archrules.fixtures.good.application.HoldSeat;
import io.github.azneotec.ticketing.archrules.fixtures.good.domain.Seat;

public class SeatController {
  private final HoldSeat holdSeat;

  public SeatController(HoldSeat holdSeat) {
    this.holdSeat = holdSeat;
  }

  public Seat post(String id) {
    return holdSeat.hold(id);
  }
}
