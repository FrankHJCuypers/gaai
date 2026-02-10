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

package be.cuypers_ghys.gaai.ui.device

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.ChargerType
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.home.GaaiDeviceCard
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.permissions.RequireBluetooth
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme

// Tag for logging
private const val TAG = "DeviceEntryScreen"

/**
 * The [NavigationDestination] information for the [DeviceEntryScreen].
 *
 * @author Frank HJ Cuypers
 */
object DeviceEntryDestination : NavigationDestination {
  override val route = "device_entry"
  override val titleRes = R.string.device_entry_title
}


/**
 * Implements the complete screen for entering the information for a new Nexxtender charger device,
 * and make a BLE connection to it.
 * The screen includes app bars.
 * @param navigateBack Function to be called when [DeviceEntryScreen] wants to navigate back.
 * @param onNavigateUp Function to be called when [DeviceEntryScreen] wants to navigate up.
 * @param canNavigateUp Is the [DeviceEntryScreen] allowed to navigate back?
 * @param viewModel The [DeviceEntryViewModel] to be associated with this [DeviceEntryScreen].
 *
 * @author Frank HJ Cuypers
 */

@Composable
fun DeviceEntryScreen(
  navigateBack: () -> Unit,
  onNavigateUp: () -> Unit,
  canNavigateUp: Boolean = true,
  viewModel: DeviceEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
  Log.v(TAG, "ENTRY DeviceEntryScreen()")

  RequireBluetooth {
    DeviceEntryScreenNoViewModel(
      navigateBack, onNavigateUp, canNavigateUp, viewModel.deviceUiState,
      onDeviceValueChange = viewModel::updateUiState,
      onEntryStatusChange = viewModel::updateUiState,
      scanDevice = viewModel::scanDevice,
      cancelScanDevice = viewModel::cancelScanDevice,
      saveDevice = viewModel::saveDevice
    )
  }
  Log.v(TAG, "RETURN DeviceEntryScreen()")
}

