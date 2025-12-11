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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * JUnit tests for [OfflineDevicesRepository].
 * @author Frank HJ Cuypers
 */
class OfflineDevicesRepositoryTest {
  lateinit var repository: OfflineDevicesRepository

  private fun getDeviceListSortedBySn(): List<Device> {
    return getDeviceListSortedBySn(FakeDevicesSource.devicesList)
  }

  private fun getDeviceListSortedBySn(deviceList: List<Device>): List<Device> {
    return deviceList.sortedWith(compareBy { it.sn })
  }

  @BeforeEach
  fun setup() {
    repository = OfflineDevicesRepository(FakeDeviceDao(FakeDevicesSource.devicesList))
  }

  @Test
  fun verifyCanInsertUnique() =
    runTest {
      val newDevice = Device(0, "1", "2", "3", 2, ChargerType.HOME)
      assertTrue(repository.canInsert(newDevice))
    }

  @Test
  fun verifyCannotInsertDuplicateMac() =
    runTest {
      val newDevice = Device(0, "1", "2", FakeDevicesSource.devicesList[0].mac, 2, ChargerType.HOME)
      assertFalse(repository.canInsert(newDevice))
    }

  @Test
  fun verifyCanInsertDuplicateSn() =
    runTest {
      val newDevice = Device(0, "1", FakeDevicesSource.devicesList[0].sn, "3", 2, ChargerType.HOME)
      assertTrue(repository.canInsert(newDevice))
    }

  @Test
  fun verifyCanInsertDuplicatePn() =
    runTest {
      val newDevice = Device(0, FakeDevicesSource.devicesList[0].pn, "2", "3", 2, ChargerType.HOME)
      assertTrue(repository.canInsert(newDevice))
    }

  @Test
  fun verifyCannotInsertDuplicatePnSn() =
    runTest {
      val newDevice =
        Device(0, FakeDevicesSource.devicesList[0].pn, FakeDevicesSource.devicesList[0].sn, "3", 2, ChargerType.HOME)
      assertFalse(repository.canInsert(newDevice))
    }

  @Test
  fun verifyGetAllDevicesStreamSingleEmit() =
    runTest {
      val firstItem = repository.getAllDevicesStream().first()
      assertEquals(getDeviceListSortedBySn(), firstItem)
    }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun verifyInsertDeviceEmit() =
    runTest {
      val values = mutableListOf<List<Device>>()
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        repository.getAllDevicesStream().toList(values)
      }

      // First value; value before inserting an item
      val firstItem = values[0]
      assertEquals(getDeviceListSortedBySn(), firstItem)

      // Insert new device, which triggers a new value in the flow
      val newDevice = Device(11, "60211-A2", "2303-00008-E3", "FF:B8:37:72:4F:00", 2, ChargerType.HOME)
      repository.insertDevice(newDevice)

      // verify that a [Device] is added
      assertEquals(11, values[1].count())

      // Verify that the inserted [Device] is returned in the correct place in the list.
      val secondItem = values[1]
      val expectedDeviceList = FakeDevicesSource.devicesList.toList().toMutableList()
      expectedDeviceList.addLast(newDevice)
      assertEquals(expectedDeviceList.sortedWith(compareBy { it.sn }), secondItem)
    }

  @OptIn(ExperimentalCoroutinesApi::class)
  @Test
  fun verifyDeleteDeviceEmit() =
    runTest {
      val values = mutableListOf<List<Device>>()
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        repository.getAllDevicesStream().toList(values)
      }

      // First value; value before inserting an item
      val firstItem = values[0]
      assertEquals(getDeviceListSortedBySn(), firstItem)

      // Delete a device, which triggers a new value in the flow
      Device(11, "60211-A2", "2303-00008-E3", "FF:B8:37:72:4F:00", 2, ChargerType.HOME)
      repository.deleteDevice(FakeDevicesSource.devicesList.get(3))

      // verify that a [Device] is deleted
      assertEquals(9, values[1].count())

      // Verify that the deleted [Device] is no longer in the list
      val secondItem = values[1]
      val expectedDeviceList = FakeDevicesSource.devicesList.toList().toMutableList()
      expectedDeviceList.remove(FakeDevicesSource.devicesList.get(3))
      assertEquals(getDeviceListSortedBySn(expectedDeviceList), secondItem)
    }

  @Test
  fun verifyGetDeviceStreamDoubleEmit() =
    runTest {
      val expectedDevice = FakeDevicesSource.devicesList[FakeDevicesSource.devicesList.indexOfFirst { it.id == 5 }]
      val devices = repository.getDeviceStream(5).toList()
      assertEquals(expectedDevice, devices[0])
      assertEquals(expectedDevice, devices[1])
    }
}
