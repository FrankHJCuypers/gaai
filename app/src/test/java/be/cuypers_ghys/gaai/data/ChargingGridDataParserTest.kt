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
class ChargingGridDataParserTest {

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun parse_VerifyResultsFromKnownTestVectors(
    chargingGridData: ByteArray,
    expectedTimeStamp: Long,
    expectedL1: Int,
    expectedL2: Int,
    expectedL3: Int,
    expectedConsumed: Int,
    expectedInterval: Int
  ) {
    val computedChargingGridData = ChargingGridDataParser.parse(chargingGridData)
    assertNotNull(computedChargingGridData)
    assertEquals(expectedTimeStamp.toUInt(), computedChargingGridData!!.timestamp)
    assertEquals(expectedL1.toShort(), computedChargingGridData.l1)
    assertEquals(expectedL2.toShort(), computedChargingGridData.l2)
    assertEquals(expectedL3.toShort(), computedChargingGridData.l3)
    assertEquals(expectedConsumed.toShort(), computedChargingGridData.consumed)
    assertEquals(expectedInterval.toUShort(), computedChargingGridData.interval)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parse_ChargingGridDataLengthToShort() {
    assertNull(ChargingGridDataParser.parse("1234567890ABCDEF1234567890ABEE".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parse_ChargingGridDataLengthToLong() {
    assertNull(ChargingGridDataParser.parse("1234567890ABCDEF1234567890ABEEDBFF".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_ChargingGridDataIncorrectCRC16() {
    assertNull(ChargingGridDataParser.parse("1234567890ABCDEF1234567890AB6969".hexToByteArray()))
  }

  companion object {
    /**
     * Returns the test vectors.
     *
     * @return Stream of arguments to test
     */
    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    @Suppress("unused")
    fun usedCombinationsProvider(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(
          "1234567890ABCDEF1234567890ABEEDB".hexToByteArray(),
          0x78563412,
          0xAB90,
          0xEFCD,
          0x3412,
          0x7856,
          0xAB90
        ),
        Arguments.of(
          "11223344556677889900AABBCCDDC1D3".hexToByteArray(),
          0x44332211,
          0x6655,
          0x8877,
          0x0099,
          0xBBAA,
          0xDDCC
        ),
        Arguments.of(
          "FB0E2D66000000000000370013038814".hexToByteArray(),
          0x662D0EFB,
          0x0000,
          0x0000,
          0x0000,
          0x0037,
          0x0313
        ),
      )
    }
  }
}