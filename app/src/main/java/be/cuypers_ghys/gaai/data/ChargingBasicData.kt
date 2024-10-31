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

/**
 * Discriminator.
 */
enum class Discriminator {
  STARTED, CHARGING, STOPPED, UNKNOWN
}

/**
 * Status.
 */
enum class Status {
  PLUGGED, CHARGING, FAULT, UNKNOWN
}

/**
 * Holds the result of the Charging Basic Data BLE Characteristic.
 *
 * @author Frank HJ Cuypers
 */
data class ChargingBasicData(
  /** Number of seconds since start of charging? */
  val seconds: UShort = 0u,
  /** State of the discriminator. */
  val discriminator: Discriminator = Discriminator.UNKNOWN,
  /** State of the charger. */
  val status: Status = Status.UNKNOWN,
  /** Not yet decoded Status */
  val rawStatus: Byte = 0,
  /** Total energy in Wh charged during this session? */
  val energy: UInt = 0u,
  /** Charging Phase Count?*/
  val phaseCount: UByte = 0u
)
