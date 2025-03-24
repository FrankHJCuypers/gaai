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
 * Codes the charging mode.
 *
 * @author Frank HJ Cuypers
 */
enum class Mode {
  ECO_PRIVATE, MAX_PRIVATE, ECO_OPEN, MAX_OPEN, UNKNOWN
}

/**
 * Codes the network type.
 *
 * @author Frank HJ Cuypers
 */
@Suppress("SpellCheckingInspection")
enum class NetWorkType {
  MONO_TRIN, TRI, UNKNOWN
}

/**
 * Codes the Configuration version type.
 *
 * @author Frank HJ Cuypers
 */
enum class ConfigVersion {
  CONFIG_1_0, CONFIG_1_1, CONFIG_CBOR
}

/**
 * Holds the Configuration result of the operations
 * [CONFIG_OPERATION_SET][be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_OPERATION_SET],
 * [CONFIG_OPERATION_GET][be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_OPERATION_GET],
 * [CONFIG_OPERATION_CBOR_SET][be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_OPERATION_CBOR_SET] and
 * [CONFIG_OPERATION_CBOR_GET][be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_OPERATION_CBOR_GET].
 * These operations are available using the
 * [Generic Command]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC],
 * [Generic Status]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_STATUS_CHARACTERISTIC] and
 * [Generic Data]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
 * characteristics.
 *
 *
 * There are 3 variants of the configuration data.
 * 1. [ConfigVersion.CONFIG_1_0] which is used by all firmware versions before
 *   [VERSION_1_1_0][be.cuypers_ghys.gaai.data.ConfigDataParserComposer.VERSION_1_1_0].
 * 2. [ConfigVersion.CONFIG_1_1] which is used by all firmware versions from
 *   [VERSION_1_1_0][be.cuypers_ghys.gaai.data.ConfigDataParserComposer.VERSION_1_1_0]
 *   to [VERSION_3_50][be.cuypers_ghys.gaai.data.ConfigDataParserComposer.VERSION_3_50].
 *   Compared to [ConfigVersion.CONFIG_1_0] it adds the fields [maxDevice] and [networkType].
 * 3. [ConfigVersion.CONFIG_CBOR] which is used by all firmware versions from
 *   [VERSION_3_50][be.cuypers_ghys.gaai.data.ConfigDataParserComposer.VERSION_3_50]
 *   Compared to [ConfigVersion.CONFIG_1_1] it adds the fields [minDevice] and [iCapacity].
 *
 * @see ConfigDataParserComposer.getConfigVersion()
 * @author Frank HJ Cuypers
 */
data class ConfigData(
  /** Maximum allowed grid consumption limit in A. */
  val maxGrid: UByte = 0U,

  /**
   * Maximum allowed charging speed in A for the device.
   * Only available in [ConfigVersion.CONFIG_1_1] and [ConfigVersion.CONFIG_CBOR].
   */
  val maxDevice: UByte = 0U,

  /**
   * Default charging mode.
   */
  val mode: Mode = Mode.UNKNOWN,

  /** Minimum charging speed in A for the device. */
  val safe: UByte = 0U,

  /**
   * Codes the network type.
   * Only available in [ConfigVersion.CONFIG_1_1] and [ConfigVersion.CONFIG_CBOR].
   */
  val networkType: NetWorkType = NetWorkType.UNKNOWN,

  /** Off peak charging start time of each weekday. Coded in minutes since midnight. */
  val touWeekStart: Short = 0,

  /** Off peak charging end time of each weekday. Coded in minutes since midnight. */
  val touWeekEnd: Short = 0,

  /** Off peak charging start time of each weekend day. Coded in minutes since midnight. */
  val touWeekendStart: Short = 0,

  /** Off peak charging end time of each weekend day. Coded in minutes since midnight. */
  val touWeekendEnd: Short = 0,

  /**
   * ?
   * Only available in [ConfigVersion.CONFIG_CBOR].
   */
  val minDevice: UByte = 0U,

  /**
   * Peak grid current limit.
   * Only available in [ConfigVersion.CONFIG_CBOR].
   */
  val iCapacity: UByte = 0U,

  /** Which configuration is used?. */
  val configVersion: ConfigVersion = ConfigVersion.CONFIG_1_0,

  /**
   * Does this configuration still have its default values; i.e. not overwritten yet?
   */
  val default: Boolean = true
)
