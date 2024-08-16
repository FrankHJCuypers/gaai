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
 * Holds the result of the Config Get operation.
 *
 * @author Frank HJ Cuypers
 */
data class ConfigGetData(
    /** Maximum allowed grid consumption limit in A. */
    val maxGrid: UByte,
    /**
     * Maximum allowed charging speed in A for the device.
     */
    val maxDevice: UByte,
    /**
     * Default charging mode.
     */
    val mode: Mode,
    /** Minimum charging speed in A for the device. */
    val safe: UByte,
    /** Codes the network type. */
    val networkType: NetWorkType,
    /** Off peak charging start time of each weekday. Coded in minutes since midnight. */
    val touWeekStart: Short,
    /** Off peak charging end time of each weekday. Coded in minutes since midnight. */
    val touWeekEnd: Short,
    /** Off peak charging start time of each weekend day. Coded in minutes since midnight. */
    val touWeekendStart: Short,
    /** Off peak charging end time of each weekend day. Coded in minutes since midnight. */
    val touWeekendEnd: Short,
    /** ?*/
    val minDevice: UByte,
    /** ?*/
    val iCapacity: UByte,
    /** Is it a Config 1.0, Config 1.0 or Config_CBOR format. */
    val configVersion: ConfigVersion
)
