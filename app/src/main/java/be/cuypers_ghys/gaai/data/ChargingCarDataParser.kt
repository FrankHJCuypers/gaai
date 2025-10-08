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
private const val TAG = "ChargingCarDataParser"

/**
 * Parses [Charging Car Data].
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_CAR_DATA_CHARACTERISTIC]. *
 * @author Frank HJ Cuypers
 */
object ChargingCarDataParser {
  /**
   * Parses a byte array with the contents of the [Charging Car Data BLE Characteristic]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_CAR_DATA_CHARACTERISTIC]
   * into an [ChargingCarData].
   * @param chargingCarData Byte array with the value read from the Charging Car Data BLE
   *      Characteristic.
   * @return A [ChargingCarData] holding the parsed result.
   *      Null if *ChargingCarData* is not 18 bytes long or the CRC16 is not correct.
   */
  fun parse(chargingCarData: ByteArray): ChargingCarData? {
    Log.d(TAG, "ENTRY parse(chargingCarData = $chargingCarData)")

    if (chargingCarData.size != 18) {
      Log.d(TAG, "chargingCarData.size is not 18 but ${chargingCarData.size} ")
      return null
    }

    val crc = chargingCarData.fromUint16LE(16)
    val computedCrc = CRC16.modBus(chargingCarData, 0, 16).toUShort()
    if (computedCrc != crc) {
      Log.d(TAG, "chargingCarData crc is ${computedCrc.toHexString()} in stead of ${crc.toHexString()}")
      return null
    }

    val timestamp = chargingCarData.fromUint32LE(0)
    val l1 = chargingCarData.fromInt16LE(4)
    val l2 = chargingCarData.fromInt16LE(6)
    val l3 = chargingCarData.fromInt16LE(8)
    val p1 = chargingCarData.fromInt16LE(10)
    val p2 = chargingCarData.fromInt16LE(12)
    val p3 = chargingCarData.fromInt16LE(14)

    val retval = ChargingCarData(
      timestamp,
      l1,
      l2,
      l3,
      p1,
      p2,
      p3
    )
    Log.d(TAG, "RETURN parse()=$retval")
    return retval
  }
}