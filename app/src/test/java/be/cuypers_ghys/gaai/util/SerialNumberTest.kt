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
 * @author Frank HJ Cuypers
 */
class SerialNumberTest {

    @OptIn(ExperimentalStdlibApi::class)
    @ParameterizedTest
    @MethodSource("UsedCombinationsProvider")
    fun verifyResultsFromKnownTestVectors(serialNumberString: String, expectedSerialNumber: Int, expectedSerialNumberString: String) {
        val computedSerialNumber = SerialNumber.calcSerialNumber(serialNumberString)
        Assertions.assertEquals(expectedSerialNumber, computedSerialNumber)
        val computedSerialNumberString = SerialNumber.calcSerialNumberString(serialNumberString)
        Assertions.assertEquals(expectedSerialNumberString, computedSerialNumberString)
    }

    companion object {
        /**
         * Returns the test vectors.
         *
         * @return Stream of arguments to test
         */
        @JvmStatic
        fun UsedCombinationsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("60211-A2-2303-00005-E3", 0x17030005, "17030005"),
                Arguments.of("2303-00005-E3", 0x17030005, "17030005"),
            )
        }
    }
}