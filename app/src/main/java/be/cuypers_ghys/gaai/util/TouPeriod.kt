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

import java.util.Locale

/**
 * Handles a Time Of Use (tou) period with a [startTime] and an [endTime].
 *
 * @property startTime Start time.
 * @property endTime End time.
 * @constructor Creates an instance with the specified times.
 *
 * @author Frank HJ Cuypers
 */
data class TouPeriod(
  var startTime: TouTime,
  var endTime: TouTime
) {
  /**
   * Creates an instance with the specified times.
   * @param startTime Start time in minutes since midnight.
   * @param endTime End time in minutes since midnight.
   */
  constructor(startTime: Short, endTime: Short) :
      this(startTime = TouTime(startTime), endTime = TouTime(endTime))

  /**
   * Converts the period into a readable string.
   * @return Time period in "HH:MM - HH:MM" format
   */
  override fun toString(): String {
    return String.format(Locale.getDefault(), "%s - %s", startTime.toString(), endTime.toString())
  }
}