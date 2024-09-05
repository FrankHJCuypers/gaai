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

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update, delete, and retrieve of [Device] from a given data source.
 */
interface DevicesRepository {
    /**
     * Retrieve all the devices from the the given data source.
     */
    fun getAllDevicesStream(): Flow<List<Device>>

    /**
     * Retrieve a device from the given data source that matches with the [id].
     */
    fun getDeviceStream(id: Int): Flow<Device?>

    /**
     * Count the number of devices already in the db with this mac.
     * @param mac
     */
    suspend fun count(mac: String) : Int

    /**
     * Count the number of devices already in the db with this pn and sn.
     * @param pn
     * @param sn
     */
    suspend fun count(pn: String, sn:String) : Int

    /**
     * @return true if this device can be inserted with [insertDevice].
     */
    suspend fun canInsert(device:Device) : Boolean

    /**
     * Insert device in the data source
     */
    suspend fun insertDevice(device: Device)

    /**
     * Delete device from the data source
     */
    suspend fun deleteDevice(device: Device)

    /**
     * Update device in the data source
     */
    suspend fun updateDevice(device: Device)
}
