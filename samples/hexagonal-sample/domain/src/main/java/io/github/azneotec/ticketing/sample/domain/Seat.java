package io.github.azneotec.ticketing.sample.domain;

import java.util.Objects;

/** A seat that can be held once. */
public record Seat(String id, boolean held) {

  public Seat {
    Objects.requireNonNull(id, "id");
    if (id.isBlank()) {
      throw new IllegalArgumentException("Seat id must not be blank");
    }
  }

  public static Seat available(String id) {
    return new Seat(id, false);
  }

  public Seat hold() {
    if (held) {
      throw new SeatAlreadyHeldException(id);
    }
    return new Seat(id, true);
  }
}
