package io.github.azneotec.ticketing.sample;

import io.github.azneotec.ticketing.archrules.HexagonalArchitecture;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  @Test
  void isHexagonal() {
    HexagonalArchitecture.forBasePackage("io.github.azneotec.ticketing.sample").check();
  }
}
