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
import be.cuypers_ghys.gaai.util.fromInt32LE
import be.cuypers_ghys.gaai.util.fromUint16LE
import be.cuypers_ghys.gaai.util.fromUint32LE
import be.cuypers_ghys.gaai.util.modBus
import no.nordicsemi.android.kotlin.ble.profile.common.CRC16

// Tag for logging
private const val TAG = "ChargingAdvancedDataParser"

/**
 * Parses [Charging Advanced Data BLE Characteristic]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_ADVANCED_DATA_CHARACTERISTIC].
 *
 * @author Frank HJ Cuypers
 */
object ChargingAdvancedDataParser {
  /**
   * Parses a byte array with the contents of the [Charging Advanced Data BLE Characteristic]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_ADVANCED_DATA_CHARACTERISTIC]
   * into an [ChargingAdvancedData].
   * @param chargingAdvancedData Byte array with the value read from the Charging Car Data BLE
   *      Characteristic.
   * @return A [ChargingAdvancedData] holding the parsed result.
   *      Null if *chargingAdvancedData* is not 18 bytes long or the CRC16 is not correct.
   */
  fun parse(chargingAdvancedData: ByteArray): ChargingAdvancedData? {
    Log.d(TAG, "ENTRY parse(chargingAdvancedData = $chargingAdvancedData)")

    if (chargingAdvancedData.size != 18) {
      Log.d(TAG, "chargingAdvancedData.size is not 18 but ${chargingAdvancedData.size} ")
      return null
    }

    val crc = chargingAdvancedData.fromUint16LE(16)
    val computedCrc = CRC16.modBus(chargingAdvancedData, 0, 16).toUShort()
    if (computedCrc != crc) {
      Log.d(TAG, "chargingAdvancedData crc is ${computedCrc.toHexString()} in stead of ${crc.toHexString()}")
      return null
    }

    val timestamp = chargingAdvancedData.fromUint32LE(0)
    val iAvailable = chargingAdvancedData.fromInt16LE(4)
    val gridPower = chargingAdvancedData.fromInt32LE(6)
    val carPower = chargingAdvancedData.fromInt32LE(10)
    val authorizationStatus = AuthorizationStatus(chargingAdvancedData[14])
    val errorCode = chargingAdvancedData[15]

    val retval = ChargingAdvancedData(
      timestamp,
      iAvailable,
      gridPower,
      carPower,
      authorizationStatus,
      errorCode
    )
    Log.d(TAG, "RETURN parse()=$retval")
    return retval
  }
}