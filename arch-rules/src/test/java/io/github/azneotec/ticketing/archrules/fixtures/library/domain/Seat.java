package io.github.azneotec.ticketing.archrules.fixtures.library.domain;

import com.tngtech.archunit.base.DescribedPredicate;

public record Seat(DescribedPredicate<String> rule) {}
