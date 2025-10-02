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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * @author Frank HJ Cuypers
 */
class DeviceTest {

  @Test
  fun createTestDefault() {
    val device = Device()
    assertNotNull(device)

    assertEquals(0, device.id)
    assertEquals("", device.pn)
    assertEquals("", device.sn)
    assertEquals("", device.mac)
    assertEquals(0, device.serviceDataValue)
    assertEquals(ChargerType.HOME, device.type)
  }

  @Test
  fun createTest() {
    val device = Device(1, "pn", "sn", "mac", 2, ChargerType.UNKNOWN)
    assertNotNull(device)

    assertEquals(1, device.id)
    assertEquals("pn", device.pn)
    assertEquals("sn", device.sn)
    assertEquals("mac", device.mac)
    assertEquals(2, device.serviceDataValue)
    assertEquals(ChargerType.UNKNOWN, device.type)
  }
}