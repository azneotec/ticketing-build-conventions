package io.github.azneotec.ticketing.sample.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.azneotec.ticketing.sample.domain.Seat;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class HoldSeatServiceTest {

  private final Map<String, Seat> store = new HashMap<>();
  private final HoldSeatService service =
      new HoldSeatService(
          new SeatRepository() {
            @Override
            public Optional<Seat> find(String id) {
              return Optional.ofNullable(store.get(id));
            }

            @Override
            public void save(Seat seat) {
              store.put(seat.id(), seat);
            }
          });

  @Test
  void holdsAndSavesTheSeat() {
    store.put("A1", Seat.available("A1"));
    assertThat(service.hold("A1").held()).isTrue();
    assertThat(store.get("A1").held()).isTrue();
  }

  @Test
  void failsForUnknownSeat() {
    assertThatThrownBy(() -> service.hold("Z9")).isInstanceOf(NoSuchElementException.class);
  }
}
