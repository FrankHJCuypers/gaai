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

package be.cuypers_ghys.gaai.ui.home

import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.DevicesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Fake [DevicesRepository] for [HomeViewModelTest] JUnit tests.
 * @author Frank HJ Cuypers
 */
class FakeDevicesRepository : DevicesRepository {
  private val flow = MutableSharedFlow<List<Device>>()
  suspend fun emit(value: List<Device>) = flow.emit(value)
  override fun getAllDevicesStream(): Flow<List<Device>> = flow

  override fun getDeviceStream(id: Int): Flow<Device?> {
    TODO("Not yet implemented")
  }

  override suspend fun canInsert(device: Device): Boolean {
    TODO("Not yet implemented")
  }

  override suspend fun insertDevice(device: Device) {
    TODO("Not yet implemented")
  }

  /**
   * Fake delete emits the specified record for verification by the JUnit test
   */
  override suspend fun deleteDevice(device: Device) {
    flow.emit(listOf(device))
  }
}