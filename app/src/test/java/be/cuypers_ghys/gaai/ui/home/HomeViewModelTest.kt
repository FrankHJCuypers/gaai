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

import app.cash.turbine.test
import be.cuypers_ghys.gaai.data.ChargerType
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.FakeDevicesSource
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * JUnit tests for [HomeViewModel].
 *
 * See [Working with StateFlows created by stateIn](https://developer.android.com/kotlin/flow/test#statein)
 * for specific info for testing StateFlows created by the
 * [stateIn](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/state-in.html).
 * operator.
 *
 * Uses [Turbine](https://github.com/cashapp/turbine)
 * @author Frank HJ Cuypers
 * @see https://developer.android.com/kotlin/flow/test
 */
class HomeViewModelTest {
  private fun getDeviceListSortedBySn(): List<Device> {
    return getDeviceListSortedBySn(FakeDevicesSource.devicesList)
  }

  private fun getDeviceListSortedBySn(deviceList: List<Device>): List<Device> {
    return deviceList.sortedWith(compareBy { it.sn })
  }

  var fakeRepository = FakeDevicesRepository()
  val homeViewModel = HomeViewModel(devicesRepository = fakeRepository)
  val homeUiStateFlow = homeViewModel.homeUiState

  @Test
  fun verifyHomeUiState() =
    runTest {
      homeUiStateFlow.test {
        // First item is always empty (see initialValue in stateIn() call of HomeViewModel.kt)
        val firstItem = awaitItem()
        assertEquals(0, firstItem.deviceList.size)

        // Now emit a new value in the fakeRepository and wait for it to be collected
        fakeRepository.emit(FakeDevicesSource.devicesList)
        val secondItem = awaitItem()
        assertEquals(FakeDevicesSource.devicesList.size, secondItem.deviceList.size)
        assertEquals(FakeDevicesSource.devicesList, secondItem.deviceList)
      }
    }

  @Test
  fun verifyRemoveDevice() =
    runTest {
      homeUiStateFlow.test {
        // First item is always empty (see initialValue in stateIn() call of HomeViewModel.kt
        val firstItem = awaitItem()
        assertEquals(0, firstItem.deviceList.size)

        // Now delete
        val dummyDevice = Device(11, "60211-A2", "2303-00008-E3", "FF:B8:37:72:4F:00", 2, ChargerType.HOME)
        homeViewModel.removeDevice(dummyDevice)

        val secondItem = awaitItem()
        assertEquals(1, secondItem.deviceList.size)
        assertEquals(listOf(dummyDevice), secondItem.deviceList)
      }
    }
}