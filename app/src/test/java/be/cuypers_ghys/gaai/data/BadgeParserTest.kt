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
class BadgeParserTest {
  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun parse_BadgeDataVerifyResultsFromKnownTestVectors(
    lengthUuidArray: ByteArray,
    uuidLength: Int,
    uuidArray: ByteArray,
  ) {
    val badge = BadgeParser.parse(lengthUuidArray)
    assertNotNull(badge)
    assertArrayEquals(badge?.uuid, uuidArray)
    assertEquals(badge?.chargeType, ChargeType.UNKNOWN)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_BadgeDataUUIDLengthIncorrect() {
    assertNull(BadgeParser.parse("08112233445566778899AA".hexToByteArray()))
  }

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun parse_GetLengthUUIDVerifyResultsFromKnownTestVectors(
    lengthUuidArray: ByteArray,
    uuidLength: Int,
    uuidArray: ByteArray,
  ) {
    val lengthUUID = BadgeParser.getLengthUUID(Badge(uuidArray))
    assertEquals(uuidLength, lengthUUID.value[0].toInt())
    assertArrayEquals(uuidArray, lengthUUID.copyOfRange(1, lengthUUID.value[0].toInt() + 1).value)
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
          "00".hexToByteArray(),
          0,
          "".hexToByteArray(),
        ),
        Arguments.of(
          "0411223344".hexToByteArray(),
          4,
          "11223344".hexToByteArray(),
        ),
        Arguments.of(
          "0711223344556677".hexToByteArray(),
          7,
          "11223344556677".hexToByteArray(),
        ),
        Arguments.of(
          "0A112233445566778899AA".hexToByteArray(),
          10,
          "112233445566778899AA".hexToByteArray(),
        ),
      )
    }
  }
}