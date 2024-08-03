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
class SerialNumberParserTest {

    @ParameterizedTest
    @MethodSource("usedCombinationsProvider")
    fun verifyResultsFromKnownCorrectTestVectors(serialNumberString: String, expectedYear: Int, expectedMonth: Int, expectedNumber: Int, expectedUnknown: Int, expectedSerialNumber: Int, expectedSerialNumberString: String) {
        val computedSerialNumber = SerialNumberParser.parse(serialNumberString)
        Assertions.assertNotNull(computedSerialNumber)
        Assertions.assertEquals(expectedYear.toUByte(), computedSerialNumber!!.year)
        Assertions.assertEquals(expectedMonth.toUByte(), computedSerialNumber.month)
        Assertions.assertEquals(expectedNumber.toUInt(), computedSerialNumber.number)
        Assertions.assertEquals(expectedUnknown.toUByte(), computedSerialNumber.unknown)

        val calcHexSerialNumber = SerialNumberParser.calcHexSerialNumber(computedSerialNumber)
        Assertions.assertEquals(expectedSerialNumber.toLong(), calcHexSerialNumber.toLong())

        val calcHexSerialNumberString = SerialNumberParser.calcHexSerialNumberString(computedSerialNumber)
        Assertions.assertEquals(expectedSerialNumberString, calcHexSerialNumberString)
    }

    @Test
    fun parse_colonInsteadOfDash() {
        Assertions.assertNull(SerialNumberParser.parse("2303:00005:E3"))
    }

    @Test
    fun parse_pipeInsteadOfDash() {
        Assertions.assertNull(SerialNumberParser.parse("2303|00005|E3"))
    }

    @Test
    fun parse_YYMMToShort() {
        Assertions.assertNull(SerialNumberParser.parse("230-00005-E3"))
    }

    @Test
    fun parse_YYMMToLong() {
        Assertions.assertNull(SerialNumberParser.parse("23030-00005-E3"))
    }

    @Test
    fun parse_YYMMHex() {
        Assertions.assertNull(SerialNumberParser.parse("230A-00005-E3"))
    }

    @Test
    fun parse_YYMMNotHex() {
        Assertions.assertNull(SerialNumberParser.parse("230Z-00005-E3"))
    }

    @Test
    fun parse_SSSSSToShort() {
        Assertions.assertNull(SerialNumberParser.parse("2303-0005-E3"))
    }

    @Test
    fun parse_SSSSSToLong() {
        Assertions.assertNull(SerialNumberParser.parse("2303-000905-E3"))
    }

    @Test
    fun parse_SSSSSHex() {
        Assertions.assertNull(SerialNumberParser.parse("2303-00A05-E3"))
    }

    @Test
    fun parse_SSSSSNotHex() {
        Assertions.assertNull(SerialNumberParser.parse("2303-K0005-E3"))
    }

    @Test
    fun parse_UUToShort() {
        Assertions.assertNull(SerialNumberParser.parse("2303-00005-E"))
    }

    @Test
    fun parse_UUToLong() {
        Assertions.assertNull(SerialNumberParser.parse("2303-00005-E34"))
    }

    @Test
    fun parse_UUNotHex() {
        Assertions.assertNull(SerialNumberParser.parse("2303-00005-Z3"))
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
                Arguments.of("2303-00005-E3", 0x17, 0x03, 5, 0xE3, 0x17030005, "17030005"),
                Arguments.of("230300005E3", 0x17, 0x03, 5, 0xE3, 0x17030005, "17030005"),
                Arguments.of("2303-00005E3", 0x17, 0x03,5, 0xE3, 0x17030005, "17030005"),
                Arguments.of("230300005-E3", 0x17, 0x03, 5, 0xE3, 0x17030005, "17030005"),
            )
        }
    }
}