/*
 * Project Gaai: one app to control the Nexxtender Home charger.
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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * JUnit tests for the [TouPeriod] class.
 * @author Frank HJ Cuypers
 */
class ToPeriodTest {

    @ParameterizedTest
    @MethodSource("usedCombinationsProvider")
    fun verifyResultsFromKnownTestVectors( startTime: Int, endTime: Int, expectedString: String) {
        val touPeriod = TouPeriod(startTime.toShort(), endTime.toShort())
        Assertions.assertEquals(expectedString, touPeriod.toString())
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
                Arguments.of(720, 1234, "12:00 - 20:34"),
                Arguments.of(754, 1425, "12:34 - 23:45"),
                Arguments.of(0, 1439, "00:00 - 23:59"),
            )
        }
    }
}