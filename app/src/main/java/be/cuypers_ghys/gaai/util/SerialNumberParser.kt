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

package be.cuypers_ghys.gaai.util

import java.util.regex.Pattern

/**
 * Parses serial numbers (SN).
 *
 * @author Frank HJ Cuypers
 */
object SerialNumberParser {

  /**
   * Regex that matches the serial number string.
   * The serial number is the SN of the Nexxtender Home,
   * as displayed on the sticker at the bottom of the box.
   * Its format is as follows:
   * - SN: YYMM-NNNNN-UU.
   *
   * with
   * - YY: Year of production, 2 decimal digits.
   * - MM: Month of production, 2 decimal digits.
   * - NNNNN: Serial number? 5 decimal digits (unique within YYMM?).
   * - UU: Unknown? 2 hexadecimal digits.
   *
   * The '-' is allowed to be absent.
   */
  private const val REGEX: String =
    "^([0-9]{2})([0-9]{2})(?:-|)([0-9]{5})(?:-|)([0-9a-fA-F]{2})$"

  /**
   * Parses a string of the format specified by [REGEX], representing a serial number.
   * @param serialNumberString The input string to parse.
   * @return A [SerialNumber] holding the parsed result.
   *  null if the serialNumberString did not match the [REGEX] regular expression.
   */
  fun parse(serialNumberString: String): SerialNumber? {
    val matcher = Pattern.compile(REGEX).matcher(serialNumberString)
    if (matcher.find() && matcher.groupCount() == 4) {
      // We verified that matcher.groupCount() == 4, so groups 1 to 4 will not return null
      val yearString = matcher.group(1)!!
      val monthString = matcher.group(2)!!
      val numberString = matcher.group(3)!!
      val unknownString = matcher.group(4)!!
      return SerialNumber(
        yearString.toUByte(),
        monthString.toUByte(),
        numberString.toUInt(),
        unknownString.toUByte(16)
      )
    }
    return null
  }

  /**
   * Returns an integer representing the *serialNumber* input.
   * @param serialNumber
   * @return A 4-byte unsigned integer 0xYY'MM'NNNN'.
   *  + YY' is the number that corresponds with the ASCII string YY.
   *  + MM' is the number that corresponds with the ASCII string MM.
   *  + NNNN' is the number that corresponds with the ASCII string NNNNN.
   */
  fun calcHexSerialNumber(serialNumber: SerialNumber): UInt {
    val hexSerialNumber =
      (serialNumber.year.toInt() shl 24) or (serialNumber.month.toInt() shl 16) or serialNumber.number.toInt()
    return hexSerialNumber.toUInt()
  }

  /**
   * Returns a string with the hexadecimal value returned by [calcHexSerialNumber].
   * @param serialNumber
   * @return String with the hexadecimal value returned by [calcHexSerialNumber]
   */
  @OptIn(ExperimentalStdlibApi::class)
  fun calcHexSerialNumberString(serialNumber: SerialNumber): String {
    return calcHexSerialNumber(serialNumber).toHexString()
  }
}