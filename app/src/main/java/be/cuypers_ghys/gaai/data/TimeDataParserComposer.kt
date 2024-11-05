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

import be.cuypers_ghys.gaai.util.fromUint32LE
import be.cuypers_ghys.gaai.util.toUint32LE

/**
 *  Parses and composes Time data.
 *
 * @author Frank HJ Cuypers
 */
object TimeDataParserComposer {
  /**
   * Parses a byte array with the contents of the Time Get operation into a [TimeData].
   * @param timeGetData Byte array with the value read from the Time Get operation.
   * @return A [TimeData] holding the parsed result.
   *      Null if *timeGetData* is not 4 bytes long.
   */
  fun parse(timeGetData: ByteArray): TimeData? {
    if (timeGetData.size != 4) {
      return null
    }
    val time = timeGetData.fromUint32LE(0)

    return TimeData(time)
  }

  /**
   * Composes a byte array with the contents of the Time Set operation from an [TimeData].
   * @param timeData The data to compose.
   * @return Byte array with the compose configuration.
   */
  fun compose(timeData: TimeData): ByteArray {
    val data = ByteArray(4)
    var offset = 0
    data.toUint32LE(offset, timeData.time)
    return data
  }
}