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

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner

/**
 * Local devices database
 */
class NordicBleRepository(override val context: Context, override val scanner: BleScanner) : BleRepository {
  @SuppressLint("MissingPermission")
  override fun getScannerState() = scanner.scan()

  @SuppressLint("MissingPermission")
  override suspend fun getClientBleGattConnection(
    macAddress: String,
    scope: CoroutineScope,
    options: BleGattConnectOptions
  ): ClientBleGatt {
    return ClientBleGatt.connect(context, macAddress, scope, options)
  }
}
