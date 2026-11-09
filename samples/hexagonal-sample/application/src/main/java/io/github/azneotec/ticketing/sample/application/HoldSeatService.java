package io.github.azneotec.ticketing.sample.application;

import io.github.azneotec.ticketing.sample.domain.Seat;
import java.util.NoSuchElementException;

/** Use case: hold a seat. */
public final class HoldSeatService {

  private final SeatRepository seats;

  public HoldSeatService(SeatRepository seats) {
    this.seats = seats;
  }

  public Seat hold(String seatId) {
    Seat held =
        seats
            .find(seatId)
            .orElseThrow(() -> new NoSuchElementException("No seat " + seatId))
            .hold();
    seats.save(held);
    return held;
  }
}
