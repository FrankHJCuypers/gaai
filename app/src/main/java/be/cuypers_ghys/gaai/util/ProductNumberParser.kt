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
 * Parses product numbers (PN).
 *
 * @author Frank HJ Cuypers
 */
object ProductNumberParser {

  /**
   * Regex that matches the product number string.
   * The product number is the PN of the Nexxtender Home,
   * as displayed on the sticker at the bottom of the box.
   * Its format is as follows:
   * - PN: AAAAA-RR.
   *
   * with
   * - AAAAA: Model Number String, 5 decimal digits.
   * - RR: Hardware Revision String, 2 hexadecimal digits.
   *
   * The '-' is allowed to be absent.
   */
  private const val REGEX: String =
    "^([0-9]{5})(?:-|)([0-9a-fA-F]{2})$"

  /**
   * Parses a string in the format specified by [REGEX], representing a product number.
   * @param productNumberString The input string to parse.
   * @return A [ProductNumber] holding the parsed result.
   *  null if the productNumberString did not match the [REGEX] regular expression.
   */
  fun parse(productNumberString: String): ProductNumber? {
    val matcher = Pattern.compile(REGEX).matcher(productNumberString)
    if (matcher.find() && matcher.groupCount() == 2) {
      // We verified that matcher.groupCount() == 2, so groups 1 to 2 will not return null
      val modelNumberString = matcher.group(1)!!
      val hardwareRevisionString = matcher.group(2)!!
      return ProductNumber(
        modelNumberString.toUInt(),
        hardwareRevisionString.toUByte(16)
      )
    }
    return null
  }
}