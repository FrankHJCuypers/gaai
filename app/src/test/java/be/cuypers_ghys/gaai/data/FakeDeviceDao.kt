/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2025, Frank HJ Cuypers
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

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex

/**
 * Fake [DeviceDao] for [OfflineDevicesRepositoryTest] JUnit tests.
 * @author Frank HJ Cuypers
 */
class FakeDeviceDao(val initialDeviceList: List<Device>) : DeviceDao {
  val deviceList = initialDeviceList.toList().sortedWith(compareBy { it.sn }).toMutableList()
  val mutex = Mutex(locked = false)

  override fun getAllDevices(): Flow<List<Device>> = flow {
    while (true) {
      emit(deviceList)
      mutex.lock()
    }
  }

  override fun getDevice(id: Int): Flow<Device> = flow {
    val index = deviceList.indexOfFirst { it.id == id }
    if (index >= 0) {
      emit(deviceList[index])
      delay(1000)
      emit(deviceList[index])
      delay(1000)
    }
  }

  override suspend fun count(mac: String): Int {
    var count = 0
    deviceList.forEach {
      if (it.mac == mac) {
        count++
      }
    }
    return count
  }

  override suspend fun count(pn: String, sn: String): Int {
    var count = 0
    deviceList.forEach {
      if (it.pn == pn && it.sn == sn) {
        count++
      }
    }
    return count
  }

  override suspend fun insert(device: Device) {
    deviceList.add(device)
    deviceList.sortBy { it.sn }
    mutex.unlock()
  }

  override suspend fun update(device: Device) {
    val index = deviceList.indexOfFirst { it.id == device.id }
    if (index >= 0) {
      deviceList[index] = device
      deviceList.sortBy { it.sn }
    }
    mutex.unlock()
  }

  override suspend fun delete(device: Device) {
    val index = deviceList.indexOfFirst { it.id == device.id }
    if (index >= 0) {
      deviceList.removeAt(index)
      mutex.unlock()
    }
  }
}