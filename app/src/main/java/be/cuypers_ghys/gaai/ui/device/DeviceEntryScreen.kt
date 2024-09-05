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

package be.cuypers_ghys.gaai.ui.device

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
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import kotlinx.coroutines.launch


object DeviceEntryDestination : NavigationDestination {
    override val route = "device_entry"
    override val titleRes = R.string.device_entry_title
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceEntryScreen(
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: DeviceEntryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            GaaiTopAppBar(
                title = stringResource(DeviceEntryDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        DeviceEntryBody(
            deviceUiState = viewModel.deviceUiState,
            onDeviceValueChange = viewModel::updateUiState,
            // TODO: move body of onButtonClick to the [DeviceEntryViewModel]
            onButtonClick = {
                when(viewModel.deviceUiState.entryState) {
                    EntryState.INPUTTING -> {}
                    EntryState.ENTRY_VALID ->
                        // Note: similar remark as for onSaveClick?
                        coroutineScope.launch {
                            viewModel.updateUiState(EntryState.SCANNING)
                            viewModel.scanDevice()
                        }
                    EntryState.SCANNING -> {
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
            enabled = deviceUiState.entryState!=EntryState.INPUTTING,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text =
                when(deviceUiState.entryState) {
                    EntryState.INPUTTING ->  stringResource(R.string.scan_action)
                    EntryState.ENTRY_VALID -> stringResource(R.string.scan_action)
                    EntryState.SCANNING -> stringResource(R.string.cancel_scanning)
                    EntryState.DEVICE_FOUND -> stringResource(R.string.save_action)
                }
            )
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun DeviceDataForm(
    deviceUiState: DeviceUiState,
    modifier: Modifier = Modifier
){
    if ( deviceUiState.entryState == EntryState.DEVICE_FOUND ) {
        Text(
            text = stringResource(R.string.found_mac_colon) + deviceUiState.deviceDetails.mac,
            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
        )//"0x"+device.serviceDataValue.toHexString()
        Text(
            text = stringResource(R.string.found_servicedata_colon) + "0x" + deviceUiState.deviceDetails.serviceDataValue.toHexString(),
            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
        )
    }
}

@Suppress("SpellCheckingInspection"
)
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
            supportingText = { if (!deviceUiState.isPnValid) {Text(text = "Required format: AAAAA-RR", color = MaterialTheme.colorScheme.error) }},
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
            supportingText = { if (!deviceUiState.isSnValid) {Text(text = "Required format: YYMM-NNNNN-UU", color = MaterialTheme.colorScheme.error) }},
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

@Preview(showBackground = true)
@Composable
private fun DeviceEntryScreenPreview() {
    GaaiTheme {
        DeviceEntryBody(deviceUiState = DeviceUiState(
            DeviceDetails(
                pn = "60211-A2", sn = "2303-00005-E3"
            ), entryState = EntryState.ENTRY_VALID, isSnValid = true, isPnValid = true
        ), onDeviceValueChange = {}, onButtonClick = {})
    }
}
@Preview(showBackground = true)
@Composable
private fun DeviceEntryScreenEmptyPreview() {
    GaaiTheme {
        DeviceEntryBody(deviceUiState = DeviceUiState(
            DeviceDetails(
                pn = "", sn = ""
            ), entryState = EntryState.ENTRY_VALID, isSnValid = true, isPnValid = true
        ), onDeviceValueChange = {}, onButtonClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceEntryScreenPnIncorrectPreview() {
    GaaiTheme {
        DeviceEntryBody(deviceUiState = DeviceUiState(
            DeviceDetails(
                pn = "12-34", sn = "1234-56789-00"
            ), entryState= EntryState.INPUTTING, isSnValid = true, isPnValid = false
        ), onDeviceValueChange = {}, onButtonClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceEntryScreenScanCorrectPreview() {
    GaaiTheme {
        DeviceEntryBody(deviceUiState = DeviceUiState(
            DeviceDetails(
                pn = "12-34", sn = "1234-56789-00", mac = "11:22:33:44:55:66", serviceDataValue = 0x17030005
            ), entryState= EntryState.DEVICE_FOUND, isSnValid = true, isPnValid = false
        ), onDeviceValueChange = {}, onButtonClick = {})
    }
}
