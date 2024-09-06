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

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.home.GaaiDeviceCard
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme

// Tag for logging
private const val TAG = "DeviceDetailsScreen"


object DeviceDetailsDestination : NavigationDestination {
    override val route = "device_detail"
    override val titleRes = R.string.device_details_title
    const val deviceIdArg = "deviceId"
    val routeWithArgs = "$route/{$deviceIdArg}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsScreen(
    // TODO: remove unused navigateBack? What is difference with onNavigateUp? Is correct one used?
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true,
    viewModel: DeviceDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    Log.d(TAG, "Entering DeviceDetailsScreen")
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            GaaiTopAppBar(
                title = stringResource(DeviceDetailsDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        }
    ) {
        innerPadding ->
        Log.d(TAG, "Before Entering DeviceDetailsBody")
        DeviceDetailsBody(
            deviceDetailsUiState = viewModel.deviceDetailsUiState,
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
fun DeviceDetailsBody(
    deviceDetailsUiState: DeviceDetailsUiState,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "Entering DeviceDetailsBody, device = ${deviceDetailsUiState.device}")

    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        GaaiDeviceCard(
            device = deviceDetailsUiState.device,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun DeviceDetailsScreenPreview() {
    GaaiTheme {
        DeviceDetailsBody(deviceDetailsUiState = DeviceDetailsUiState(
            Device(
                pn = "12345-AB", sn = "1234-56789-00", mac = "11:22:33:44:55:66", serviceDataValue = 0x17030005,
            )))
    }
}