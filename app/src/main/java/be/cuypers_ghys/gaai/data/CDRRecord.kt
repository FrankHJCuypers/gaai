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


/**
 * Holds the result of the [CDR Record]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CDR_RECORD_CHARACTERISTIC].
 * Seems to hold the information regarding a charging session.
 *
 * @author Frank HJ Cuypers
 */
data class CDRRecord(
  val unknown1: Int = 0, // Always 0??

  /**
   * Charging session start time in [Unix Time](https://en.wikipedia.org/wiki/Unix_time).
   */
  val sessionStartTime: UInt = 0u,

  /**
   * Total amount of energy in Wh since the installation of the charger, at the moment charging starts.
   */
  val sessionStartEnergy: UInt = 0u,

  val unknown2: UInt = 0u, // seems to be 0x1d or 0x00. Some kind of state?
  val unknown3: UInt = 0u, // seems to be 0x4A, 0x49 or  0x41. Some kind of state?

  /**
   * Charging session stop time in [Unix Time](https://en.wikipedia.org/wiki/Unix_time).
   */
  val sessionStopTime: UInt = 0u,

  /**
   * Total amount of energy in Wh since the installation of the charger, at the moment charging stops.
   */
  val sessionStopEnergy: UInt = 0u,

  val unknown4: Short = 0 // ?
)
