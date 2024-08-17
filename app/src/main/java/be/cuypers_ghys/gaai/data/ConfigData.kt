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
 * Charging mode.
 */
enum class Mode {
    ECO_PRIVATE, MAX_PRIVATE, ECO_OPEN, MAX_OPEN, UNKNOWN
}

/**
 * Codes the network type.
 */
@Suppress("SpellCheckingInspection")
enum class NetWorkType {
    MONO_TRIN, TRI, UNKNOWN
}

/**
 * Codes the Configuration version type.
 */
enum class ConfigVersion {
    CONFIG_1_0, CONFIG_1_1, CONFIG_CBOR
}

/**
 * Holds the Configuration.
 *
 * @author Frank HJ Cuypers
 */
data class ConfigData(
    /** Maximum allowed grid consumption limit in A. */
    val maxGrid: UByte,

    /**
     * Maximum allowed charging speed in A for the device.
     * Only available in [ConfigVersion.CONFIG_1_1] and [ConfigVersion.CONFIG_CBOR].
     */
    val maxDevice: UByte,

    /**
     * Default charging mode.
     */
    val mode: Mode,

    /** Minimum charging speed in A for the device. */
    val safe: UByte,

    /**
     * Codes the network type.
     * Only available in [ConfigVersion.CONFIG_1_1] and [ConfigVersion.CONFIG_CBOR].
     */
    val networkType: NetWorkType,

    /** Off peak charging start time of each weekday. Coded in minutes since midnight. */
    val touWeekStart: Short,

    /** Off peak charging end time of each weekday. Coded in minutes since midnight. */
    val touWeekEnd: Short,

    /** Off peak charging start time of each weekend day. Coded in minutes since midnight. */
    val touWeekendStart: Short,

    /** Off peak charging end time of each weekend day. Coded in minutes since midnight. */
    val touWeekendEnd: Short,

    /**
     * ?
     * Only available in [ConfigVersion.CONFIG_CBOR].
     */
    val minDevice: UByte,

    /**
     * ?
     * Only available in [ConfigVersion.CONFIG_CBOR].
     */
    val iCapacity: UByte,

    /** Which configuration is used?. */
    val configVersion: ConfigVersion
)
