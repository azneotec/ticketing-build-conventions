package io.github.azneotec.ticketing.archrules.fixtures.good.application;

import io.github.azneotec.ticketing.archrules.fixtures.good.domain.Seat;

public class HoldSeat {
  private final SeatRepository seats;

  public HoldSeat(SeatRepository seats) {
    this.seats = seats;
  }

  public Seat hold(String id) {
    return new Seat(seats.find(id).id(), true);
  }
}
