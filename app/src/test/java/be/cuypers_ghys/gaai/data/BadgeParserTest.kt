/*
 * Project Gaai: one app to control the Nexxtender chargers.
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

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * @author Frank HJ Cuypers
 */
class BadgeParserTest {
  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_BadgeDataUUIDLength4() {
    val badge = BadgeParser.parse("0411223344".hexToByteArray())
    assertNotNull(badge)
    assertArrayEquals(badge?.uuid, "11223344".hexToByteArray())
    assertEquals(badge?.chargeType, ChargeType.UNKNOWN)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_BadgeDataUUIDLength7() {
    val badge = BadgeParser.parse("0711223344556677".hexToByteArray())
    assertNotNull(badge)
    assertArrayEquals(badge?.uuid, "11223344556677".hexToByteArray())
    assertEquals(badge?.chargeType, ChargeType.UNKNOWN)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_BadgeDataUUIDLength10() {
    val badge = BadgeParser.parse("0A112233445566778899AA".hexToByteArray())
    assertNotNull(badge)
    assertArrayEquals(badge?.uuid, "112233445566778899AA".hexToByteArray())
    assertEquals(badge?.chargeType, ChargeType.UNKNOWN)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_BadgeDataUUIDLengthIncorrect() {
    assertNull(BadgeParser.parse("08112233445566778899AA".hexToByteArray()))
  }

}