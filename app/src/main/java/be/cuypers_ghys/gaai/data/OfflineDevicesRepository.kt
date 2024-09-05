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
 * Local devices database
 */
class OfflineDevicesRepository(private val deviceDao: DeviceDao) : DevicesRepository {
    override fun getAllDevicesStream(): Flow<List<Device>> = deviceDao.getAllDevices()

    override fun getDeviceStream(id: Int): Flow<Device?> = deviceDao.getDevice(id)

    override suspend fun count(mac: String)  = deviceDao.count(mac)

    override suspend fun count(pn: String, sn: String)  = deviceDao.count(pn, sn)

    override suspend fun canInsert(device:Device)  = deviceDao.canInsert(device)

    override suspend fun insertDevice(device: Device) = deviceDao.insert(device)

    override suspend fun deleteDevice(device: Device) = deviceDao.delete(device)

    override suspend fun updateDevice(device: Device) = deviceDao.update(device)
}
