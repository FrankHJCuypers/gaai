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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Database access object to access the Gaai [GaaiDatabase] with Nexxtender Home devices.
 *
 * @author Frank HJ Cuypers
 */
@Dao
interface DeviceDao {
  /**
   * @return A Flow of all [Device]s ordered by [Device.sn] in the database.
   */
  @Query("SELECT * from devices ORDER BY sn ASC")
  fun getAllDevices(): Flow<List<Device>>

  /**
   * @param id The unique [Device.id] of the record to return.
   * @return A Flow of the [Device] matching the specified [Device. in] in the database.
   */
  @Query("SELECT * from devices WHERE id = :id")
  fun getDevice(id: Int): Flow<Device>

  /**
   * @param mac The [Device.mac] of the records to count.
   * @return A count of the [Device]s matching the specified [Device.mac] in the database.
   */
  @Query("SELECT COUNT(*) from devices WHERE mac = :mac")
  suspend fun count(mac: String): Int

  /**
   * @param pn The [Device.pn] of the records to count.
   * @param sn The [Device.sn] of the records to count.
   * @return A count of the [Device]s matching the specified [Device.pn] and [Device.sn] in the database.
   * Can only be 0 or 1, due to the unique index on pn+sn in [Device].
   */
  @Query("SELECT COUNT(*) from devices WHERE pn = :pn AND sn = :sn")
  suspend fun count(pn: String, sn: String): Int

  /**
   * Tests if a [device] can be inserted in the database.
   * @param device New device to test for insertion.
   * @return False if a [Device] with the same MAC or sn+pn as [device] already exists in the database.
   */
  @Transaction
  suspend fun canInsert(device: Device): Boolean {
    return (count(device.mac) == 0) and (count(device.pn, device.sn) == 0)
  }

  /**
   * Inserts a [device] in the database.
   *
   * The conflict strategy IGNORE is specified:
   * when the user tries to add an existing [device] into the database,
   * [Room](https://developer.android.com/training/data-storage/room) ignores the conflict.
   *
   * @param device New device to insert.
   */
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insert(device: Device)

  /**
   * Updates the specified [device] in the database.
   * @param device New record for the device.
   */
  @Update
  suspend fun update(device: Device)

  /**
   * Deletes the specified [device] from the database.
   * @param device The device to delete.
   */
  @Delete
  suspend fun delete(device: Device)
}