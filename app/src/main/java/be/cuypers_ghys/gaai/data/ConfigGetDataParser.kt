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

import be.cuypers_ghys.gaai.util.MODBUS
import be.cuypers_ghys.gaai.util.fromInt16LE
import be.cuypers_ghys.gaai.util.fromUint16LE
import no.nordicsemi.android.kotlin.ble.profile.common.CRC16

/**
 * Parses Config Get operation data.
 *
 * @author Frank HJ Cuypers
 */
object ConfigGetDataParser {
    /**
     * Parses a byte array with the contents of the Config Get operation in Config_1_0 format into an
     * [ConfigGetData].
     * @param configGetData Byte array with the value read from the Config Get operation.
     * @return A [ConfigGetData] holding the parsed result.
     *      Null if *configGetData* is not 13 long or the CRC16 is not correct.
     */
    fun parseConfig_1_0(configGetData: ByteArray): ConfigGetData? {
        if (configGetData.size !=  13)
        {
            return null
        }

        return parse(configGetData, ConfigVersion.CONFIG_1_0)
    }

    /**
     * Parses a byte array with the contents of the Config Get operation in Config_1_1 format into an
     * [ConfigGetData].
     * @param configGetData Byte array with the value read from the Config Get operation.
     * @return A [ConfigGetData] holding the parsed result.
     *      Null if *configGetData* is not 15 long or the CRC16 is not correct.
     */
    fun parseConfig_1_1(configGetData: ByteArray): ConfigGetData? {
        if (configGetData.size !=  15)
        {
            return null
        }

        return parse(configGetData, ConfigVersion.CONFIG_1_1)
    }

    /**
     * Parses a byte array with the contents of the Config Get operation into an
     * [ConfigGetData].
     * @param configGetData Byte array with the value read from the Config Get operation.
     * @param configVersion The configutation version.
     * @return A [ConfigGetData] holding the parsed result.
     *      Null if *configGetData* is not 13 or 15 bytes long or the CRC16 is not correct.
     */
    fun parse(configGetData: ByteArray, configVersion : ConfigVersion): ConfigGetData? {
        if ((configGetData.size !=  13) && (configGetData.size !=  15))
        {
            return null
        }

        val crc =  configGetData.fromUint16LE(configGetData.size-2)
        val computedCrc = CRC16.MODBUS(configGetData,0, configGetData.size-2).toUShort()
        if ( computedCrc != crc )
        {
            return null
        }

        var offset = 0
        val maxGrid = configGetData[offset++].toUByte()

        var maxDevice = 0.toUByte()
        if ( configVersion == ConfigVersion.CONFIG_1_1 )
        {
            maxDevice = configGetData[offset++].toUByte()
        }

        val rawMode = configGetData[offset++]
        val mode = when(rawMode) {
            0.toByte() -> Mode.ECO_PRIVATE
            1.toByte() -> Mode.MAX_PRIVATE
            4.toByte() -> Mode.ECO_OPEN
            5.toByte() -> Mode.MAX_OPEN
            else -> Mode.UNKNOWN

        }
        val safe=  configGetData[offset++].toUByte()

        var networkType = NetWorkType.UNKNOWN
        if ( configVersion == ConfigVersion.CONFIG_1_1 )
        {
            val rawNetworkType = configGetData[offset++]
            networkType = when(rawNetworkType) {
                0.toByte() -> NetWorkType.MONO_TRIN
                2.toByte() -> NetWorkType.TRI
                else -> NetWorkType.UNKNOWN
            }
        }
        val touWeekStart = configGetData.fromInt16LE(offset)
        offset += 2
        val touWeekEnd = configGetData.fromInt16LE(offset)
        offset += 2
        val touWeekendStart = configGetData.fromInt16LE(offset)
        offset += 2
        val touWeekendEnd = configGetData.fromInt16LE(offset)
        return  ConfigGetData(
            maxGrid,
            maxDevice,
            mode,
            safe,
            networkType ,
            touWeekStart,
            touWeekEnd,
            touWeekendStart,
            touWeekendEnd,
            configVersion
        )
    }


}