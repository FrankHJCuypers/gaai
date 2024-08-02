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

object SerialNumber {
    fun calcSerialNumber(str: String): Int {
        val split = str.split("-".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val numberString: String?
        var yearMonthString: String? = null
        when (split.size) {
            5 -> {
                yearMonthString = split[2]
                numberString = split[3]
            }
            3 -> {
                yearMonthString = split[0]
                numberString = split[1]
            }
            else -> {
                numberString = null
            }
        }
        if (yearMonthString == null || numberString == null || yearMonthString.length != 4) {
            return 0
        }
        val yearString = yearMonthString.substring(0, 2)
        val monthString = yearMonthString.substring(2, 4)
        val year = yearString.toInt()
        val month = monthString.toInt()
        val number = numberString.toInt()

        val serialNumber = (year shl 24) or (month shl 16) or number
        return serialNumber
    }

    fun calcSerialNumberString(str: String): String {
        val serialNumber = calcSerialNumber(str)
        val serialNumberString = String.format("%08X", serialNumber)
        return serialNumberString
    }
}
