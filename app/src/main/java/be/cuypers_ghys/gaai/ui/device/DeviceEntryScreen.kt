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
import androidx.compose.ui.tooling.preview.Wallpapers.RED_DOMINATED_EXAMPLE
import androidx.lifecycle.viewmodel.compose.viewModel
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.ChargerType
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.home.GaaiDeviceCard
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import kotlinx.coroutines.launch

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceEntryScreen(
  navigateBack: () -> Unit,
  onNavigateUp: () -> Unit,
  canNavigateUp: Boolean = true,
  viewModel: DeviceEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
  val coroutineScope = rememberCoroutineScope()
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
      deviceUiState = viewModel.deviceUiState,
      onDeviceValueChange = viewModel::updateUiState,
      // TODO: move body of onButtonClick to the [DeviceEntryViewModel]
      onButtonClick = {
        when (viewModel.deviceUiState.entryState) {
          EntryState.INPUTTING -> {}
          EntryState.ENTRY_VALID ->
            // Note: similar remark as for onSaveClick?
            coroutineScope.launch {
              viewModel.updateUiState(EntryState.SCANNING)
              viewModel.scanDevice()
            }

          EntryState.SCANNING, EntryState.DUPLICATE_DEVICE_FOUND -> {
            viewModel.updateUiState(EntryState.ENTRY_VALID)
            viewModel.cancelScanDevice()
          }

          EntryState.DEVICE_FOUND ->
            // Note: If the user rotates the screen very fast, the operation may get cancelled
            // and the device may not be saved in the Database. This is because when config
            // change occurs, the Activity will be recreated and the rememberCoroutineScope will
            // be cancelled - since the scope is bound to composition.
            coroutineScope.launch {
              viewModel.saveDevice()
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
  if ((deviceUiState.entryState == EntryState.DEVICE_FOUND)
    or (deviceUiState.entryState == EntryState.DUPLICATE_DEVICE_FOUND)
  ) {
    GaaiDeviceCard(
      device = deviceUiState.deviceDetails.toDevice(),
      ConnectionState.NOT_CONNECTED,
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
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceEntryScreenPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceEntryScreenPreviewLight")
@Preview(
  showBackground = true,
  uiMode = UI_MODE_NIGHT_NO,
  name = "DeviceEntryScreenPreviewDynamic",
  wallpaper = RED_DOMINATED_EXAMPLE
)
@Composable
private fun DeviceEntryScreenPreview() {
  GaaiTheme(dynamicColor = true) {
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
@Preview(
  showBackground = true,
  uiMode = UI_MODE_NIGHT_NO,
  name = "DeviceEntryScreenScanningPreviewDynamic",
  wallpaper = RED_DOMINATED_EXAMPLE
)
@Composable
private fun DeviceEntryScreenScanningPreview() {
  GaaiTheme(dynamicColor = true) {
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
@Preview(
  showBackground = true,
  uiMode = UI_MODE_NIGHT_NO,
  name = "DeviceEntryScreenEmptyPreviewDynamic",
  wallpaper = RED_DOMINATED_EXAMPLE
)
@Composable
private fun DeviceEntryScreenEmptyPreview() {
  GaaiTheme(dynamicColor = true) {
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
@Preview(
  showBackground = true,
  uiMode = UI_MODE_NIGHT_NO,
  name = "DeviceEntryScreenPnIncorrectPreviewDynamic",
  wallpaper = RED_DOMINATED_EXAMPLE
)
@Composable
private fun DeviceEntryScreenPnIncorrectPreview() {
  GaaiTheme(dynamicColor = true) {
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
@Preview(
  showBackground = true,
  uiMode = UI_MODE_NIGHT_NO,
  name = "DeviceEntryScreenScanCorrectPreviewDynamic",
  wallpaper = RED_DOMINATED_EXAMPLE
)
@Composable
private fun DeviceEntryScreenScanCorrectPreview() {
  GaaiTheme(dynamicColor = true) {
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
@Preview(
  showBackground = true,
  uiMode = UI_MODE_NIGHT_NO,
  name = "DeviceEntryScreenScanDuplicatePreviewDynamic",
  wallpaper = RED_DOMINATED_EXAMPLE
)
@Composable
private fun DeviceEntryScreenScanDuplicatePreview() {
  GaaiTheme(dynamicColor = true) {
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
