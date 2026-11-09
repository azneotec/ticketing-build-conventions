package io.github.azneotec.ticketing.archrules.fixtures.good.adapter.persistence;

import io.github.azneotec.ticketing.archrules.fixtures.good.application.SeatRepository;
import io.github.azneotec.ticketing.archrules.fixtures.good.domain.Seat;

public class InMemorySeatRepository implements SeatRepository {
  @Override
  public Seat find(String id) {
    return new Seat(id, false);
  }
}
