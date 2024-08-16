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
import com.google.iot.cbor.CborInteger
import com.google.iot.cbor.CborMap
import no.nordicsemi.android.kotlin.ble.profile.common.CRC16

/** Enumerates all CONFIG_CBOR keys. s*/
enum class CborKey(val keyNum: Int) {
    ChargeMode(1),
    ModbusSlaveAddress(2),
    CycleRate(3),
    IMax(4),
    IEvseMax(5),
    IEvseMin(6),
    ILevel1(7),
    SolarMode(8),
    PhaseSeq(9),
    ChargingPhases(10),
    BlePin(11),
    TouWeekStart(12),
    TouWeekStop(13),
    TouWeekendStart(14),
    TouWeekendStop(15),
    Timezone(16),
    RelayOffPeriod(17),
    ExternalRegulation(18),
    ICapacity(19)
}
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
     * @param configVersion The configuration version.
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
        val mode = getMode(rawMode.toInt())
        val safe=  configGetData[offset++].toUByte()

        var networkType = NetWorkType.UNKNOWN
        if ( configVersion == ConfigVersion.CONFIG_1_1 )
        {
            val rawNetworkType = configGetData[offset++]
            networkType = getNetWorkType(rawNetworkType.toInt())
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
            0u,
            0u,
            configVersion
        )
    }

    private fun getNetWorkType(rawNetworkType: Int) = when (rawNetworkType) {
        0 -> NetWorkType.MONO_TRIN
        2 -> NetWorkType.TRI
        else -> NetWorkType.UNKNOWN
    }

    private fun getMode(rawMode: Int) = when (rawMode) {
        0 -> Mode.ECO_PRIVATE
        1 -> Mode.MAX_PRIVATE
        4 -> Mode.ECO_OPEN
        5 -> Mode.MAX_OPEN
        else -> Mode.UNKNOWN
    }

    /**
     * Parses a byte array with the contents of the Config Get operation in Config_CBOR format into an
     * [ConfigGetData].
     * @param configGetData Byte array with the value read from the Config Get operation.
     * @return A [ConfigGetData] holding the parsed result.
     *      Null if the CRC16 is not correct, configData is not correctly CBOR coded.
     */
    fun parseConfig_CBOR(configGetData: ByteArray): ConfigGetData? {
        val crc =  configGetData.fromUint16LE(configGetData.size-2)
        val computedCrc = CRC16.MODBUS(configGetData,0, configGetData.size-2).toUShort()
        if ( computedCrc != crc ){
            return null
        }

        val cborMap = CborMap.createFromCborByteArray(configGetData, 0, configGetData.size-2 ) ?: return null
        val submap0 = cborMap.get(CborInteger.create(0)) ?: return null
        val submap1 = cborMap.get(CborInteger.create(1)) ?: return null
        if ( submap1 !is CborMap) return null

        var maxGrid: UByte = 0u
        var maxDevice: UByte = 0u
        var mode: Mode = Mode.UNKNOWN
        var safe: UByte = 0u
        var networkType: NetWorkType = NetWorkType.UNKNOWN
        var touWeekStart: Short = 0
        var touWeekEnd: Short = 0
        var touWeekendStart: Short = 0
        var touWeekendEnd: Short = 0
        var minDevice: UByte = 0u
        var iCapacity: UByte = 0u
        val configVersion = ConfigVersion.CONFIG_CBOR

        submap1.mapValue().forEach { entry ->
            val intValue = entry.value as? CborInteger
            val intKey = entry.key as? CborInteger
            if ( (intValue!= null ) && (intKey != null)) {
                val rawInt = intValue.intValueExact()
                val rawKey = intKey.intValueExact()
                when( rawKey ){
                    CborKey.ChargeMode.keyNum -> mode = getMode(rawInt)
                    CborKey.IMax.keyNum -> maxGrid = rawInt.toUByte()
                    CborKey.IEvseMax.keyNum -> maxDevice = rawInt.toUByte()
                    CborKey.IEvseMin.keyNum -> minDevice = rawInt.toUByte()
                    CborKey.ILevel1.keyNum -> safe = rawInt.toUByte()
                    CborKey.PhaseSeq.keyNum -> networkType = getNetWorkType(rawInt )
                    CborKey.TouWeekStart.keyNum -> touWeekStart = rawInt.toShort()
                    CborKey.TouWeekStop.keyNum -> touWeekEnd = rawInt.toShort()
                    CborKey.TouWeekendStart.keyNum -> touWeekendStart = rawInt.toShort()
                    CborKey.TouWeekendStop.keyNum -> touWeekendEnd = rawInt.toShort()
                    CborKey.ICapacity.keyNum -> iCapacity = rawInt.toUByte()
                }
            }
        }
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
            minDevice,
            iCapacity,
            configVersion
        )
    }
}