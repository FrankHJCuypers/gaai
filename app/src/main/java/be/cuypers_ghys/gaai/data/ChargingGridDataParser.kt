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

import android.util.Log
import be.cuypers_ghys.gaai.util.fromInt16LE
import be.cuypers_ghys.gaai.util.fromUint16LE
import be.cuypers_ghys.gaai.util.fromUint32LE
import be.cuypers_ghys.gaai.util.modBus
import no.nordicsemi.android.kotlin.ble.profile.common.CRC16

// Tag for logging
private const val TAG = "ChargingGridDataParser"

/**
 * Parses [Charging Grid Data]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_GRID_DATA_CHARACTERISTIC].
 *
 * @author Frank HJ Cuypers
 */
object ChargingGridDataParser {
  /**
   * Parses a byte array with the contents of the [Charging Grid Data BLE Characteristic]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_GRID_DATA_CHARACTERISTIC].
   * into an [ChargingGridData].
   * @param chargingGridData Byte array with the value read from the Charging Grid Data BLE
   * Characteristic.
   * @return A [ChargingGridData] holding the parsed result.
   *      Null if *chargingGridData* is not 16 bytes long or the CRC16 is not correct.
   */
  fun parse(chargingGridData: ByteArray): ChargingGridData? {
    Log.d(TAG, "ENTRY parse(chargingGridData = $chargingGridData)")

    if (chargingGridData.size != 16) {
      Log.d(TAG, "chargingGridData.size is not 16 but ${chargingGridData.size} ")
      return null
    }

    val crc = chargingGridData.fromUint16LE(14)
    val computedCrc = CRC16.modBus(chargingGridData, 0, 14).toUShort()
    if (computedCrc != crc) {
      Log.d(TAG, "chargingGridData crc is ${computedCrc.toHexString()} in stead of ${crc.toHexString()}")
      return null
    }

    val timestamp = chargingGridData.fromUint32LE(0)
    val l1 = chargingGridData.fromInt16LE(4)
    val l2 = chargingGridData.fromInt16LE(6)
    val l3 = chargingGridData.fromInt16LE(8)
    val consumed = chargingGridData.fromInt16LE(10)
    val interval = chargingGridData.fromUint16LE(12)

    val retval = ChargingGridData(
      timestamp,
      l1,
      l2,
      l3,
      consumed,
      interval
    )
    Log.d(TAG, "RETURN parse()=$retval")
    return retval
  }
}