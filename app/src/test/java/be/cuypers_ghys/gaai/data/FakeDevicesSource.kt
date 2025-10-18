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

/**
 * Fake resources for [OfflineDevicesRepositoryTest] JUnit tests.
 * @author Frank HJ Cuypers
 */
object FakeDevicesSource {
  /**
   * Initial set of [Device] records for testing.
   * This set should be as for a correct [DeviceDao], [Device] implementation, so
   * + id must be unique
   * + mac must be unique
   * + sn+pn must be unique
   */
  val devicesList = listOf(
    Device(1, "60211-A2", "2303-00005-E3", "FF:B8:37:72:4F:F8", 2, ChargerType.HOME),
    Device(2, "60207-A1", "2135-00353-5E", "FF:B8:37:72:4F:F9", 2, ChargerType.HOME),
    Device(3, "60207-A2", "2134-00349-EC", "FF:B8:37:72:4F:FA", 2, ChargerType.HOME),
    Device(4, "60211-A2", "2324-00217-48", "FF:B8:37:72:4F:FB", 2, ChargerType.MOBILE),
    Device(5, "60211-A2", "2324-00276-31", "FF:B8:37:72:4F:FC", 2, ChargerType.HOME),
    Device(6, "60207-A2", "2129-00146-0A", "FF:B8:37:72:4F:FD", 2, ChargerType.HOME),
    Device(7, "60210-A1", "2141-05750-05", "FF:B8:37:72:4F:FE", 2, ChargerType.HOME),
    Device(8, "60000-C1", "2140-00117-39", "FF:B8:37:72:4F:FF", 2, ChargerType.HOME),
    Device(9, "60211-A1", "2237-01396-95", "FF:B8:37:72:50:00", 2, ChargerType.HOME),
    Device(10, "60211-A1", "2205-00322-3C", "FF:B8:37:72:50:01", 2, ChargerType.HOME),
  )
}