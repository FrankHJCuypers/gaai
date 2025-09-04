/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2024, Frank HJ Cuypers
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package be.cuypers_ghys.gaai.util

import kotlinx.datetime.TimeZone
import org.junit.Assume
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * @author Frank HJ Cuypers
 */
class TimestampTest {

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun verifyResultsFromKnownCorrectTestVectors(
    timeStamp: UInt,
    expectedTimeString: String,
  ) {

    println("timezone in test: " + TimeZone.currentSystemDefault().id)
    Assume.assumeTrue("Europe/Brussels" == TimeZone.currentSystemDefault().id)

    val computedTimeString = Timestamp.toString(timeStamp)
    Assertions.assertNotNull(computedTimeString)
    Assertions.assertEquals(expectedTimeString, computedTimeString)
  }

  companion object {
    /**
     * Returns the test vectors.
     *
     * @return Stream of arguments to test
     */
    @JvmStatic
    fun usedCombinationsProvider(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(1, "1970-01-01 01:00:01"),
        Arguments.of(1733852224, "2024-12-10 18:37:04"),
      )
    }
  }
}