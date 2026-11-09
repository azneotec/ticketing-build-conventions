package io.github.azneotec.ticketing.archrules.fixtures.good.application;

import io.github.azneotec.ticketing.archrules.fixtures.good.domain.Seat;

public interface SeatRepository {
  Seat find(String id);
}
