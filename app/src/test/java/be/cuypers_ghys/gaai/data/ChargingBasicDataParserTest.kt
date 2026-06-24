/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2024-2025, Frank HJ Cuypers
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
 * JUnit tests for [ChargingBasicDataParser].
 * @author Frank HJ Cuypers
 */
class ChargingBasicDataParserTest {

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun parse_VerifyResultsFromKnownTestVectors(
    chargingBasicData: ByteArray,
    expectedSeconds: Int,
    expectedDiscriminator: Discriminator,
    expectedStatus: Status,
    expectedRfu1CarPower: UInt,
    expectedRawStatus: Int,
    expectedEnergy: Long,
    expectedPhaseCount: Int
  ) {
    val computedChargingBasicData = ChargingBasicDataParser.parse(chargingBasicData)
    assertNotNull(computedChargingBasicData)
    assertEquals(expectedSeconds.toUShort(), computedChargingBasicData!!.seconds)
    assertEquals(expectedDiscriminator, computedChargingBasicData.discriminator)
    assertEquals(expectedStatus, computedChargingBasicData.status)
    assertEquals(expectedRfu1CarPower, computedChargingBasicData.rfu1CarPower)
    assertEquals(expectedRawStatus.toByte(), computedChargingBasicData.rawStatus)
    assertEquals(expectedEnergy.toUInt(), computedChargingBasicData.energy)
    assertEquals(expectedPhaseCount.toUByte(), computedChargingBasicData.phaseCount)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_ChargingBasicDataLengthToShort() {
    assertNull(ChargingBasicDataParser.parse("03000242000000007856341200".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_ChargingBasicDataLengthToLong() {
    assertNull(ChargingBasicDataParser.parse("030002420000000078563412000100".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_ChargingBasicDataRFU1Not0() {
    assertNotNull(ChargingBasicDataParser.parse("0300024200000001785634120001".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_ChargingBasicDataRFU2Not0() {
    assertNotNull(ChargingBasicDataParser.parse("0300024200000000785634120101".hexToByteArray()))
  }

  companion object {
    /**
     * Returns the test vectors.
     *
     * @return Stream of arguments to test
     */
    @Suppress("SpellCheckingInspection", "RedundantSuppression")
    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    fun usedCombinationsProvider(): Stream<Arguments> {
      @Suppress("SpellCheckingInspection")
      return Stream.of(
        Arguments.of(
          "0300024200000000785634120001".hexToByteArray(),
          0x03,
          Discriminator.CHARGING,
          Status.PLUGGED,
          0,
          'B',
          0x12345678,
          1
        ),
        Arguments.of(
          "FFFF014300000000A8A6A4A20002".hexToByteArray(),
          0xFFFF,
          Discriminator.STARTED,
          Status.CHARGING,
          0,
          'C',
          0xA2A4A6A8,
          2
        ),
        Arguments.of(
          "1234034400000000000000000003".hexToByteArray(),
          0x3412,
          Discriminator.STOPPED,
          Status.CHARGING,
          0,
          'D',
          0x00000000,
          3
        ),
        Arguments.of(
          "1234044500000000111111110004".hexToByteArray(),
          0x3412,
          Discriminator.UNKNOWN,
          Status.FAULT,
          0,
          'E',
          0x11111111,
          4
        ),
        Arguments.of(
          "1234044600000000111111110004".hexToByteArray(),
          0x3412,
          Discriminator.UNKNOWN,
          Status.FAULT,
          0,
          'F',
          0x11111111,
          4
        ),
        Arguments.of(
          "1234044700000000111111110004".hexToByteArray(),
          0x3412,
          Discriminator.UNKNOWN,
          Status.UNKNOWN,
          0,
          'G',
          0x11111111,
          4
        ),
        Arguments.of(
          "1234044712345678111111110004".hexToByteArray(),
          0x3412,
          Discriminator.UNKNOWN,
          Status.UNKNOWN,
          0x78563412,
          'G',
          0x11111111,
          4
        ),
      )
    }
  }
}