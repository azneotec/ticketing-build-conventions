package io.github.azneotec.ticketing.sample.adapter.persistence;

import io.github.azneotec.ticketing.sample.application.SeatRepository;
import io.github.azneotec.ticketing.sample.domain.Seat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Outbound adapter: implements the port with an in-memory map (a real service uses Postgres). */
@Component
class InMemorySeatRepository implements SeatRepository {

  private final Map<String, Seat> seats = new ConcurrentHashMap<>();

  InMemorySeatRepository() {
    save(Seat.available("A1"));
    save(Seat.available("A2"));
  }

  @Override
  public Optional<Seat> find(String id) {
    return Optional.ofNullable(seats.get(id));
  }

  @Override
  public void save(Seat seat) {
    seats.put(seat.id(), seat);
  }
}
