/*
 * Project Gaai: one app to control the Nexxtender Home charger.
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
 * Holds the result of the [Event Record]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC].
 *
 * @author Frank HJ Cuypers
 */
data class EventRecord(
  /** Event time in [Unix Time](https://en.wikipedia.org/wiki/Unix_time). */
  val timestamp: UInt = 0u,
  val unknown1: Byte = 0, // In a small sample I observed the values 02 and 06
  val unknown2: Byte = 0, // In a small sample I only observed the value 01. Status flag?
  val unknown3: Byte = 0, // In a small sample I observed the values 01 and 08. Status flag?
  val unknown4: ByteArray = ByteArray(11) //data?
)
