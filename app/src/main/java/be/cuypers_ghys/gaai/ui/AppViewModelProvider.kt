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

package be.cuypers_ghys.gaai.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import be.cuypers_ghys.gaai.GaaiApplication
import be.cuypers_ghys.gaai.ui.device.DeviceEntryViewModel
import be.cuypers_ghys.gaai.ui.home.HomeViewModel

// Tag for logging
private const val TAG = "AppViewModelProvider"

/**
 * Provides Factory to create instance of ViewModel for the entire Gaai app
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {

        // Initializer for DeviceEntryViewModel
        initializer {
            DeviceEntryViewModel(gaaiApplication().container.devicesRepository)
        }

        // Initializer for HomeViewModel
        initializer {
            Log.d(TAG, "Initialize HomeViewModel")
            val gaaiApplication = gaaiApplication()
            Log.d(TAG, "GaaiApplication: $gaaiApplication")
            HomeViewModel(gaaiApplication().container.devicesRepository)
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [GaaiApplication].
 */
fun CreationExtras.gaaiApplication(): GaaiApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as GaaiApplication)
