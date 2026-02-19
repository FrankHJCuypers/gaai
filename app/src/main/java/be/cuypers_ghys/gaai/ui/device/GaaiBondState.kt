/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2026, Frank HJ Cuypers
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

package be.cuypers_ghys.gaai.ui.device

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import be.cuypers_ghys.gaai.data.Device
import no.nordicsemi.android.kotlin.ble.core.data.BondState

// Tag for logging
private const val TAG = "GaaiBondState"

object GaaiBondState {
  /**
   * @param gaaiDevice
   * @return The [BondState] of the [gaaiDevice]
   */
  @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
  fun getBondState(context: Context, gaaiDevice: Device): BondState {
    val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    val bondedDevices = bluetoothAdapter.bondedDevices
    val foundDevice = bondedDevices.find { it.address == gaaiDevice.mac }
    return if (foundDevice != null) BondState.BONDED else BondState.NONE
  }
}