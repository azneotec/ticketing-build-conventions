package io.github.azneotec.ticketing.sample;

import io.github.azneotec.ticketing.sample.application.HoldSeatService;
import io.github.azneotec.ticketing.sample.application.SeatRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/** Spring Boot wiring: the only place where framework, application and adapters meet. */
@SpringBootApplication
public class SampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(SampleApplication.class, args);
  }

  @Bean
  HoldSeatService holdSeatService(SeatRepository seats) {
    return new HoldSeatService(seats);
  }
}
