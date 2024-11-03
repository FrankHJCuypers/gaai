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
 * Codes the Operation IDs and Status IDs used in the Generic Service.
 * These operations are available using the
 * [Generic Command]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC],
 * [Generic Status]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_STATUS_CHARACTERISTIC] and
 * [Generic Data]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
 * characteristics.
 *
 * The Operation IDs and Status IDs consists of 16 bits.
 * The MS Byte codes the operation and Status types:
 * + [LOADER]
 * + [EVENT]
 * + [METRIC]
 * + [BADGE]
 * + [TIME]
 * + [CONFIG]
 *
 * The LS Byte code the specific Operation and Status within the type.
 *
 * @author Frank HJ Cuypers
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object OperationAndStatusIDs {
  const val LOADER = 0x0000
  const val EVENT = 0x1000
  const val METRIC = 0x2000
  const val BADGE = 0x3000
  const val TIME = 0x4000
  const val CONFIG = 0x5000

  const val LOADER_OPERATION_START_CHARGING_DEFAULT = LOADER + 0x01
  const val LOADER_OPERATION_START_CHARGING_MAX = LOADER + 0x02
  const val LOADER_OPERATION_START_CHARGING_AUTO = LOADER + 0x03
  const val LOADER_OPERATION_START_CHARGING_ECO = LOADER + 0x04
  const val LOADER_OPERATION_STOP_CHARGING = LOADER + 0x06
  const val LOADER_STATUS_UNLOCKED = LOADER + 0x01
  const val LOADER_STATUS_UNLOCKED_FORCE_MAX = LOADER + 0x02

  const val EVENT_OPERATION_NEXT = EVENT + 0x01
  const val EVENT_OPERATION_UPDATE_STATUS = EVENT + 0x02
  const val EVENT_STATUS_BASE = EVENT + 0x00

  const val METRIC_OPERATION_NEXT = METRIC + 0x01
  const val METRIC_OPERATION_UPDATE_STATUS = METRIC + 0x02
  const val METRIC_STATUS_BASE = METRIC + 0x00

  const val BADGE_OPERATION_ADD_DEFAULT = BADGE + 0x01
  const val BADGE_OPERATION_ADD_MAX = BADGE + 0x02
  const val BADGE_OPERATION_DELETE = BADGE + 0x04
  const val BADGE_OPERATION_LIST_START = BADGE + 0x05
  const val BADGE_OPERATION_LIST_NEXT = BADGE + 0x06
  const val BADGE_STATUS_WAIT_ADD1 = BADGE + 0x01
  const val BADGE_STATUS_WAIT_ADD2 = BADGE + 0x02
  const val BADGE_STATUS_WAIT_DELETE = BADGE + 0x04
  const val BADGE_STATUS_WAIT_NEXT = BADGE + 0x05
  const val BADGE_STATUS_WAIT_FINISH = BADGE + 0x07
  const val BADGE_STATUS_WAIT_ADDED = BADGE + 0x08
  const val BADGE_STATUS_WAIT_EXISTS = BADGE + 0x09

  const val TIME_OPERATION_SET = TIME + 0x01
  const val TIME_OPERATION_GET = TIME + 0x02
  const val TIME_STATUS_READY = TIME + 0x01
  const val TIME_STATUS_SUCCESS = TIME + 0x02
  const val TIME_STATUS_POPPED = TIME + 0x03

  const val CONFIG_OPERATION_SET = CONFIG + 0x01
  const val CONFIG_OPERATION_GET = CONFIG + 0x02
  const val CONFIG_OPERATION_CBOR_SET = CONFIG + 0x03
  const val CONFIG_OPERATION_CBOR_GET = CONFIG + 0x04
  const val CONFIG_STATUS_READY = CONFIG + 0x01
  const val CONFIG_STATUS_SUCCESS = CONFIG + 0x02
  const val CONFIG_STATUS_POPPED = CONFIG + 0x03
  const val CONFIG_STATUS_READY_CBOR = CONFIG + 0x04
  const val CONFIG_STATUS_SUCCESS_CBOR = CONFIG + 0x05
  const val CONFIG_STATUS_POPPED_CBOR = CONFIG + 0x06
}