/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2025-2026, Frank HJ Cuypers
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

import android.util.Log
import app.cash.turbine.test
import be.cuypers_ghys.gaai.ble.BleRepository
import be.cuypers_ghys.gaai.data.ChargerType
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.FakeDevicesSource
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

// Tag for logging
private const val TAG = "HomeViewModelTest"

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

// TODO: move to androidTest
@Disabled("Junit test can not use context. Should be moved to androidTest")
class HomeViewModelTest {
  private fun getDeviceListSortedBySn(): List<Device> {
    return getDeviceListSortedBySn(FakeDevicesSource.devicesList)
  }

  private fun getDeviceListSortedBySn(deviceList: List<Device>): List<Device> {
    return deviceList.sortedWith(compareBy { it.sn })
  }

  var fakeDevicesRepository = FakeDevicesRepository()
  val context = TestScope().coroutineContext

  val bleRepositoryMock = mockk<BleRepository>()

  val homeViewModel = HomeViewModel(devicesRepository = fakeDevicesRepository, bleRepository = bleRepositoryMock)
  val homeUiStateFlow = homeViewModel.homeUiState

  @Test
  fun verifyHomeUiState() =
    runTest {

      homeUiStateFlow.test {
        Log.d(TAG, "ENTRY verifyHomeUiState() homeUiStateFlow.test body")
        this.coroutineContext
        // First item is always empty (see initialValue in stateIn() call of HomeViewModel.kt)
        val firstItem = awaitItem()
        Log.d(TAG, "verifyHomeUiState() homeUiStateFlow.test firstItem=$firstItem")
        assertEquals(0, firstItem.deviceStateList.size)

        // Now emit a new value in the fakeRepository and wait for it to be collected
        Log.d(TAG, "verifyHomeUiState() homeUiStateFlow.test emit fakeRepository")
        fakeDevicesRepository.emit(FakeDevicesSource.devicesList)
        Log.d(TAG, "verifyHomeUiState() homeUiStateFlow.test await second item")
        val secondItem = awaitItem()
        Log.d(TAG, "verifyHomeUiState() homeUiStateFlow.test secondItem=$secondItem")
        assertEquals(FakeDevicesSource.devicesList.size, secondItem.deviceStateList.size)
        assertEquals(FakeDevicesSource.devicesList, secondItem.deviceStateList)
        cancelAndIgnoreRemainingEvents()
        Log.d(TAG, "verifyHomeUiState() EXIT homeUiStateFlow.test body")
      }
    }

  @Test
  fun verifyRemoveDevice() =
    runTest {
      homeUiStateFlow.test {
        Log.d(TAG, "ENTRY verifyRemoveDevice() homeUiStateFlow.test body")
        // First item is always empty (see initialValue in stateIn() call of HomeViewModel.kt
        val firstItem = awaitItem()
        Log.d(TAG, "verifyRemoveDevice() homeUiStateFlow.test firstItem=$firstItem")

        assertEquals(0, firstItem.deviceStateList.size)

        // Now delete
        val dummyDevice = Device(11, "60211-A2", "2303-00008-E3", "FF:B8:37:72:4F:00", 2, ChargerType.HOME)
        homeViewModel.removeDevice(dummyDevice)
        Log.d(TAG, "verifyRemoveDevice() homeUiStateFlow.test await second item")
        val secondItem = awaitItem()
        Log.d(TAG, "verifyRemoveDevice() homeUiStateFlow.test secondItem=$secondItem")
        assertEquals(1, secondItem.deviceStateList.size)
        assertEquals(listOf(dummyDevice), secondItem.deviceStateList)
        cancelAndIgnoreRemainingEvents()
        Log.d(TAG, "verifyRemoveDevice() EXIT homeUiStateFlow.test body")
      }
    }
}