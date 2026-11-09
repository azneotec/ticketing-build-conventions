package io.github.azneotec.ticketing.sample;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.azneotec.ticketing.sample.application.HoldSeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SampleApplicationTest {

  @Autowired HoldSeatService holdSeatService;

  @Test
  void holdsASeatThroughTheWiredAdapter() {
    assertThat(holdSeatService.hold("A1").held()).isTrue();
  }

  @Test
  void mainStartsTheApplication() {
    SampleApplication.main(new String[0]);
  }
}
