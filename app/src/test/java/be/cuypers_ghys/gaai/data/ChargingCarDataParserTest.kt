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
package be.cuypers_ghys.gaai.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * @author Frank HJ Cuypers
 */
class ChargingCarDataParserTest {

    @ParameterizedTest
    @MethodSource("usedCombinationsProvider")
    fun parse_VerifyResultsFromKnownTestVectors(chargingCarData : ByteArray,
                                                expectedTimeStamp: Long,
                                                expectedL1: Int,
                                                expectedL2 : Int,
                                                expectedL3: Int,
                                                expectedP1: Int,
                                                expectedP2: Int,
                                                expectedP3: Int) {
        val computedChargingCarData = ChargingCarDataParser.parse(chargingCarData)
        assertNotNull(computedChargingCarData)
        assertEquals(expectedTimeStamp.toUInt(), computedChargingCarData!!.timestamp)
        assertEquals(expectedL1.toShort(), computedChargingCarData.l1)
        assertEquals(expectedL2.toShort(), computedChargingCarData.l2)
        assertEquals(expectedL3.toShort(), computedChargingCarData.l3)
        assertEquals(expectedP1.toShort(), computedChargingCarData.p1)
        assertEquals(expectedP2.toShort(), computedChargingCarData.p2)
        assertEquals(expectedP3.toShort(), computedChargingCarData.p3)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun parse_ChargingCarDataLengthToShort() {
        assertNull(ChargingCarDataParser.parse("1234567890ABCDEF1234567890ABCDEF18".hexToByteArray()))
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun parse_ChargingCarDataLengthToLong() {
        assertNull(ChargingCarDataParser.parse("1234567890ABCDEF1234567890ABCDEF18E7FFFF".hexToByteArray()))
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun parse_ChargingCarDataIncorrectCRC16() {
        assertNull(ChargingBasicDataParser.parse("1234567890ABCDEF1234567890ABCDEF6969".hexToByteArray()))
    }

    companion object {
        /**
         * Returns the test vectors.
         *
         * @return Stream of arguments to test
         */
        @OptIn(ExperimentalStdlibApi::class)
        @JvmStatic
        fun usedCombinationsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("1234567890ABCDEF1234567890ABCDEF18E7".hexToByteArray(), 0x78563412, 0xAB90, 0xEFCD, 0x3412, 0x7856, 0xAB90, 0xEFCD),
                Arguments.of("11223344556677889900AABBCCDDEEFF1DED".hexToByteArray(), 0x44332211, 0x6655, 0x8877, 0x0099, 0xBBAA, 0xDDCC, 0xFFEE),
                Arguments.of("2E0F2D66000000000000000000000000C903".hexToByteArray(), 0x662D0F2E, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000),
            )
        }
    }
}