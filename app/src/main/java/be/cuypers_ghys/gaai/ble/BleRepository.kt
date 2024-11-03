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

package be.cuypers_ghys.gaai.ble

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner

/**
 * Repository that provides BLE scanning and GATT operations for a [Device][be.cuypers_ghys.gaai.data.Device].
 *
 * @property context The Application context.
 * @property scanner The BLE scanner object.
 * @constructor Sets up the BLE repository.
 *
 * @author Frank HJ Cuypers
 */
interface BleRepository {
  val context: Context
  val scanner: BleScanner

  /**
   * Starts scanning and emit results in the Flow.
   * Automatically stops scanning when CoroutineScope of the Flow is closed.
   */
  fun getScannerState(): Flow<BleScanResult>

  /**
   * Connects to the specified device. Device is provided using mac address.
   * Uses the Application context.
   * @param macAddress MAC address of a device.
   * @param options Connection options.
   * @return [ClientBleGatt] with initiated connection based on [options] provided.
   */
  suspend fun getClientBleGattConnection(
    macAddress: String,
    scope: CoroutineScope,
    options: BleGattConnectOptions = BleGattConnectOptions()
  ): ClientBleGatt
}
