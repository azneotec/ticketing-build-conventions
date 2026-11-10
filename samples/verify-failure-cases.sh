#!/usr/bin/env bash
# Proves the conventions bite: the sample builds, and each deliberate violation fails the build
# with the expected message. Runs on throw-away copies; the sample itself is never modified.
#
# Prerequisite: ./gradlew build publishToMavenLocal   (from the repo root)
# Usage:        samples/verify-failure-cases.sh
set -euo pipefail

REPO="$(cd "$(dirname "$0")/.." && pwd)"
SAMPLE="$REPO/samples/hexagonal-sample"
PKG="src/main/java/io/github/azneotec/ticketing/sample"
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

fresh_copy() {
  rm -rf "$WORK/sample"
  rsync -a --exclude build --exclude .gradle "$SAMPLE/" "$WORK/sample/"
}

run_build() { # writes the build log to $WORK/log; returns Gradle's exit code
  "$REPO/gradlew" -p "$WORK/sample" build --console=plain >"$WORK/log" 2>&1
}

PASS=0
FAIL=0
expect_failure() { # $1 = case name, $2 = text that must appear in the log
  if run_build; then
    echo "FAIL  $1: build passed, but it should have failed"
    FAIL=$((FAIL + 1))
  elif grep -q -- "$2" "$WORK/log"; then
    echo "OK    $1: build failed with \"$2\""
    PASS=$((PASS + 1))
  else
    echo "FAIL  $1: build failed, but without \"$2\". Last lines:"
    tail -20 "$WORK/log"
    FAIL=$((FAIL + 1))
  fi
}

# 0. Baseline: the untouched sample must build.
fresh_copy
if run_build; then
  echo "OK    baseline: sample builds"
  PASS=$((PASS + 1))
else
  echo "FAIL  baseline: sample does not build. Last lines:"
  tail -30 "$WORK/log"
  exit 1
fi

# 1. Spring dependency + import in the domain module -> domain dependency guard.
fresh_copy
cat >>"$WORK/sample/domain/build.gradle.kts" <<'KTS'

dependencies {
    implementation("org.springframework:spring-context:7.0.10")
}
KTS
sed -i.bak \
  -e 's/^import java.util.Objects;/import java.util.Objects;\nimport org.springframework.stereotype.Component;/' \
  -e 's/^public record Seat/@Component\npublic record Seat/' \
  "$WORK/sample/domain/$PKG/domain/Seat.java"
expect_failure "spring-in-domain" "Domain module must not depend on frameworks"

# 2. Untested production code in the domain -> line coverage below 80%.
fresh_copy
cat >"$WORK/sample/domain/$PKG/domain/Discount.java" <<'JAVA'
package io.github.azneotec.ticketing.sample.domain;

public final class Discount {

  private Discount() {}

  public static long apply(long priceCents, int percent) {
    if (percent < 0 || percent > 100) {
      throw new IllegalArgumentException("percent must be 0..100");
    }
    long off = priceCents * percent / 100;
    long result = priceCents - off;
    if (result < 0) {
      result = 0;
    }
    return result;
  }

  public static long applyTwice(long priceCents, int percent) {
    long once = apply(priceCents, percent);
    long twice = apply(once, percent);
    return twice;
  }
}
JAVA
expect_failure "coverage-below-80" "lines covered ratio is"

# 3. Badly formatted code -> spotlessCheck.
fresh_copy
sed -i.bak 's/^  public static Seat available(String id) {/      public   static Seat available( String id ){/' \
  "$WORK/sample/domain/$PKG/domain/Seat.java"
expect_failure "bad-formatting" "format violations"

# 4. Bonus: Spring code placed in the domain *package* of the Spring module. The Gradle
#    guard cannot see it (the module may use Spring); the ArchUnit rule catches it.
fresh_copy
mkdir -p "$WORK/sample/adapter/$PKG/domain"
cat >"$WORK/sample/adapter/$PKG/domain/SeatPolicy.java" <<'JAVA'
package io.github.azneotec.ticketing.sample.domain;

import org.springframework.stereotype.Component;

@Component
public class SeatPolicy {}
JAVA
expect_failure "archunit-spring-in-domain-package" "ArchitectureTest > isHexagonal() FAILED"

# 5. Generated code (under build/generated) that is untested and badly formatted must NOT
#    fail the build: it is excluded from Spotless and JaCoCo (conventions 0.1.1).
fresh_copy
cat >>"$WORK/sample/domain/build.gradle.kts" <<'KTS'

val generateDemo = tasks.register("generateDemo") {
    val out = layout.buildDirectory.dir("generated/sources/demo/java/main")
    outputs.dir(out)
    doLast {
        val dir = out.get().asFile.resolve("io/github/azneotec/ticketing/sample/domain/gen")
        dir.mkdirs()
        dir.resolve("Generated.java").writeText(
            "package io.github.azneotec.ticketing.sample.domain.gen;\n" +
                "public final class Generated { private Generated() {}\n" +
                "      public static int   twice(int x){ if (x > 0) { return x * 2; } return 0; }\n" +
                "  public static int thrice(int x) { int y = x * 3; return y; } }\n"
        )
    }
}
sourceSets.main { java.srcDir(generateDemo) }
KTS
if run_build; then
  echo "OK    generated-code-ignored: untested, badly formatted generated code builds"
  PASS=$((PASS + 1))
else
  echo "FAIL  generated-code-ignored: build failed. Last lines:"
  tail -20 "$WORK/log"
  FAIL=$((FAIL + 1))
fi

# 6. Versioning: a release tag on a clean tree gives the release version, but uncommitted work on
#    that same tag must give a -SNAPSHOT (conventions 0.1.2), so a dirty build never overwrites a
#    release in mavenLocal. Runs on a throw-away copy of the whole repo with a fake tag v9.9.9.
rsync -a --exclude build --exclude .gradle --exclude .kotlin "$REPO/" "$WORK/repo/"
git -C "$WORK/repo" add -A
git -C "$WORK/repo" commit -q --allow-empty -m "verify: snapshot of the working tree"
git -C "$WORK/repo" tag v9.9.9
project_version() { "$REPO/gradlew" -p "$WORK/repo" -q :arch-rules:properties | sed -n 's/^version: //p'; }
expect_version() { # $1 = case name, $2 = expected version
  local actual
  actual="$(project_version)"
  if [ "$actual" = "$2" ]; then
    echo "OK    $1: version is $2"
    PASS=$((PASS + 1))
  else
    echo "FAIL  $1: expected version $2, got '$actual'"
    FAIL=$((FAIL + 1))
  fi
}
expect_version "version-clean-on-tag" "9.9.9"
echo "uncommitted" >"$WORK/repo/dirty.txt"
expect_version "version-dirty-on-tag" "9.9.10-SNAPSHOT"

echo
echo "$PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ]
