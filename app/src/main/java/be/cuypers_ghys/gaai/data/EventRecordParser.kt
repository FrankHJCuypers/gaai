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

import android.util.Log
import be.cuypers_ghys.gaai.util.fromUint16LE
import be.cuypers_ghys.gaai.util.fromUint32LE
import be.cuypers_ghys.gaai.util.modBus
import no.nordicsemi.android.kotlin.ble.profile.common.CRC16

// Tag for logging
private const val TAG = "EventRecordParser"

/**
 * Parses [Event Record]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC].
 *
 * @author Frank HJ Cuypers
 */
object EventRecordParser {
  /**
   * Parses a byte array with the contents of the [Event Record]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
   * into an [EventRecord].
   * @param eventRecord Byte array with the value read from the CCDT Record Characteristic.
   * @return An [EventRecord] holding the parsed result.
   *      Null if *eventRecord* is not 16 bytes long or the CRC16 is not correct.
   */
  fun parse(eventRecord: ByteArray): EventRecord? {
    Log.d(TAG, "ENTRY parse(eventRecord = $eventRecord)")

    if (eventRecord.size != 20) {
      Log.d(TAG, "eventRecord.size is not 20 but ${eventRecord.size} ")
      return null
    }

    val crc = eventRecord.fromUint16LE(18)
    val computedCrc = CRC16.modBus(eventRecord, 0, 18).toUShort()
    if (computedCrc != crc) {
      Log.d(TAG, "eventRecord crc is ${computedCrc.toHexString()} in stead of ${crc.toHexString()}")
      return null
    }

    val timestamp = eventRecord.fromUint32LE(0)
    val unknown1 = eventRecord[4]
    val unknown2 = eventRecord[5]
    val unknown3 = eventRecord[6]
    val unknown4 = eventRecord.copyOfRange(7, 18)

    val retval = EventRecord(
      timestamp, unknown1, unknown2, unknown3, unknown4
    )
    Log.d(TAG, "RETURN parse()=$retval")
    return retval

  }
}