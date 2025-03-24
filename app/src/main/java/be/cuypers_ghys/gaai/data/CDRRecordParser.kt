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

import be.cuypers_ghys.gaai.util.fromInt16LE
import be.cuypers_ghys.gaai.util.fromInt32LE
import be.cuypers_ghys.gaai.util.fromUint16LE
import be.cuypers_ghys.gaai.util.fromUint32LE
import be.cuypers_ghys.gaai.util.modBus
import no.nordicsemi.android.kotlin.ble.profile.common.CRC16

/**
 * Parses [CDR Record]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CDR_RECORD_CHARACTERISTIC].
 *
 * @author Frank HJ Cuypers
 */
object CDRRecordParser {
  /**
   * Parses a byte array with the contents of the [CDR Record]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CDR_RECORD_CHARACTERISTIC]
   * into an [CDRRecord].
   * @param cdrRecord Byte array with the value read from the CDR Record Characteristic.
   * @return A [CDRRecord] holding the parsed result.
   *      Null if *cdrRecord* is not 16 bytes long or the CRC16 is not correct.
   */
  fun parse(cdrRecord: ByteArray): CDRRecord? {
    if (cdrRecord.size != 32) {
      return null
    }

    val crc = cdrRecord.fromUint16LE(30)
    val computedCrc = CRC16.modBus(cdrRecord, 0, 30).toUShort()
    if (computedCrc != crc) {
      return null
    }

    val unknown1 = cdrRecord.fromInt32LE(0)
    val sessionStartTime = cdrRecord.fromUint32LE(4)
    val sessionStartEnergy = cdrRecord.fromUint32LE(8)
    val unknown2 = cdrRecord.fromUint32LE(12)
    val unknown3 = cdrRecord.fromUint32LE(16)
    val sessionStopTime = cdrRecord.fromUint32LE(20)
    val sessionStopEnergy = cdrRecord.fromUint32LE(24)
    val unknown4 = cdrRecord.fromInt16LE(28)

    return CDRRecord(
      unknown1, sessionStartTime, sessionStartEnergy, unknown2, unknown3, sessionStopTime, sessionStopEnergy, unknown4
    )
  }
}