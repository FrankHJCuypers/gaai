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
 * Holds the result of the Charging Grid Data BLE Characteristic.
 *
 * @author Frank HJ Cuypers
 */
data class ChargingGridData(
  /** Measurement time in Unix Time. */
  val timestamp: UInt = 0u,
  /**
   * Grid phase L1 current in dA.
   * A negative value indicates that a power surplus is delivered to the Grid.
   * That can only happen in case there are solar panels and solar power is more than what is
   * consumed by the house and the EV.
   */
  val l1: Short = 0,
  /**
   * Grid phase L2 current in dA.
   * @see l1
   */
  val l2: Short = 0,
  /**
   * Grid phase L3 current in dA.
   * @see l1
   */
  val l3: Short = 0,
  /**
   * Total grid power consumption in Watt.
   * @see l1
   */
  val consumed: Short = 0,
  /** Counter that goes from 0 to 900 (15 minutes)? */
  val interval: UShort = 0u,
)
