/*
 * Project Gaai: one app to control the Nexxtender chargers.
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
package be.cuypers_ghys.gaai.ui.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED
import androidx.core.content.ContextCompat.registerReceiver
import androidx.lifecycle.viewmodel.compose.viewModel
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState

// Tag for logging
private const val TAG = "MissingPermissions"

/**
 * Shows a system activity that allows the user to turn on Bluetooth.
 * @param context [Context] in which to start the activity.
 *
 * @author Frank HJ Cuypers
 */
@SuppressLint("MissingPermission")
private fun enableBluetooth(context: Context) {
  context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
}

/**
 * Determines if Bluetooth is enabled.
 * @param context
 * @return true if Bluetooth is enabled.
 *
 * @author Frank HJ Cuypers
 */
private fun isBluetoothEnabledState(context: Context): Boolean {
  val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
  val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
  if (bluetoothAdapter == null) {
    Log.d(TAG, "Bluetooth not supported")
  }
  val enabled = bluetoothAdapter?.isEnabled == true
  Log.d(TAG, "isBluetoothEnabledState return $enabled")
  return enabled
}

/**
 * Creates the text to present to the user when asking for permissions required by the app, but not yet granted.
 * Based on [RequestMultiplePermissionsSample](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/permissions/RequestMultiplePermissionsSample.kt)
 *
 * This function is made @Composable for easier string resource handling.
 * @param permissions List of required permission states.
 * @param shouldShowRationale Do we need to present a rationale for the permissions to the user?
 *
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun getTextToShowGivenPermissions(
  permissions: List<PermissionState>,
  shouldShowRationale: Boolean
): String {
  val revokedPermissionsSize = permissions.size
  if (revokedPermissionsSize == 0) return ""

  Log.d(TAG, "Entered getTextToShowGivenPermissions()")

  val textToShow = StringBuilder().apply {
    append(stringResource(R.string.the_space))
  }

  for (i in permissions.indices) {
    textToShow.append(permissions[i].permission)
    when {
      revokedPermissionsSize > 1 && i == revokedPermissionsSize - 2 -> {
        textToShow.append(stringResource(R.string.command_and_space))
      }

      i == revokedPermissionsSize - 1 -> {
        textToShow.append(" ")
      }

      else -> {
        textToShow.append(", ")
      }
    }
  }
  textToShow.append(
    if (revokedPermissionsSize == 1) stringResource(R.string.permission_is)
    else stringResource(R.string.permissions_are)
  )

  textToShow.append(
    if (shouldShowRationale) {
      stringResource(R.string.permissions_required)
    } else {
      stringResource(R.string.permissions_denied)
    }
  )
  Log.d(TAG, "Exiting getTextToShowGivenPermissions()")
  Log.d(TAG, "Returned text: $textToShow")
  return textToShow.toString()
}

@OptIn(ExperimentalPermissionsApi::class)
class DummyPermissionState(
  override val permission: String, override val status: PermissionStatus
) : PermissionState {
  override fun launchPermissionRequest() {
  }
}

/**
 * Implements the complete screen for handling the required Bluetooth permissions.
 * Based on [RequestMultiplePermissionsSample](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/permissions/RequestMultiplePermissionsSample.kt)
 * @param viewModel View model for the bluetooth permissions.
 * @param modifier The [Modifier] to be applied to this DeviceDetailsBody.
 * @param content The composable to execute after all bluetooth permissions are granted.
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequireBluetooth(
  modifier: Modifier = Modifier,
  viewModel: MissingPermissionsViewModel = viewModel(factory = AppViewModelProvider.Factory),
  content: @Composable () -> Unit
) {
  Log.d(TAG, "Entered RequireBluetooth()")
  Log.d(TAG, "Required permissions: ${viewModel.permissions.joinToString()}")

  // rememberMultiplePermissionsState() is a composable, so can not be called from a ViewModel.
  val multiplePermissionsState = rememberMultiplePermissionsState(
    permissions = viewModel.permissions,
  )
  val context = LocalContext.current
  val isBluetoothEnabledState = isBluetoothEnabledState(context)
  viewModel.updateUiState(isBluetoothEnabledState)

  Log.d(TAG, "permissionState permissions: ${multiplePermissionsState.permissions.joinToString()}.")
  Log.d(TAG, "permissionState revoked permissions: ${multiplePermissionsState.revokedPermissions.joinToString()}.")
  Log.d(TAG, "permissionState all permissions granted: ${multiplePermissionsState.allPermissionsGranted}.")
  Log.d(TAG, "isBluetoothEnabledState: $isBluetoothEnabledState.")

  // broadcast receiver to receive the Bluetooth enabled event.
  // TODO: Move to the ViewModel?
  val bluetoothReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
      val action = intent?.action
      if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
        val state = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
        when (state) {
          BluetoothAdapter.STATE_OFF -> {
          }

          BluetoothAdapter.STATE_TURNING_OFF -> {
          }

          BluetoothAdapter.STATE_ON -> {
            viewModel.updateUiState(isBluetoothEnabledState(context))
          }

          BluetoothAdapter.STATE_TURNING_ON -> {
          }
        }
      }
    }
  }

  // register the receiver
  val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
  registerReceiver(context, bluetoothReceiver, filter, RECEIVER_NOT_EXPORTED)

  if (multiplePermissionsState.allPermissionsGranted) {
    Log.d(TAG, "All permissions granted")
    if (viewModel.bleUiState.isBluetoothEnabledState) {
      Log.d(TAG, "Bluetooth enabled")
      context.unregisterReceiver(bluetoothReceiver)
      Log.d(TAG, "Calling content()")
      content()
      Log.d(TAG, "Called navigateToHome()")
    } else {
      // TODO: Factorize to its own Composable?
      Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = stringResource(R.string.bluetooth_disabled)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
          Log.d(TAG, "enabling Bluetooth")
          enableBluetooth(context)
          Log.d(TAG, "Updating Ui Bluetooth state")

          viewModel.updateUiState(isBluetoothEnabledState(context))
        }) {
          Text(stringResource(R.string.enable_bluetooth))
        }
      }

    }
  } else {
    // TODO: Factorize to its own Composable?
    Log.d(TAG, "Not all permissions granted")
    Column(
      modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(Modifier.weight(1f))
      Text(
        text = getTextToShowGivenPermissions(
          multiplePermissionsState.revokedPermissions,
          multiplePermissionsState.shouldShowRationale
        )
      )
      Spacer(modifier = Modifier.height(8.dp))
      Button(onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }) {
        Text(stringResource(R.string.request_permissions))
      }
      Spacer(Modifier.weight(1f))
    }
  }
  Log.d(TAG, "Exiting RequireBluetooth()")
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview(showBackground = true)
@Composable
private fun GetTextToShowGivenPermissions() {
  GaaiTheme {
    Text(
      text = getTextToShowGivenPermissions(
        listOf(
          DummyPermissionState(Manifest.permission.ACCESS_FINE_LOCATION, PermissionStatus.Denied(true)),
          DummyPermissionState(Manifest.permission.BLUETOOTH_SCAN, PermissionStatus.Denied(true)),
          DummyPermissionState(Manifest.permission.BLUETOOTH_CONNECT, PermissionStatus.Denied(true))
        ),
        true
      )
    )
  }
}