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
package be.cuypers_ghys.gaai.util

import no.nordicsemi.android.kotlin.ble.profile.common.CRC16
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * JUnit tests for [CRC16.modBus].
 * @author Frank HJ Cuypers
 */
class TestCRC16MODBUS {
  @OptIn(ExperimentalStdlibApi::class)
  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun verifyResultsFromKnownTestVectors(sData: String, expectedCrc: Int) {
    val data = sData.hexToByteArray()

    val computedCrc = CRC16.modBus(data, 0, data.size)
    Assertions.assertEquals(expectedCrc, computedCrc)
  }

  companion object {
    /**
     * Returns the test vectors.
     *
     * These are computed with https://crccalc.com/ using CRC-16/MODBUS in HEX mode (polynomial 0x8005)
     *
     *  - Check: 0x4B37
     *  - Poly: 0x8005
     *  - Init: 0xFFFF
     *  - RefIn: true
     *  - RefOut: true
     *  - XorOut: 0x00
     *
     * See [Online CRC-8 CRC-16 CRC-32 Calculator](https://crccalc.com/)
     * @return Stream of arguments to test
     */
    @Suppress("SpellCheckingInspection")
    @JvmStatic
    fun usedCombinationsProvider(): Stream<Arguments> {
      return Stream.of(
        Arguments.of("010203", 0x6161),
        Arguments.of("481A6A0A08030303", 0xC6DD),
        Arguments.of(
          "be81c6c867ae823e78b43d7220ecf40082dc6872d8775f95124efd350b9548d616d9dee080fed66296115cf310e95a933001ff61ed29b8a25c61746b93308644828459c59f09d1a02a8dbe2fc6c59113",
          0x7898
        ),
        Arguments.of("263b130bff13b97472d0143635984d0bb76c7bb6f45dfcc381", 0x9DD5),
        Arguments.of("cefba20853", 0xEB9A),
        Arguments.of("fb0e2d6600000000000037001303", 0x1488)
      )
    }
  }
}