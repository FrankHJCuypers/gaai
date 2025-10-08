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
private const val TAG = "CCDTRecordParser"

/**
 * Parses [CCDT Record]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CCDT_RECORD_CHARACTERISTIC].
 *
 * @author Frank HJ Cuypers
 */
object CCDTRecordParser {
  /**
   * Parses a byte array with the contents of the [CCDT Record]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CCDT_RECORD_CHARACTERISTIC]
   * into an [CCDTRecord].
   * @param ccdtRecord Byte array with the value read from the CCDT Record Characteristic.
   * @return A [CCDTRecord] holding the parsed result.
   *      Null if *ccdtRecord* is not 16 bytes long or the CRC16 is not correct.
   */
  fun parse(ccdtRecord: ByteArray): CCDTRecord? {
    Log.d(TAG, "ENTRY parse(ccdtRecord = $ccdtRecord)")

    if (ccdtRecord.size != 16) {
      Log.d(TAG, "ccdtRecord.size is not 16 but ${ccdtRecord.size} ")
      return null
    }

    val crc = ccdtRecord.fromUint16LE(14)
    val computedCrc = CRC16.modBus(ccdtRecord, 0, 14).toUShort()
    if (computedCrc != crc) {
      Log.d(TAG, "ccdtRecord crc is ${computedCrc.toHexString()} in stead of ${crc.toHexString()}")
      return null
    }

    val timestamp = ccdtRecord.fromUint32LE(0)
    val eventEnergy = ccdtRecord.fromUint32LE(4)
    val quarterEnergy = ccdtRecord.fromUint16LE(8)
    val rawEventType = ccdtRecord[10]
    val eventType = when (rawEventType) {
      0x42.toByte() -> EventType.CHARGE_START
      0x49.toByte() -> EventType.CHARGE_STOP
      0x4A.toByte() -> EventType.CHARGING
      0x60.toByte() -> EventType.UNKNOWN
      else -> EventType.UNKNOWN
    }
    val l1 = ccdtRecord[11].toUByte()
    val l2 = ccdtRecord[12].toUByte()
    val l3 = ccdtRecord[13].toUByte()

    val retval = CCDTRecord(
      timestamp, eventEnergy, quarterEnergy, eventType, l1, l2, l3
    )
    Log.d(TAG, "RETURN parse()=$retval")
    return retval
  }
}