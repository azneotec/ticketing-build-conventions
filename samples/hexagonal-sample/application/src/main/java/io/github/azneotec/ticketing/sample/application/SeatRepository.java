package io.github.azneotec.ticketing.sample.application;

import io.github.azneotec.ticketing.sample.domain.Seat;
import java.util.Optional;

/** Outbound port: the application asks for seats without knowing where they are stored. */
public interface SeatRepository {

  Optional<Seat> find(String id);

  void save(Seat seat);
}
