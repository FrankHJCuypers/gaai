/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2025, Frank HJ Cuypers
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

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

/**
 * Parses [Badge Record].
 *
 * @author Frank HJ Cuypers
 */
object BadgeParser {
  /**
   * Parses a byte array with the contents of the [Badge Record] into an [Badge].
   * @param badgeData Byte array with the value read from the Generic Data Characteristic for badge ADD and LIST operations.
   * @return A [Badge] holding the parsed result.
   *      Null if *ccdtRecord* is not 16 bytes long or the CRC16 is not correct.
   */
  fun parse(badgeData: ByteArray): Badge? {
    val uuidLength = badgeData[0]
    if (badgeData.size != (uuidLength + 1)) {
      return null
    }
    val uuid = badgeData.copyOfRange(1, badgeData.size)

    return Badge(uuid)
  }

  /**
   * Returns the UUID of a [Badge], preceded with 1 bet with the UUID length.
   * @param badge The badge
   * @return A [DataByteArray] holding the length of teh UUID and teh UUID..
   */
  fun getLengthUUID(badge: Badge): DataByteArray {
    val retval = ByteArray(badge.uuid.size + 1)
    retval[0] = badge.uuid.size.toByte()
    badge.uuid.copyInto(retval, 1)
    return DataByteArray(retval)
  }

}