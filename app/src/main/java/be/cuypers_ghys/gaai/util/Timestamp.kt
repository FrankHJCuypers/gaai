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
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

/**
 * Handles the Timestamp fields in data coming from the Nexxtender Home.
 *
 * @author Frank HJ Cuypers
 */
object Timestamp {

    /**
     * Converts the timestamp into a readable string.
     * @param timeStamp As received from the Nexxtender Home.
     * @return String representation of the timestamp.
     *  null if the productNumberString did not match the [REGEX] regular expression.
     */
    // TODO: Use format without T and Z letters. Note that commented out java.time format crashes the app.
    // TODO: junit tests
    fun toString(timeStamp: UInt): String {
//        return java.time.format.DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
//            .format(java.time.Instant.ofEpochSecond(timeStamp.toLong()))
        val gmtTime = Instant.fromEpochSeconds(timeStamp.toLong())
        val localDateTime = gmtTime.toLocalDateTime(TimeZone.currentSystemDefault())
        val format = LocalDateTime.Format {
            year()
            char('-')
            monthNumber()
            char('-')
            dayOfMonth()

            char(' ')

            hour()
            char(':')
            minute()
            char(':')
            second()
        }

        return format.format(localDateTime)
    }
}