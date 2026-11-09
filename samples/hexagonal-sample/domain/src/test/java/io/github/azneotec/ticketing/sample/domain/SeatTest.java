package io.github.azneotec.ticketing.sample.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SeatTest {

  @Test
  void holdsAnAvailableSeat() {
    assertThat(Seat.available("A1").hold().held()).isTrue();
  }

  @Test
  void refusesToHoldTwice() {
    Seat held = Seat.available("A1").hold();
    assertThatThrownBy(held::hold)
        .isInstanceOf(SeatAlreadyHeldException.class)
        .hasMessage("Seat A1 is already held");
  }

  @Test
  void rejectsBlankId() {
    assertThatThrownBy(() -> Seat.available(" ")).isInstanceOf(IllegalArgumentException.class);
  }
}
