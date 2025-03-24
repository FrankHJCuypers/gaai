/*
 * Project Gaai: one app to control the Nexxtender chargers.
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
 * Holds the Time result of the operations
 * [TIME_OPERATION_SET][be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_OPERATION_SET],
 * [TIME_OPERATION_GET][be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_OPERATION_GET].
 * These operations are available using the
 * [Generic Command]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC],
 * [Generic Status]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_STATUS_CHARACTERISTIC] and
 * [Generic Data]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
 * characteristics.
 *
 * @author Frank HJ Cuypers
 */
data class TimeData(
  /** Time in [Unix Time](https://en.wikipedia.org/wiki/Unix_time). */
  val time: UInt = 0u
)
