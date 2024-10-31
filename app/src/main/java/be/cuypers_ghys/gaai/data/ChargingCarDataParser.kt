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

import be.cuypers_ghys.gaai.util.MODBUS
import be.cuypers_ghys.gaai.util.fromInt16LE
import be.cuypers_ghys.gaai.util.fromUint16LE
import be.cuypers_ghys.gaai.util.fromUint32LE
import no.nordicsemi.android.kotlin.ble.profile.common.CRC16

/**
 * Parses Charging Car Data.
 *
 * @author Frank HJ Cuypers
 */
object ChargingCarDataParser {
  /**
   * Parses a byte array with the contents of the Charging Car Data BLE Characteristic into an
   * [ChargingCarData].
   * @param chargingCarData Byte array with the value read from the Charging Car Data BLE
   * Characteristic.
   * @return A [ChargingCarData] holding the parsed result.
   *      Null if *ChargingCarData* is not 18 bytes long or the CRC16 is not correct.
   */
  fun parse(chargingCarData: ByteArray): ChargingCarData? {
    if (chargingCarData.size != 18) {
      return null
    }

    val crc = chargingCarData.fromUint16LE(16)
    val computedCrc = CRC16.MODBUS(chargingCarData, 0, 16).toUShort()
    if (computedCrc != crc) {
      return null
    }

    val timestamp = chargingCarData.fromUint32LE(0)
    val l1 = chargingCarData.fromInt16LE(4)
    val l2 = chargingCarData.fromInt16LE(6)
    val l3 = chargingCarData.fromInt16LE(8)
    val p1 = chargingCarData.fromInt16LE(10)
    val p2 = chargingCarData.fromInt16LE(12)
    val p3 = chargingCarData.fromInt16LE(14)

    return ChargingCarData(
      timestamp,
      l1,
      l2,
      l3,
      p1,
      p2,
      p3
    )
  }
}