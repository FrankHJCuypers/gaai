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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.ChargingBasicData
import be.cuypers_ghys.gaai.data.ChargingCarData
import be.cuypers_ghys.gaai.data.ChargingGridData
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.Discriminator
import be.cuypers_ghys.gaai.data.Status
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.home.GaaiDeviceCard
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.util.Timestamp

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
                navigateUp = {viewModel.navigateBack(onNavigateUp)}
            )
        }
    ) {
        innerPadding ->
        Log.d(TAG, "Before Entering DeviceDetailsBody")

        val state by viewModel.state.collectAsStateWithLifecycle()
        val device by viewModel.device.collectAsStateWithLifecycle()

        DeviceDetailsBody(
            device,
            state,
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
    device : Device?,
    state: DeviceDetailsViewState,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "Entering DeviceDetailsBody, device = $device")

    Column(
        modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
    ) {
        device?.let {
            GaaiDeviceCard(
                device = it,
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
            )
        }

        GaaiDeviceNameCard(
            deviceName=state.deviceName,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
        )

        GaaiDeviceInformationCard(
            deviceInformation=state.deviceInformation,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
        )

        GaaiChargingBasicDataCard(
            chargingBasicData=state.chargingBasicData,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
        )

        GaaiChargingGridDataCard(
            chargingGridData=state.chargingGridData,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
        )

        GaaiChargingCarDataCard(
            chargingCarData=state.chargingCarData,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
        )

    }
}

@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiDeviceNameCard(
    deviceName: String, modifier: Modifier = Modifier
) {
    Log.d(TAG, "Entered GaaiNameCard with deviceName = $deviceName")
    Card(
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_ev_charger_24),
                contentDescription = stringResource(id = R.string.ev_charger_content_desc)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.device_name),
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Log.d(TAG, "exiting GaaiNameCard with deviceName = $deviceName")
    }
}

@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiDeviceInformationCard(
    deviceInformation: DeviceInformation, modifier: Modifier = Modifier
) {
    Log.d(TAG, "Entered GaaiDeviceInformationCard with deviceInformation = $deviceInformation")
    Card(
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row (
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.model_number),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = deviceInformation.modelNumber,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.serial_number),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = deviceInformation.serialNumber,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.firmware_revision),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = deviceInformation.firmwareRevision,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.hardware_revision),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = deviceInformation.hardwareRevision,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        Log.d(TAG, "exiting GaaiDeviceInformationCard with deviceInformation = $deviceInformation")
    }
}

@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiChargingBasicDataCard(
    chargingBasicData: ChargingBasicData, modifier: Modifier = Modifier
) {
    Log.d(TAG, "Entered GaaiChargingBasicDataCard with chargingBasicData = $chargingBasicData")
    Card(
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row (
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.seconds),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingBasicData.seconds.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.discriminator),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingBasicData.discriminator.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.status),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingBasicData.status.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.energy),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingBasicData.energy.toInt().toString()+" Wh",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.phasecount),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingBasicData.phaseCount.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        Log.d(TAG, "exiting GaaiChargingBasicDataCard with chargingBasicData = $chargingBasicData")
    }
}

@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiChargingGridDataCard(
    chargingGridData: ChargingGridData, modifier: Modifier = Modifier
) {
    Log.d(TAG, "Entered GaaiChargingGridDataCard with chargingGridData = $chargingGridData")
    Card(
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row (
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.timestamp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(text= Timestamp.toString(chargingGridData.timestamp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.l1),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingGridData.l1.toString()+" dA",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.l2),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingGridData.l2.toString()+" dA",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.l3),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingGridData.l3.toString()+" dA",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.consumed),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingGridData.consumed.toString()+" W",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.interval),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingGridData.interval.toInt().toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        Log.d(TAG, "exiting GaaiChargingGridDataCard with chargingGridData = $chargingGridData")
    }
}

@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiChargingCarDataCard(
    chargingCarData: ChargingCarData, modifier: Modifier = Modifier
) {
    Log.d(TAG, "Entered GaaiChargingCarDataCard with chargingCarData = $chargingCarData")
    Card(
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row (
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.timestamp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(text= Timestamp.toString(chargingCarData.timestamp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.l1),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingCarData.l1.toString()+" dA",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.l2),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingCarData.l2.toString()+" dA",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.l3),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingCarData.l3.toString()+" dA",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.l1_power),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingCarData.p1.toString()+" W",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.l2_power),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingCarData.p2.toString()+" W",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.l3_power),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingCarData.p3.toString()+" W",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        Log.d(TAG, "exiting GaaiChargingCarDataCard with chargingCarData = $chargingCarData")
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceDetailsScreenPreview() {
    GaaiTheme {
        DeviceDetailsBody(
            device =  Device(
                pn = "12345-AB", sn = "1234-56789-00", mac = "11:22:33:44:55:66", serviceDataValue = 0x17030005
            ),
        state = DeviceDetailsViewState(
            deviceName = "HOME2_",
            deviceInformation= DeviceInformation(modelNumber = "12345", serialNumber= "67890",
                firmwareRevision = "1.23.4", hardwareRevision = "A1"),
            chargingBasicData = ChargingBasicData(seconds =123u, discriminator = Discriminator.STOPPED,
                status = Status.PLUGGED, energy = 1234u, phaseCount = 2u),
            chargingGridData = ChargingGridData(timestamp=0x662D0EFBu, l1=1, l2=2, l3=-1, consumed=12345, interval=345u),
            chargingCarData = ChargingCarData(timestamp=0x662D0EFBu, l1=1, l2=2, l3=-1, p1=1111, p2 =22222, p3=3333)
        ))
    }
}

@Preview(showBackground = true)
@Composable
private fun GaaiChargingCarDataCardPreview() {
    GaaiTheme {
        GaaiChargingCarDataCard(
            chargingCarData = ChargingCarData(timestamp=0x662D0EFBu, l1=1, l2=2, l3=-1, p1=1111, p2 =22222, p3=3333),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
            )
    }
}