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

package be.cuypers_ghys.gaai

import android.app.Application
import android.content.Context
import android.util.Log
import be.cuypers_ghys.gaai.data.AppContainer
import be.cuypers_ghys.gaai.data.DefaultAppContainer

// Tag for logging
private const val TAG = "GaaiApplication"

class GaaiApplication : Application() {
    /** AppContainer instance used by the rest of classes to obtain dependencies */
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate Called")
        container = DefaultAppContainer( this )
        GaaiApplication.appContext = applicationContext
    }

    companion object {
        lateinit  var appContext: Context
            private set
    }
}