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
class ConfigGetDataParserTest {

    @ParameterizedTest
    @MethodSource("usedCombinationsProvider")
    fun parse_VerifyResultsFromKnownTestVectors(configGetData : ByteArray,
                                                expectedMaxGrid: Int,
                                                expectedMaxDevice: Int,
                                                expectedMode: Mode,
                                                expectedSafe: Int,
                                                expectedNetworkType: NetWorkType,
                                                expectedTouWeekStart: Int,
                                                expectedTouWeekEnd: Int,
                                                expectedTouWeekendStart: Int,
                                                expectedTouWeekendEnd: Int,
                                                expectedConfigurationVersion: ConfigVersion) {

        val computedConfigGetData = when( expectedConfigurationVersion )        {
            ConfigVersion.CONFIG_1_0 -> ConfigGetDataParser.parseConfig_1_0(configGetData)
            ConfigVersion.CONFIG_1_1 -> ConfigGetDataParser.parseConfig_1_1(configGetData)
            ConfigVersion.CONFIG_CBOR -> ConfigGetDataParser.parseConfig_1_1(configGetData)
        }
        assertNotNull(computedConfigGetData)
        assertEquals(expectedMaxGrid.toUByte(), computedConfigGetData!!.maxGrid)
        assertEquals(expectedMaxDevice.toUByte(), computedConfigGetData.maxDevice)
        assertEquals(expectedMode, computedConfigGetData.mode)
        assertEquals(expectedSafe.toUByte(), computedConfigGetData.safe)
        assertEquals(expectedNetworkType, computedConfigGetData.networkType)
        assertEquals(expectedTouWeekStart.toShort(), computedConfigGetData.touWeekStart)
        assertEquals(expectedTouWeekEnd.toShort(), computedConfigGetData.touWeekEnd)
        assertEquals(expectedTouWeekendStart.toShort(), computedConfigGetData.touWeekendStart)
        assertEquals(expectedTouWeekendEnd.toShort(), computedConfigGetData.touWeekendEnd)
        assertEquals(expectedConfigurationVersion, computedConfigGetData.configVersion)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun parse_ChargingAdvancedDataLengthToShort() {
        assertNull(ChargingAdvancedDataParser.parse("1234057800BCDEF0123456789ACC".hexToByteArray()))
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun parse_ChargingAdvancedDataLengthToLong() {
        assertNull(ChargingAdvancedDataParser.parse("1234057800BCDEF0123456789ACC2FFF".hexToByteArray()))
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun parse_ChargingAdvancedDataIncorrectCRC16() {
        assertNull(ChargingAdvancedDataParser.parse("1234057800BCDEF0123456789A6969".hexToByteArray()))
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
                Arguments.of("1234057800BCDEF0123456789ACC2F".hexToByteArray(), 0x12, 0x34, Mode.MAX_OPEN, 0x78, NetWorkType.MONO_TRIN, 0xDEBC, 0x12F0, 0x5634, 0x9A78, ConfigVersion.CONFIG_1_1),
                Arguments.of("112200440266778899AABBCCDD5AD6".hexToByteArray(), 0x11, 0x22, Mode.ECO_PRIVATE, 0x44, NetWorkType.TRI, 0x7766, 0x9988, 0xBBAA, 0xDDCC, ConfigVersion.CONFIG_1_1),
                Arguments.of("112201440366778899AABBCCDD53BA".hexToByteArray(), 0x11, 0x22, Mode.MAX_PRIVATE, 0x44, NetWorkType.UNKNOWN, 0x7766, 0x9988, 0xBBAA, 0xDDCC, ConfigVersion.CONFIG_1_1),
                Arguments.of("112204440366778899AABBCCDD4276".hexToByteArray(), 0x11, 0x22, Mode.ECO_OPEN, 0x44, NetWorkType.UNKNOWN, 0x7766, 0x9988, 0xBBAA, 0xDDCC, ConfigVersion.CONFIG_1_1),
                Arguments.of("112264440366778899AABBCCDDBC77".hexToByteArray(), 0x11, 0x22, Mode.UNKNOWN, 0x44, NetWorkType.UNKNOWN, 0x7766, 0x9988, 0xBBAA, 0xDDCC, ConfigVersion.CONFIG_1_1),
                Arguments.of("32200006003C00B10400003D00DD09".hexToByteArray(), 0x32, 0x20, Mode.ECO_PRIVATE, 0x06, NetWorkType.MONO_TRIN, 0x003C, 0x04B1, 0x0000, 0x003D, ConfigVersion.CONFIG_1_1 ),
                Arguments.of("120578BCDEF0123456789A9E1A".hexToByteArray(), 0x12, 0x00, Mode.MAX_OPEN, 0x78, NetWorkType.UNKNOWN, 0xDEBC, 0x12F0, 0x5634, 0x9A78, ConfigVersion.CONFIG_1_0),
                Arguments.of("11004466778899AABBCCDD12EE".hexToByteArray(), 0x11, 0x00, Mode.ECO_PRIVATE, 0x44, NetWorkType.UNKNOWN, 0x7766, 0x9988, 0xBBAA, 0xDDCC, ConfigVersion.CONFIG_1_0),
            )
        }
    }
}