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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Gaai Database class with a singleton Instance object for handling the Nexxtender Home BLE devices.
 *
 * See [Save data in a local database using Room](https://developer.android.com/training/data-storage/room).
 *
 * @author Frank HJ Cuypers
 */
@Database(entities = [Device::class], version = 2, exportSchema = false)
abstract class GaaiDatabase : RoomDatabase() {

  /**
   * @return The Data Access Object (DAO) for accessing the [Device] table in the database.
   */
  abstract fun deviceDao(): DeviceDao

  companion object {
    @Volatile
    private var Instance: GaaiDatabase? = null

    /**
     * @param context
     * @return The singleton [GaaiDatabase]
     */
    fun getDatabase(context: Context): GaaiDatabase {
      // if the Instance is not null, return it, otherwise create a new database instance.
      return Instance ?: synchronized(this) {
        Room.databaseBuilder(context, GaaiDatabase::class.java, "device_database")
          /*
           * Setting this option in your app's database builder means that Room
           * permanently deletes all data from the tables in your database when it
           * attempts to perform a migration with no defined migration path.
           */
          .fallbackToDestructiveMigration()
          .build()
          .also { Instance = it }
      }
    }
  }
}
