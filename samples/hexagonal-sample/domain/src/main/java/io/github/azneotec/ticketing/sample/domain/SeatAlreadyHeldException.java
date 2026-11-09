package io.github.azneotec.ticketing.sample.domain;

public final class SeatAlreadyHeldException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public SeatAlreadyHeldException(String seatId) {
    super("Seat " + seatId + " is already held");
  }
}
