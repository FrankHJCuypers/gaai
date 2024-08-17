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
class ChargingAdvancedDataParserTest {

    @ParameterizedTest
    @MethodSource("usedCombinationsProvider")
    fun parse_VerifyResultsFromKnownTestVectors(chargingAdvancedData : ByteArray,
                                                expectedTimeStamp: Long,
                                                expectedIAvailable: Int,
                                                expectedGridPower : Long,
                                                expectedCarPower: Long,
                                                expectedAuthorizationStatus: AuthorizationStatus,
                                                expectedErrorCode: Int) {
        val computedChargingAdvancedData = ChargingAdvancedDataParser.parse(chargingAdvancedData)
        assertNotNull(computedChargingAdvancedData)
        assertEquals(expectedTimeStamp.toUInt(), computedChargingAdvancedData!!.timestamp)
        assertEquals(expectedIAvailable.toShort(), computedChargingAdvancedData.iAvailable)
        assertEquals(expectedGridPower.toInt(), computedChargingAdvancedData.gridPower)
        assertEquals(expectedCarPower.toInt(), computedChargingAdvancedData.carPower)
        assertEquals(expectedAuthorizationStatus, computedChargingAdvancedData.authorizationStatus)
        assertEquals(expectedErrorCode.toByte(), computedChargingAdvancedData.errorCode)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("SpellCheckingInspection")
    @Test
    fun parse_ChargingAdvancedDataLengthToShort() {
        assertNull(ChargingAdvancedDataParser.parse("1234567890ABCDEF1234567890ABCDEF18".hexToByteArray()))
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("SpellCheckingInspection")
    @Test
    fun parse_ChargingAdvancedDataLengthToLong() {
        assertNull(ChargingAdvancedDataParser.parse("1234567890ABCDEF1234567890ABCDEF18E7FF".hexToByteArray()))
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("SpellCheckingInspection")
    @Test
    fun parse_ChargingAdvancedDataIncorrectCRC16() {
        assertNull(ChargingAdvancedDataParser.parse("1234567890ABCDEF1234567890ABCDEF6969".hexToByteArray()))
    }

    companion object {
        /**
         * Returns the test vectors.
         *
         * @return Stream of arguments to test
         */
        @OptIn(ExperimentalStdlibApi::class)
        @JvmStatic
        @Suppress("SpellCheckingInspection")
        fun usedCombinationsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("1234567890ABCDEF1234567890ABCDEF18E7".hexToByteArray(), 0x78563412, 0xAB90, 0x3412EFCD, 0xAB907856, AuthorizationStatus(0xCD.toByte()), 0xEF),
                Arguments.of("11223344556677889900AABBCCDDEEFF1DED".hexToByteArray(), 0x44332211, 0x6655, 0x00998877, 0xDDCCBBAA, AuthorizationStatus(0xEE.toByte()), 0xFF),
                Arguments.of("660F2D660600FEFFFFFF0000000001009764".hexToByteArray(), 0x662D0F66, 0x0006, 0xFFFFFFFE, 0x00000000, AuthorizationStatus(0x01.toByte()), 0x00),
            )
        }
    }
}