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
 * Possible event type values.
 *
 * @author Frank HJ Cuypers
 */
enum class EventType {
  CHARGE_START, CHARGING, CHARGE_STOP, UNKNOWN
}

/**
 * Holds the result of the [CCDT Record]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CCDT_RECORD_CHARACTERISTIC].
 *
 * @author Frank HJ Cuypers
 */
data class CCDTRecord(
  /** CCDT event time in [Unix Time](https://en.wikipedia.org/wiki/Unix_time). */
  val timestamp: UInt = 0u,

  /**
   * Total amount of energy in Wh since the installation of the charger, at the moment of the event.
   */
  val eventEnergy: UInt = 0u,

  /**
   * Total amount of energy in Wh since the start of the current quarter.
   */
  val quarterEnergy: UShort = 0u,

  /**
   * The type of the event.
   */
  val eventType: EventType = EventType.UNKNOWN,

  /**
   * Car phase L1 current in A.
   */
  val L1: UByte = 0u,

  /**
   * Car phase L2 current in A.
   */
  val L2: UByte = 0u,

  /**
   * Car phase L3 current in A.
   */
  val L3: UByte = 0u,
)