/**
 * Implements the complete screen for entering the information for a new Nexxtender charger device,
 * and make a BLE connection to it.
 * The screen includes app bars.
 *
 * Version that does not access the ViewModel directly, so that a @Preview works.
 *
 * @param navigateBack Function to be called when [DeviceEntryScreen] wants to navigate back.
 * @param onNavigateUp Function to be called when [DeviceEntryScreen] wants to navigate up.
 * @param canNavigateUp Is the [DeviceEntryScreen] allowed to navigate back?
 * @param deviceUiState The device state determined by the [DeviceEntryViewModel].
 * @param onDeviceValueChange Function to be called when any of the values in the entry screen changes.
 * @param onEntryStatusChange Function to called when teh entryStatus changed.
 * @param scanDevice Function called to start a BLE scan for the specified device.
 * @param cancelScanDevice Function called to stop the BLE scan.
 * @param saveDevice Inserts the found device in the database.
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceEntryScreenNoViewModel(
  navigateBack: () -> Unit,
  onNavigateUp: () -> Unit,
  canNavigateUp: Boolean = true,
  deviceUiState: DeviceUiState,
  onDeviceValueChange: (DeviceDetails) -> Unit,
  onEntryStatusChange: (EntryState) -> Unit,
  scanDevice: () -> Unit,
  cancelScanDevice: () -> Unit,
  saveDevice: () -> Unit
) {
  Log.v(TAG, "ENTRY DeviceEntryScreenNoViewModel()")

  rememberCoroutineScope()
  Scaffold(
    topBar = {
      GaaiTopAppBar(
        title = stringResource(DeviceEntryDestination.titleRes),
        canNavigateUp = canNavigateUp,
        navigateUp = onNavigateUp
      )
    }
  ) { innerPadding ->
    DeviceEntryBody(
      deviceUiState = deviceUiState,
      onDeviceValueChange = onDeviceValueChange,
      // TODO: move body of onButtonClick to the [DeviceEntryViewModel]
      onButtonClick = {
        when (deviceUiState.entryState) {
          EntryState.INPUTTING -> {}
          EntryState.ENTRY_VALID ->
            // Note: similar remark as for onSaveClick?
          {
            onEntryStatusChange(EntryState.SCANNING)
            scanDevice()
          }

          EntryState.SCANNING, EntryState.DUPLICATE_DEVICE_FOUND -> {
            onEntryStatusChange(EntryState.ENTRY_VALID)
            cancelScanDevice()
          }

          EntryState.DEVICE_FOUND ->
            // Note: If the user rotates the screen very fast, the operation may get cancelled
            // and the device may not be saved in the Database. This is because when config
            // change occurs, the Activity will be recreated and the rememberCoroutineScope will
            // be cancelled - since the scope is bound to composition.
          {
            saveDevice()
            navigateBack()
          }
        }

      },
      modifier = Modifier
        .padding(
          start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
          top = innerPadding.calculateTopPadding(),
          end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
        )
        .verticalScroll(rememberScrollState())
        .fillMaxWidth()
    )
  }
  Log.v(TAG, "RETURN DeviceEntryScreenNoViewModel()")
}

/**
 * Implements the body of the screen for entering the information for a new Nexxtender charger device,
 * and make a BLE connection to it.
 * @param deviceUiState The device state determined by the [DeviceEntryViewModel].
 * @param onDeviceValueChange Function to execute when any of the values in the entry screen changes value.
 * @param onButtonClick Function to execute when the user clicks the button on the screen.
 * Depending on the internal state, the button has a different action.
 * @param modifier The [Modifier] to be applied to this DeviceDetailsBody
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun DeviceEntryBody(
  deviceUiState: DeviceUiState,
  onDeviceValueChange: (DeviceDetails) -> Unit,
  onButtonClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY DeviceEntryBody(deviceUiState=$deviceUiState)")

  Column(
    modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
  ) {
    DeviceInputForm(
      deviceUiState = deviceUiState,
      onValueChange = onDeviceValueChange,
      modifier = Modifier.fillMaxWidth()
    )

    DeviceDataForm(
      deviceUiState = deviceUiState,
      modifier = Modifier.fillMaxWidth()
    )

    Button(
      onClick = onButtonClick,
      enabled = deviceUiState.entryState != EntryState.INPUTTING,
      shape = MaterialTheme.shapes.small,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text =
          when (deviceUiState.entryState) {
            EntryState.INPUTTING -> stringResource(R.string.scan_action)
            EntryState.ENTRY_VALID -> stringResource(R.string.scan_action)
            EntryState.SCANNING -> stringResource(R.string.cancel_scanning)
            EntryState.DEVICE_FOUND -> stringResource(R.string.save_action)
            EntryState.DUPLICATE_DEVICE_FOUND -> stringResource(R.string.cancel_scanning)
          }
      )
    }
  }
  Log.v(TAG, "RETURN DeviceEntryBody()")
}

/**
 * Displays the [GaaiDeviceCard] when the new device is found, or if it is a duplicate,
 * @param deviceUiState The device state determined by the [DeviceEntryViewModel].
 * @param modifier The [Modifier] to be applied to this DeviceDetailsBody
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun DeviceDataForm(
  deviceUiState: DeviceUiState,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY DeviceDataForm(deviceUiState=$deviceUiState)")

  if ((deviceUiState.entryState == EntryState.DEVICE_FOUND)
    or (deviceUiState.entryState == EntryState.DUPLICATE_DEVICE_FOUND)
  ) {
    GaaiDeviceCard(
      device = deviceUiState.deviceDetails.toDevice(),
      ConnectionState.AVAILABLE,
      modifier = Modifier
        .padding(dimensionResource(id = R.dimen.padding_small))
    )
    if (deviceUiState.entryState == EntryState.DUPLICATE_DEVICE_FOUND) {
      Text(
        text = stringResource(R.string.duplicate),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
      )
    }
  }
  Log.v(TAG, "RETURN DeviceDataForm()")
}

/**
 * Input form for entering the SN and PN of a new Nexxtender charger device,.
 * @param deviceUiState The device state determined by the [DeviceEntryViewModel].
 * @param modifier The [Modifier] to be applied to this DeviceDetailsBody
 * @param onValueChange Function to execute when any of the values in the entry screen changes value.
 * @param enabled controls the enabled state of the SN and PN input fields.
 *  When false, they will not respond to user input,
 *  and will appear visually disabled and disabled to accessibility services.
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun DeviceInputForm(
  deviceUiState: DeviceUiState,
  modifier: Modifier = Modifier,
  onValueChange: (DeviceDetails) -> Unit = {},
  enabled: Boolean = true
) {
  Log.d(TAG, "ENTRY DeviceInputForm(deviceUiState=$deviceUiState)")

  val deviceDetails = deviceUiState.deviceDetails
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
  ) {
    OutlinedTextField(
      value = deviceDetails.pn,
      onValueChange = {
        onValueChange(deviceDetails.copy(pn = it))
      },
      label = { Text(stringResource(R.string.device_pn_req)) },
      placeholder = { Text(text = "AAAAA-RR") },
      isError = !deviceUiState.isPnValid,
      supportingText = {
        if (!deviceUiState.isPnValid) {
          Text(text = "Required format: AAAAA-RR", color = MaterialTheme.colorScheme.error)
        }
      },
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
      ),
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      singleLine = true,
    )
    OutlinedTextField(
      value = deviceDetails.sn,
      onValueChange = {
        onValueChange(deviceDetails.copy(sn = it))
      },
      label = { Text(stringResource(R.string.device_sn_req)) },
      placeholder = { Text(text = "YYMM-NNNNN-UU") },
      isError = !deviceUiState.isSnValid,
      supportingText = {
        if (!deviceUiState.isSnValid) {
          Text(text = "Required format: YYMM-NNNNN-UU", color = MaterialTheme.colorScheme.error)
        }
      },
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
      ),
      modifier = Modifier.fillMaxWidth(),
      enabled = enabled,
      singleLine = true
    )
    if (enabled) {
      Text(
        text = stringResource(R.string.required_fields),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
      )
    }
  }
  Log.v(TAG, "RETURN DeviceInputForm()")
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceEntryScreenNoViewModelDeviceDetailsEmptyDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceEntryScreenNoViewModelDeviceDetailsEmptyLight")
@Composable
private fun DeviceEntryScreenNoViewModelDeviceDetailsEmptyPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DeviceEntryScreenNoViewModel(
        navigateBack = {}, onNavigateUp = {}, true,
        deviceUiState = DeviceUiState(
          DeviceDetails(
            pn = "", sn = ""
          ),
          entryState = EntryState.INPUTTING, isSnValid = false, isPnValid = false
        ),
        onDeviceValueChange = {},
        onEntryStatusChange = {},
        scanDevice = {},
        cancelScanDevice = {},
        saveDevice = {},
      )
    }
  }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceEntryScreenNoViewModelDeviceDetailsValidDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceEntryScreenNoViewModelPreviewLight")
@Composable
private fun DeviceEntryScreenNoViewModelDeviceDetailsValidPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DeviceEntryScreenNoViewModel(
        navigateBack = {}, onNavigateUp = {}, true,
        deviceUiState = DeviceUiState(
          DeviceDetails(
            pn = "12345-A2", sn = "6789-12345-E3"
          ),
          entryState = EntryState.ENTRY_VALID, isSnValid = true, isPnValid = true
        ),
        onDeviceValueChange = {},
        onEntryStatusChange = {},
        scanDevice = {},
        cancelScanDevice = {},
        saveDevice = {},
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceEntryScreenPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceEntryScreenPreviewLight")
@Composable
private fun DeviceEntryScreenPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DeviceEntryBody(
        deviceUiState = DeviceUiState(
          DeviceDetails(
            pn = "12345-A2", sn = "6789-12345-E3"
          ), entryState = EntryState.ENTRY_VALID, isSnValid = true, isPnValid = true
        ), onDeviceValueChange = {}, onButtonClick = {})
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceEntryScreenScanningPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceEntryScreenScanningPreviewLight")
@Composable
private fun DeviceEntryScreenScanningPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DeviceEntryBody(
        deviceUiState = DeviceUiState(
          DeviceDetails(
            pn = "12345-A2", sn = "6789-12345-E3"
          ), entryState = EntryState.SCANNING, isSnValid = true, isPnValid = true
        ), onDeviceValueChange = {}, onButtonClick = {})
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceEntryScreenEmptyPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceEntryScreenEmptyPreviewLight")
@Composable
private fun DeviceEntryScreenEmptyPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DeviceEntryBody(
        deviceUiState = DeviceUiState(
          DeviceDetails(
            pn = "", sn = ""
          ), entryState = EntryState.INPUTTING, isSnValid = false, isPnValid = false
        ), onDeviceValueChange = {}, onButtonClick = {})
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceEntryScreenPnIncorrectPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceEntryScreenPnIncorrectPreviewLight")
@Composable
private fun DeviceEntryScreenPnIncorrectPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DeviceEntryBody(
        deviceUiState = DeviceUiState(
          DeviceDetails(
            pn = "12-34", sn = "1234-56789-00"
          ), entryState = EntryState.INPUTTING, isSnValid = true, isPnValid = false
        ), onDeviceValueChange = {}, onButtonClick = {})
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceEntryScreenScanCorrectPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceEntryScreenScanCorrectPreviewLight")
@Composable
private fun DeviceEntryScreenScanCorrectPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DeviceEntryBody(
        deviceUiState = DeviceUiState(
          DeviceDetails(
            pn = "12345-A2",
            sn = "6789-12345-E3",
            mac = "FA:CA:DE:12:34:56",
            serviceDataValue = 0x12345678,
            type = ChargerType.HOME
          ), entryState = EntryState.DEVICE_FOUND, isSnValid = true, isPnValid = true
        ), onDeviceValueChange = {}, onButtonClick = {})
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceEntryScreenScanDuplicatePreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceEntryScreenScanDuplicatePreviewLight")
@Composable
private fun DeviceEntryScreenScanDuplicatePreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DeviceEntryBody(
        deviceUiState = DeviceUiState(
          DeviceDetails(
            pn = "12345-A2", sn = "6789-12345-E3", mac = "FA:CA:DE:12:34:56", serviceDataValue = 0x12345678,
            type = ChargerType.HOME
          ), entryState = EntryState.DUPLICATE_DEVICE_FOUND, isSnValid = true, isPnValid = true
        ), onDeviceValueChange = {}, onButtonClick = {})
    }
  }
}
