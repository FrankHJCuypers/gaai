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
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * @author Frank HJ Cuypers
 */
class ProductNumberParserTest {

    @ParameterizedTest
    @MethodSource("usedCombinationsProvider")
    fun verifyResultsFromKnownCorrectTestVectors(productNumberString: String, expectedModelNumber: Int, expectedHardwareRevision: Int) {
        val computedProductNumber = ProductNumberParser.parse(productNumberString)
        Assertions.assertNotNull(productNumberString)
        Assertions.assertEquals(expectedModelNumber.toUInt(), computedProductNumber!!.modelNumber)
        Assertions.assertEquals(expectedHardwareRevision.toUByte(), computedProductNumber.hardwareRevision)
    }

    @Test
    fun parse_colonInsteadOfDash() {
        Assertions.assertNull(SerialNumberParser.parse("60211:A2"))
    }

    @Test
    fun parse_pipeInsteadOfDash() {
        Assertions.assertNull(SerialNumberParser.parse("60211|A2"))
    }

    @Test
    fun parse_AAAAAToShort() {
        Assertions.assertNull(SerialNumberParser.parse("6211-A2"))
    }

    @Test
    fun parse_YYMMToLong() {
        Assertions.assertNull(SerialNumberParser.parse("606211-A2"))
    }

    @Test
    fun parse_AAAAAHex() {
        Assertions.assertNull(SerialNumberParser.parse("6021A-A2"))
    }

    @Test
    fun parse_AAAAANotHex() {
        Assertions.assertNull(SerialNumberParser.parse("6021Z-A2"))
    }

    @Test
    fun parse_RRToShort() {
        Assertions.assertNull(SerialNumberParser.parse("60211-A"))
    }

    @Test
    fun parse_RRToLong() {
        Assertions.assertNull(SerialNumberParser.parse("60211-A20"))
    }

    @Test
    fun parse_RRNotHex() {
        Assertions.assertNull(SerialNumberParser.parse("60211-AK"))
    }

    companion object {
        /**
         * Returns the test vectors.
         *
         * @return Stream of arguments to test
         */
        @JvmStatic
        @Suppress("unused")
        fun usedCombinationsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("60211-A2", 60211, 0xA2),
                Arguments.of("60211A2", 60211, 0xA2),
            )
        }
    }
}