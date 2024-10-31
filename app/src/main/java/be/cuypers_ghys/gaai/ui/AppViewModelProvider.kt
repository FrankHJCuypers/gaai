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
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import be.cuypers_ghys.gaai.GaaiApplication
import be.cuypers_ghys.gaai.ui.device.DeviceDetailsViewModel
import be.cuypers_ghys.gaai.ui.device.DeviceEntryViewModel
import be.cuypers_ghys.gaai.ui.home.HomeViewModel
import be.cuypers_ghys.gaai.ui.permissions.MissingPermissionsViewModel

// Tag for logging
@Suppress("unused")
private const val TAG = "AppViewModelProvider"

/**
 * Provides Factory to create instance of ViewModel for the entire Gaai app
 */
object AppViewModelProvider {
  val Factory = viewModelFactory {

    // Initializer for DeviceEntryViewModel
    initializer {
      DeviceEntryViewModel(gaaiApplication().container.devicesRepository, gaaiApplication().container.bleRepository)
    }

    // Initializer for DeviceDetailsViewModel
    initializer {
      DeviceDetailsViewModel(
        this.createSavedStateHandle(),
        gaaiApplication().container.devicesRepository,
        gaaiApplication().container.bleRepository
      )
    }

    // Initializer for HomeViewModel
    initializer {
      HomeViewModel(gaaiApplication().container.devicesRepository)
    }

    // Initializer for MissingPermissionsViewModel
    initializer {
      MissingPermissionsViewModel()
    }
  }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [GaaiApplication].
 */
fun CreationExtras.gaaiApplication(): GaaiApplication =
  (this[AndroidViewModelFactory.APPLICATION_KEY] as GaaiApplication)
