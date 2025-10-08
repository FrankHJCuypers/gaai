/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2024-2025, Frank HJ Cuypers
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

package be.cuypers_ghys.gaai.ui.permissions

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// Tag for logging
private const val TAG = "MissingPermissionsViewModel"

/**
 * ViewModel to handle required permissions, to be used by
 * [RequireBluetooth].
 *
 * @constructor Called by [AppViewModelProvider][be.cuypers_ghys.gaai.ui.AppViewModelProvider].
 *
 * @author Frank HJ Cuypers
 */
// TODO: Move to the same logic as the ScannerView from the Nordic Kotlin-BLE-Library for Bluetooth permissions,
//  using the  RequiredBluetooth & RequiredLocation composables.
//  I tried it once, but it requires the use of Hilt for dependency injection and some other Nordic stuff,
//  so I abandoned it.
class MissingPermissionsViewModel : ViewModel() {

  /**
   * Holds the current BLE ui state
   */
  // TODO: the bleUiState is tracked in the viewModel, while the isBluetoothEnabledState is tracked in
  //  the MissingPermissions screen.
  //  Can this be streamlined?
  var bleUiState by mutableStateOf(BleUiState())


  /**
   * Updates the [bleUiState] with the value provided in the argument.
   * @param isBluetoothEnabledState Is ble enabled?.
   */
  fun updateUiState(isBluetoothEnabledState: Boolean) {
    Log.d(TAG, "ENTRY updateUiState(isBluetoothEnabledState = $isBluetoothEnabledState)")
    bleUiState = bleUiState.copy(isBluetoothEnabledState = isBluetoothEnabledState)
    Log.d(TAG, "RETURN updateUiState()")
  }

  /**
   * List of required permissions.
   * See [Manifest.permission].
   */
  var permissions = emptyList<String>()

  init {
    Log.d(TAG, "ENTRY init()")
    Log.d(TAG, "SDK_INT: ${Build.VERSION.SDK_INT}")
    if (Build.VERSION.SDK_INT <= 30) {
      Log.d(TAG, "SDK <= 30")
      permissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION
      )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // 4.
      Log.d(TAG, "SDK >= 30")
      permissions = permissions.plus(
        listOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.BLUETOOTH_SCAN,
          Manifest.permission.BLUETOOTH_CONNECT,
        )
      )
    }
    Log.v(TAG, "RETURN updateUiState()")
  }
}

/**
 * Represents Ui State for BLE.
 */
data class BleUiState(
  val isBluetoothEnabledState: Boolean = false,
)
