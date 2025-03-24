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

import be.cuypers_ghys.gaai.util.fromUint16LE
import be.cuypers_ghys.gaai.util.fromUint32LE

/**
 * Parses [Charging Basic Data]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CHARGING_BASIC_DATA_CHARACTERISTIC].
 *
 * @author Frank HJ Cuypers
 */
object ChargingBasicDataParser {
  /**
   * Parses a byte array with the contents of the [Charging Basic Data]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CHARGING_BASIC_DATA_CHARACTERISTIC]
   * into an [ChargingBasicData].
   * @param chargingBasicData Byte array with the value read from the Charging Basic Data BLE
   * Characteristic.
   * @return A [ChargingBasicData] holding the parsed result.
   *      Null if *chargingBasicData* is not 14 bytes long or RFU bytes are not 0.
   */
  fun parse(chargingBasicData: ByteArray): ChargingBasicData? {
    if (chargingBasicData.size != 14) {
      return null
    }
    val seconds = chargingBasicData.fromUint16LE(0)
    val rawDiscriminator = chargingBasicData[2]
    val discriminator = when (rawDiscriminator) {
      1.toByte() -> Discriminator.STARTED
      2.toByte() -> Discriminator.CHARGING
      3.toByte() -> Discriminator.STOPPED
      else -> Discriminator.UNKNOWN
    }

    val rawStatus = chargingBasicData[3]
    val status = when (rawStatus) {
      'B'.code.toByte() -> Status.PLUGGED
      'C'.code.toByte() -> Status.CHARGING
      'D'.code.toByte() -> Status.CHARGING
      'E'.code.toByte() -> Status.FAULT
      'F'.code.toByte() -> Status.FAULT
      else -> Status.UNKNOWN
    }

    val rfu1 = chargingBasicData.fromUint32LE(4)
    if (rfu1 != 0u) {
      return null
    }

    val energy = chargingBasicData.fromUint32LE(8)

    val rfu2 = chargingBasicData[12]
    if (rfu2 != 0.toByte()) {
      return null
    }

    val phaseCount = chargingBasicData[13].toUByte()

    return ChargingBasicData(
      seconds,
      discriminator,
      status,
      rawStatus,
      energy,
      phaseCount
    )
  }
}