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

import org.junit.jupiter.api.Assertions.assertArrayEquals
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
class TimeDataParserComposerTest {

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun parse_VerifyResultsFromKnownTestVectors(
    timeData: ByteArray,
    expectedTime: Long,
  ) {
    val computedTimeData = TimeDataParserComposer.parse(timeData)
    assertNotNull(computedTimeData)
    assertEquals(expectedTime.toUInt(), computedTimeData!!.time)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_TimeDataLengthToShort() {
    assertNull(TimeDataParserComposer.parse("123456".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_TimeDataLengthToLong() {
    assertNull(TimeDataParserComposer.parse("1234567890".hexToByteArray()))
  }

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun compose_VerifyResultsFromKnownTestVectors(
    timeData: ByteArray,
    expectedTime: Long,
  ) {
    val computedTime = TimeDataParserComposer.compose(TimeData(expectedTime.toUInt()))

    assertNotNull(computedTime)
    assertArrayEquals(timeData, computedTime)
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
        Arguments.of(
          "12345678".hexToByteArray(),
          0x78563412
        ),
        Arguments.of(
          "11223344".hexToByteArray(),
          0x44332211
        ),
        Arguments.of(
          "2E0F2D66".hexToByteArray(),
          0x662D0F2E
        ),
      )
    }
  }
}