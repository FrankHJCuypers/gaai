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

package be.cuypers_ghys.gaai.viewmodel

import java.util.UUID

object NexxtenderHomeSpecification {

  /** Base to convert 16-bit UUIDs into 128-bit UUIDs. */
  private const val UUID_BLE_SHORT_BASE = "0000%s-0000-1000-8000-00805f9b34fb"

  /** Base to convert 8-bit Nexxtender charger service or characteristic into 128-bit UUIDs. */
  private const val UUID_NEXXTENDER_HOME_SHORT_BASE = "fd47416a-95fb-4206-88b5-b4a8045f75"

  /** BLE Generic Access Service UUID. */
  val UUID_BLE_GENERIC_ACCESS_SERVICE: UUID = from16bitString("1800")

  /** BLE Device Name Characteristic UUID. */
  val UUID_BLE_DEVICE_NAME_CHARACTERISTIC: UUID = from16bitString("2a00")

  /** BLE Appearance Characteristic UUID. */
  val UUID_BLE_APPEARANCE_CHARACTERISTIC: UUID = from16bitString("2a01")

  /** BLE Peripheral Preferred Characteristic Characteristic UUID. */
  val UUID_BLE_PERIPHERAL_PREFERRED_CONNECTION_CHARACTERISTIC: UUID = from16bitString("2a04")

  /** BLE Generic Attribute Service UUID. */
  val UUID_BLE_GENERIC_ATTRIBUTE_SERVICE: UUID = from16bitString("1801")

  /** BLE Service Changed Characteristic UUID. */
  val UUID_BLE_SERVICE_CHANGED_CHARACTERISTIC: UUID = from16bitString("2a05")

  /** BLE Device Information Service UUID. */
  val UUID_BLE_DEVICE_INFORMATION_SERVICE: UUID = from16bitString("180a")

  /** BLE Model Number String Characteristic UUID. */
  val UUID_BLE_MODEL_NUMBER_STRING_CHARACTERISTIC: UUID = from16bitString("2a24")

  /** BLE Serial Number String Characteristic UUID. */
  val UUID_BLE_SERIAL_NUMBER_STRING_CHARACTERISTIC: UUID = from16bitString("2a25")

  /** BLE Firmware Revision String Characteristic UUID. */
  val UUID_BLE_FIRMWARE_REVISION_STRING_CHARACTERISTIC: UUID = from16bitString("2a26")

  /** BLE Hardware Revision String Characteristic UUID. */
  val UUID_BLE_HARDWARE_REVISION_STRING_CHARACTERISTIC: UUID = from16bitString("2a27")

  /** Nexxtender charger Service Data Service UUID. */
  val UUID_NEXXTENDER_CHARGER_SERVICE_DATA_SERVICE: UUID = fromNexxtenderHomeBase("c0")

  /** Nexxtender charger Generic/CDR Service UUID. */
  val UUID_NEXXTENDER_CHARGER_GENERIC_CDR_SERVICE: UUID = fromNexxtenderHomeBase("c1")

  /** Nexxtender charger CDR_COMMAND Characteristic UUID. */
  val UUID_NEXXTENDER_CHARGER_CDR_COMMAND_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("c2")

  /** Nexxtender charger CDR_STATUS Characteristic UUID. */
  val UUID_NEXXTENDER_CHARGER_CDR_STATUS_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("c3")

  /** Nexxtender charger CDR_RECORD Characteristic UUID. */
  val UUID_NEXXTENDER_CHARGER_CDR_RECORD_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("c4")

  /** Nexxtender Home GENERIC_COMMAND Characteristic UUID. */
  val UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("dd")

  /** Nexxtender Home GENERIC_STATUS Characteristic UUID. */
  val UUID_NEXXTENDER_HOME_GENERIC_STATUS_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("de")

  /** Nexxtender Home GENERIC_DATA Characteristic UUID. */
  val UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("df")

  /** Nexxtender charger CCDT Service UUID. */
  val UUID_NEXXTENDER_CHARGER_CCDT_SERVICE: UUID = fromNexxtenderHomeBase("c5")

  /** Nexxtender charger CCDT_COMMAND Characteristic UUID. */
  val UUID_NEXXTENDER_CHARGER_CCDT_COMMAND_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("c6")

  /** Nexxtender charger CCDT_STATUS Characteristic UUID. */
  val UUID_NEXXTENDER_CHARGER_CCDT_STATUS_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("c7")

  /** Nexxtender charger CCDT_RECORD Characteristic UUID. */
  val UUID_NEXXTENDER_CHARGER_CCDT_RECORD_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("c8")

  /** Nexxtender charger FIRMWARE Service UUID. */
  val UUID_NEXXTENDER_CHARGER_FIRMWARE_SERVICE: UUID = fromNexxtenderHomeBase("c9")

  /** Nexxtender charger FIRMWARE_COMMAND Characteristic UUID. */
  val UUID_NEXXTENDER_CHARGER_FIRMWARE_COMMAND_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("ca")

  /** Nexxtender charger FIRMWARE_STATUS Characteristic UUID. */
  val UUID_NEXXTENDER_CHARGER_FIRMWARE_STATUS_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("cb")

  /** Nexxtender charger FIRMWARE_WANTED_CHUNK Characteristic UUID. */
  val UUID_NEXXTENDER_CHARGER_FIRMWARE_WANTED_CHUNK_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("cc")

  /** Nexxtender charger FIRMWARE_DATA_CHUNK Characteristic UUID. */
  val UUID_NEXXTENDER_CHARGER_FIRMWARE_DATA_CHUNK_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("cd")

  /** Nexxtender charger CHARGING Service UUID. */
  val UUID_NEXXTENDER_CHARGER_CHARGING_SERVICE: UUID = fromNexxtenderHomeBase("ce")

  /** Nexxtender charger CHARGING_BASIC_DATA Characteristic UUID. */
  val UUID_NEXXTENDER_CHARGER_CHARGING_BASIC_DATA_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("cf")

  /** Nexxtender Home CHARGING_GRID_DATA Characteristic UUID. */
  val UUID_NEXXTENDER_HOME_CHARGING_GRID_DATA_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("d0")

  /** Nexxtender Home CHARGING_CAR_DATA Characteristic UUID. */
  val UUID_NEXXTENDER_HOME_CHARGING_CAR_DATA_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("da")

  /** Nexxtender Home CHARGING_ADVANCED_DATA Characteristic UUID. */
  val UUID_NEXXTENDER_HOME_CHARGING_ADVANCED_DATA_CHARACTERISTIC: UUID = fromNexxtenderHomeBase("db")

  /**
   * Creates a 128-bit UUID from a 16-bit UUID.
   * @param shortUUID 16-bit UUID
   * @return 128-bit UUID
   */
  private fun from16bitString(shortUUID: String): UUID {
    return UUID.fromString(String.format(UUID_BLE_SHORT_BASE, shortUUID))
  }

  /**
   * Creates a 128-bit UUID from a 8-bit UUID.
   * @param shortUUID 8-bit UUID
   * @return 128-bit UUID
   */
  private fun fromNexxtenderHomeBase(shortUUID: String): UUID {
    return UUID.fromString(String.format(UUID_NEXXTENDER_HOME_SHORT_BASE + shortUUID))
  }
}