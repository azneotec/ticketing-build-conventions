package io.github.azneotec.ticketing.archrules.fixtures.appleak.application;

import com.tngtech.archunit.base.DescribedPredicate;
import io.github.azneotec.ticketing.archrules.fixtures.appleak.domain.Seat;

public class HoldSeat {
  public DescribedPredicate<Seat> canHold(String id) {
    return DescribedPredicate.describe("seat " + id, seat -> seat.id().equals(id));
  }
}
