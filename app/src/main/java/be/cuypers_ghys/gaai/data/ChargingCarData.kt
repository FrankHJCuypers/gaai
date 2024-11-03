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
 * Holds the result of the [Charging Car Data BLE Characteristic]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_CAR_DATA_CHARACTERISTIC].
 *
 * @author Frank HJ Cuypers
 */
data class ChargingCarData(
  /** Measurement time in [Unix Time](https://en.wikipedia.org/wiki/Unix_time). */
  val timestamp: UInt = 0u,

  /**
   * Car phase L1 current in dA.
   */
  val l1: Short = 0,

  /**
   * Car phase L2 current in dA.
   */
  val l2: Short = 0,

  /**
   * Car phase L3 current in dA.
   */
  val l3: Short = 0,

  /**
   * Car phase L1 power consumption in W.
   */
  val p1: Short = 0,

  /**
   * Car phase L2 power consumption in W.
   */
  val p2: Short = 0,

  /**
   * Car phase L3 power consumption in W.
   */
  val p3: Short = 0
)
