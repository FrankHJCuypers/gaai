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
 * Repository that provides insert, update, delete and retrieve of [Device]s from a given data source.
 *
 * @author Frank HJ Cuypers
 */
interface DevicesRepository {
  /**
   * @return A Flow of all [Device]s ordered by [Device.sn] in the data source.
   */
  fun getAllDevicesStream(): Flow<List<Device>>

  /**
   * @param id The unique [Device.id] of the record to return.
   * @return A Flow of the [Device] matching the specified [Device. in] in the data source.
   */
  fun getDeviceStream(id: Int): Flow<Device?>

  /**
   * Tests if a [device] can be inserted in the database.
   * @param device New device to test for insertion.
   * @return False if a [Device] with the same MAC or sn+pn as [device] already exists in the data source.
   */
  suspend fun canInsert(device: Device): Boolean

  /**
   * Inserts a [device] in the data source.
   *
   * @param device New device to insert.
   */
  suspend fun insertDevice(device: Device)

  /**
   * Deletes the specified [device] from the data source.
   * @param device The device to delete.
   */
  suspend fun deleteDevice(device: Device)
}
