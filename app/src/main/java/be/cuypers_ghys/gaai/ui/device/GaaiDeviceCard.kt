/*
* Project Gaai: one app to control the Nexxtender chargers.
* Copyright © 2024-2026, Frank HJ Cuypers
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

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.ChargerType
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus

// Tag for logging
private const val TAG = "GaaiDeviceCard"

/**
 * Implements a [Card] displaying the details of the [device].
 * @param device The [Device] to display.
 * @param connectionState The connection state of the device
 * @param modifier The [Modifier] to be applied to this [GaaiDeviceCard]
 *
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalStdlibApi::class)
@Composable
internal fun GaaiDeviceCard(
  device: Device, isAdvertising: Boolean, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiDeviceCard(device = $device)")

  GaaiDeviceCardCommon(device, modifier){
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = device.type.toString(),
        style = MaterialTheme.typography.titleMedium,
      )
      Spacer(Modifier.weight(1f))
      Icon(
        painter = isAvailableToPainter(isAdvertising),
        contentDescription = stringResource(id = R.string.ev_charger_content_desc),
        modifier = Modifier.size(MaterialTheme.typography.titleMedium.fontSize.value.dp)
      )
      Text(
        text = isAvailableToText(isAdvertising),
        style = MaterialTheme.typography.titleMedium,
      )
    }
  }
  Log.v(TAG, "RETURN GaaiDeviceCard())")
}

/**
 * Implements a [Card] displaying the details of the [device].
 * @param device The [Device] to display.
 * @param gattConnectionStateWithStatus The connection state of the device
 * @param modifier The [Modifier] to be applied to this [GaaiDeviceCard]
 *
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalStdlibApi::class)
@Composable
internal fun GaaiDeviceCardDeviceDetails(
  device: Device, gattConnectionStateWithStatus: GattConnectionStateWithStatus, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiDeviceCardDeviceDetails(device = $device)")

  GaaiDeviceCardCommon(device, modifier){
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Text(
        text = device.type.toString(),
        style = MaterialTheme.typography.titleMedium,
      )
      Spacer(Modifier.weight(1f))
      Icon(
        painter = gattConnectionStateToPainter(gattConnectionStateWithStatus.state),
        contentDescription = stringResource(id = R.string.ev_charger_content_desc),
        modifier = Modifier.size(MaterialTheme.typography.titleMedium.fontSize.value.dp)
      )
      Text(
        text = gattConnectionStateToText(gattConnectionStateWithStatus.state),
        style = MaterialTheme.typography.titleMedium,
      )
    }
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth(),
    ) {
      Spacer(Modifier.weight(1f))
      Icon(
        painter = bleGattConnectionStatusToPainter(gattConnectionStateWithStatus.status),
        contentDescription = stringResource(id = R.string.ev_charger_content_desc),
        modifier = Modifier.size(MaterialTheme.typography.titleMedium.fontSize.value.dp)
      )
      Text(
        text = bleGattConnectionStatusToText(gattConnectionStateWithStatus.status),
        style = MaterialTheme.typography.titleMedium,
      )
    }
  }
  Log.v(TAG, "RETURN GaaiDeviceCardDeviceDetails())")
}

/**
 * Implements a [Card] displaying the details of the [device].
 * @param device The [Device] to display.
 * @param connectionState The connection state of the device
 * @param modifier The [Modifier] to be applied to this [GaaiDeviceCard]
 *
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalStdlibApi::class)
@Composable
internal fun GaaiDeviceCardCommon(
  device: Device, modifier: Modifier = Modifier, gaaiDeviceCardHomeEntryDetail: @Composable () -> Unit
) {
  Log.d(TAG, "ENTRY GaaiDeviceCardCommon(device = $device)")
  Card(
    modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically
    ) {
      val newPainter = when (device.type) {
        ChargerType.MOBILE -> painterResource(R.drawable.ic_cable_mobile)
        ChargerType.HOME -> painterResource(R.drawable.ic_cable_home)
        else -> painterResource(R.drawable.rounded_ev_charger_24)
      }

      Icon(
        painter = newPainter,
        contentDescription = stringResource(id = R.string.ev_charger_content_desc)
      )

      Spacer(modifier = Modifier.width(16.dp))

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        gaaiDeviceCardHomeEntryDetail()
        Row(
          modifier = Modifier.fillMaxWidth()
        ) {
          Log.v(TAG, "GaaiDeviceCard printing first line")

          Text(
            text = device.pn,
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = device.sn,
            style = MaterialTheme.typography.titleMedium
          )

        }
        Row(
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = device.mac,
            style = MaterialTheme.typography.bodyMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = "0x" + device.serviceDataValue.toHexString(),
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }
    }
    Log.v(TAG, "RETURN GaaiDeviceCardCommon())")
  }
}

/**
 * Converts [isAvailable] to a string value to display.
 * @param  * @param isAvailable
 *
 * @return The corresponding string.
 *
 * @author Frank HJ Cuypers
 */
@Composable
private fun isAvailableToText(isAvailable: Boolean) = if (isAvailable)
  stringResource(R.string.available) else stringResource(R.string.notAvailable)

/**
 * Converts [isAvailable] to a corresponding icon to display.
 * @param isAvailable
 * @return The corresponding icon.
 *
 * @author Frank HJ Cuypers
 */
@Composable
private fun isAvailableToPainter(isAvailable: Boolean ) = if (isAvailable) painterResource(R.drawable.bluetooth_searching_24px)
else painterResource(R.drawable.bluetooth_disabled_24px)


/**
 * Converts [bleGattConnectionStatus] to a string value to display.
 * @param  * @param bleGattConnectionStatus
 *
 * @return The corresponding string.
 *
 * @author Frank HJ Cuypers
 */
@Composable
private fun bleGattConnectionStatusToText(bleGattConnectionStatus: BleGattConnectionStatus) = when (bleGattConnectionStatus) {
  BleGattConnectionStatus.SUCCESS -> stringResource(R.string.ble_gatt_connection_status_success)
  BleGattConnectionStatus.TERMINATE_LOCAL_HOST -> stringResource(R.string.ble_gatt_connection_status_terminal_local_host)
  BleGattConnectionStatus.TERMINATE_PEER_USER -> stringResource(R.string.ble_gatt_connection_status_terminal_peer_user)
  BleGattConnectionStatus.LINK_LOSS -> stringResource(R.string.ble_gatt_connection_status_link_loss)
  BleGattConnectionStatus.NOT_SUPPORTED -> stringResource(R.string.ble_gatt_connection_status_not_supported)
  BleGattConnectionStatus.CANCELLED -> stringResource(R.string.ble_gatt_connection_status_timeout)
  BleGattConnectionStatus.TIMEOUT -> stringResource(R.string.ble_gatt_connection_status_cancelled)
  else -> {
    stringResource(R.string.ble_gatt_connection_status_unknown)
  }
}

/**
 * Converts [bleGattConnectionStatus] to a corresponding icon to display.
 * @param bleGattConnectionStatus
 * @return The corresponding icon.
 *
 * @author Frank HJ Cuypers
 */
@Composable
private fun bleGattConnectionStatusToPainter(bleGattConnectionStatus: BleGattConnectionStatus) = when (bleGattConnectionStatus) {
  BleGattConnectionStatus.SUCCESS -> painterResource(R.drawable.bluetooth_connected_24px)
  else -> {
    painterResource(R.drawable.bluetooth_disabled_24px)
  }
}

/**
 * Converts [gattConnectionState] a string value to display.
 * @param gattConnectionState
 * @return The corresponding string.
 *
 * @author Frank HJ Cuypers
 */
@Composable
private fun gattConnectionStateToText(gattConnectionState: GattConnectionState) = when (gattConnectionState) {
  GattConnectionState.STATE_CONNECTED -> stringResource(R.string.gatt_connection_state_connected)
  GattConnectionState.STATE_DISCONNECTED -> stringResource(R.string.gatt_connection_state_disconnected)
  GattConnectionState.STATE_CONNECTING -> stringResource(R.string.gatt_connection_state_connecting)
  GattConnectionState.STATE_DISCONNECTING ->stringResource(R.string.gatt_connection_state_disconnecting)
}

/**
 * Converts [gattConnectionState] to a corresponding icon to display.
 * @param gattConnectionState
 * @return The corresponding icon.
 *
 * @author Frank HJ Cuypers
 */
@Composable
private fun gattConnectionStateToPainter(gattConnectionState: GattConnectionState) = when (gattConnectionState) {
  GattConnectionState.STATE_CONNECTED -> painterResource(R.drawable.bluetooth_connected_24px)
  GattConnectionState.STATE_DISCONNECTED -> painterResource(R.drawable.bluetooth_disabled_24px)
  GattConnectionState.STATE_CONNECTING -> painterResource(R.drawable.bluetooth_searching_24px)
  GattConnectionState.STATE_DISCONNECTING -> painterResource(R.drawable.bluetooth_searching_24px)
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DevicePreviewHOMEDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DevicePreviewHOMELight")
@Composable
fun DevicePreviewHOME() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiDeviceCard(
        Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, ChargerType.HOME),
        false
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DevicePreviewMOBILEDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DevicePreviewMOBILELight")
@Composable
fun DevicePreviewMOBILE() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiDeviceCard(
        Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, ChargerType.MOBILE),
        false
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DevicePreviewUNKNOWNDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DevicePreviewUNKNOWNLight")
@Composable
fun DevicePreviewUNKNOWN() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiDeviceCard(
        Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, ChargerType.UNKNOWN),
        false
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiDeviceCardDeviceDetailsPreviewStateDisconnectedDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiDeviceCardDeviceDetailsPreviewStateDisconnectedLight")
@Composable
fun GaaiDeviceCardDeviceDetailsPreviewStateDisconnectedSuccess() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiDeviceCardDeviceDetails(
        Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, ChargerType.HOME),
        GattConnectionStateWithStatus(GattConnectionState.STATE_DISCONNECTED, BleGattConnectionStatus.SUCCESS)
      )
    }
  }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiDeviceCardDeviceDetailsPreviewStateConnectedSuccessDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiDeviceCardDeviceDetailsPreviewStateConnectedSuccessLight")
@Composable
fun GaaiDeviceCardDeviceDetailsPreviewStateConnectedTerminalLocalHost() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiDeviceCardDeviceDetails(
        Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, ChargerType.HOME),
        GattConnectionStateWithStatus(GattConnectionState.STATE_CONNECTED, BleGattConnectionStatus.TERMINATE_LOCAL_HOST)
      )
    }
  }
}
