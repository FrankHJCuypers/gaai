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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.AuthorizationStatus
import be.cuypers_ghys.gaai.data.ChargingAdvancedData
import be.cuypers_ghys.gaai.data.ChargingBasicData
import be.cuypers_ghys.gaai.data.ChargingCarData
import be.cuypers_ghys.gaai.data.ChargingGridData
import be.cuypers_ghys.gaai.data.ConfigData
import be.cuypers_ghys.gaai.data.ConfigVersion
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.Discriminator
import be.cuypers_ghys.gaai.data.Mode
import be.cuypers_ghys.gaai.data.NetWorkType
import be.cuypers_ghys.gaai.data.Status
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.home.GaaiDeviceCard
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.util.Timestamp
import be.cuypers_ghys.gaai.util.TouPeriod
import be.cuypers_ghys.gaai.util.TouTime
import kotlin.math.roundToInt

// TODO: Split this file im multiple files?
// Tag for logging
private const val TAG = "DeviceDetailsScreen"


object DeviceDetailsDestination : NavigationDestination {
    override val route = "device_detail"
    override val titleRes = R.string.device_details_title
    const val DEVICE_ID_ARG = "deviceId"
    val routeWithArgs = "$route/{$DEVICE_ID_ARG}"
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
            onTouWeekChange = viewModel::sendConfigOperationSetTouWeek,
            onTouWeekendChange = viewModel::sendConfigOperationSetTouWeekend,
            onMaxGridChange = viewModel::sendConfigOperationSetMaxGrid,
            onMaxDeviceChange = viewModel::sendConfigOperationSetMaxDevice,
            onModeChange = viewModel::sendConfigOperationSetMode,
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
    onTouWeekChange: (TouPeriod) -> Unit,
    onTouWeekendChange: (TouPeriod) -> Unit,
    onMaxGridChange: (UByte) -> Unit,
    onMaxDeviceChange: (UByte) -> Unit,
    onModeChange: (Mode) -> Unit,
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

        GaaiChargingAdvancedDataCard(
            chargingAdvancedData=state.chargingAdvancedData,
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
        )

        GaaiConfigDataCard(
            configData=state.configData,
            onTouWeekChange = onTouWeekChange,
            onTouWeekendChange = onTouWeekendChange,
            onMaxGridChange = onMaxGridChange,
            onMaxDeviceChange = onMaxDeviceChange,
            onModeChange = onModeChange,
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
                        text = stringResource(R.string.device_information),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
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
                        text = stringResource(R.string.basic_data),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
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
                        text = stringResource(R.string.grid_data),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
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
                        text = stringResource(R.string.car_data),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
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

@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiChargingAdvancedDataCard(
    chargingAdvancedData: ChargingAdvancedData, modifier: Modifier = Modifier
) {
    Log.d(TAG, "Entered GaaiChargingAdvancedDataCard with chargingAdvancedData = $chargingAdvancedData")
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
                        text = stringResource(R.string.advanced_data),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.timestamp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(text= Timestamp.toString(chargingAdvancedData.timestamp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.i_available),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingAdvancedData.iAvailable.toString()+" A",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.grid_power),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingAdvancedData.gridPower.toString()+" W",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.car_power),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingAdvancedData.carPower.toString()+" W",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.authorization_status),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = when (chargingAdvancedData.authorizationStatus.authStatus)
                        {
                            AuthorizationStatus.UNAUTHORIZED -> "Unauthorized"
                            AuthorizationStatus.AUTHORIZED_DEFAULT -> "Authorized Default"
                            AuthorizationStatus.AUTHORIZED_ECO -> "Authorized ECO"
                            AuthorizationStatus.AUTHORIZED_MAX -> "Authorized MAX"
                            AuthorizationStatus.CHARGE_STOPPED_IN_APP -> "Charge stopped in app"
                            AuthorizationStatus.CHARGE_PAUSED -> "Charge paused"
                            else -> {"Unknown"}
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.error_code),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = chargingAdvancedData.errorCode.toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        Log.d(TAG, "exiting GaaiChargingAdvancedDataCard with chargingAdvancedData = $chargingAdvancedData")
    }
}

@Composable
internal fun TouPeriodRow(
    title: String,
    touPeriod: TouPeriod,
    onDismissRequest: () -> Unit,
    onConfirmation: (TouPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPeriodDialog by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showPeriodDialog = true },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = touPeriod.toString(),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.width(16.dp))
        Icon(
            painter = painterResource(R.drawable.outline_edit_24),
            contentDescription = stringResource(R.string.editable)
        )

        if ( showPeriodDialog ) {
            TouPeriodDialog(
                title = title,
                touPeriod = touPeriod,
                onDismissRequest = {
                    Log.d(TAG, "ShowPeriodDialog dismissed")
                    showPeriodDialog = false
                    onDismissRequest()
                },
                onConfirmation = {
                    newTouPeriod ->
                        Log.d(TAG, "ShowPeriodDialog confirmed: $newTouPeriod")
                        showPeriodDialog = false
                        onConfirmation(newTouPeriod)
                },
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}

@Composable
internal fun GaaiConfigDataCard(
    configData: ConfigData,
    onTouWeekChange: (TouPeriod) -> Unit,
    onTouWeekendChange: (TouPeriod) -> Unit,
    onMaxGridChange: (UByte) -> Unit,
    onMaxDeviceChange: (UByte) -> Unit,
    onModeChange: (Mode) -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "Entered GaaiConfigDataCard with configData = $configData")
    Card(
        modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                var showModeDialog by remember { mutableStateOf(false) }

                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.configuration),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .clickable { showModeDialog = true },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                     Text (
                         text = stringResource(R.string.mode),
                         style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = modeToText (configData.mode) ,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.width(16.dp))
                    Icon(
                        painter = painterResource(R.drawable.outline_edit_24),
                        contentDescription = stringResource(R.string.editable)
                    )
                    if ( showModeDialog )
                    {
                        ModeDialog(
                            mode = configData.mode,
                            onConfirm = {
                                    newMode ->
                                Log.d(TAG, "ModeDialog confirmed: $newMode")
                                onModeChange( newMode )
                                showModeDialog = false
                            },
                            onDismiss = {
                                Log.d(TAG, "ModeDialog dismissed")
                                showModeDialog = false
                            },
                            modifier =  Modifier
                                .padding(dimensionResource(id = R.dimen.padding_small))
                        )
                    }
                }
                AmpereRow (
                    name  = stringResource(R.string.maxGrid),
                    value = configData.maxGrid,
                    minValue = configData.safe,
                    maxValue = 63u, // Largest allowed value in Belgium
                    onConfirm = {
                        newMaxGrid ->
                        Log.d(TAG, "GaaiConfigDataCard Max Grid confirmed: $newMaxGrid")
                        onMaxGridChange(newMaxGrid)
                    },
                    onDismiss = {
                        Log.d(TAG, "GaaiConfigDataCard Max Grid dismissed")
                    } ,
                    modifier = modifier.fillMaxWidth()
                )

                /**
                 * Only show Safe current; don't allow to change it.
                 * To dangerous to change it.
                 */
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.safe),
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = configData.safe.toString()+" A",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                val weekDays = TouPeriod(configData.touWeekStart, configData.touWeekEnd)
                TouPeriodRow(
                    title = stringResource(R.string.touWeek),
                    touPeriod = weekDays,
                    onDismissRequest = {
                        Log.d(TAG, "GaaiConfigDataCard Weekdays TouPeriodRow dismissed")
                    },
                    onConfirmation = {
                        touPeriod ->
                        Log.d(TAG, "GaaiConfigDataCard Weekdays TouPeriodRow confirmed: $touPeriod")
                        onTouWeekChange(touPeriod)
                    },
                    modifier = modifier
                )

                val weekendDays = TouPeriod(configData.touWeekendStart, configData.touWeekendEnd)
                TouPeriodRow(
                    title = stringResource(R.string.touWeekend),
                    touPeriod = weekendDays,
                    onDismissRequest = {
                        Log.d(TAG, "GaaiConfigDataCard Weekend days TouPeriodRow dismissed")
                    },
                    onConfirmation = {
                        touPeriod ->
                        Log.d(TAG, "GaaiConfigDataCard Weekend days TouPeriodRow confirmed: $touPeriod")
                        onTouWeekendChange(touPeriod)
                    },
                    modifier = modifier
                )

                if ( configData.configVersion != ConfigVersion.CONFIG_1_0 ) {
                    AmpereRow (
                        name  = stringResource(R.string.maxDevice),
                        value = configData.maxDevice,
                        minValue = configData.safe,
                        maxValue = 32u,
                        onConfirm = {
                                newMaxDevice ->
                            Log.d(TAG, "GaaiConfigDataCard Max Device confirmed: $newMaxDevice")
                            onMaxDeviceChange(newMaxDevice)
                        },
                        onDismiss = {
                            Log.d(TAG, "GaaiConfigDataCard Max Grid dismissed")
                        } ,
                        modifier = modifier.fillMaxWidth()
                    )

                    /**
                     * Only show Network Time; don't allow to change it.
                     * To dangerous to change it.
                     */
                    Row(
                        modifier = modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.networkType),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = when (configData.networkType) {
                                NetWorkType.MONO_TRIN -> stringResource(R.string.mono_tri_n)
                                NetWorkType.TRI -> stringResource(R.string.tri)
                                else -> stringResource(R.string.unknown)
                            },
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                if ( configData.configVersion == ConfigVersion.CONFIG_CBOR ) {
                    Row(
                        modifier = modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.minDevice),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = configData.minDevice.toString() + " A",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Row(
                        modifier = modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.iCapacity),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = configData.iCapacity.toString() + " A",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
        Log.d(TAG, "exiting GaaiConfigDataCard with configData = $configData")
    }
}

@Composable
private fun modeToText(mode: Mode) = when (mode) {
    Mode.MAX_OPEN -> stringResource(R.string.max_open)
    Mode.MAX_PRIVATE -> stringResource(R.string.max_private)
    Mode.ECO_OPEN -> stringResource(R.string.eco_open)
    Mode.ECO_PRIVATE -> stringResource(R.string.eco_private)
    else -> {
        stringResource(R.string.unknown)
    }
}

@Composable
private fun textToMode(text: String) = when (text) {
    stringResource(R.string.max_open) -> Mode.MAX_OPEN
    stringResource(R.string.max_private) -> Mode.MAX_PRIVATE
    stringResource(R.string.eco_open) -> Mode.ECO_OPEN
    stringResource(R.string.eco_private) -> Mode.ECO_PRIVATE
    else -> {
        Mode.UNKNOWN
    }
}

@Composable
fun ModeDialog(
    mode: Mode,
    onConfirm: (Mode) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val radioOptionsText = listOf(modeToText(mode = Mode.ECO_PRIVATE), modeToText(mode = Mode.MAX_PRIVATE),
        modeToText(mode = Mode.ECO_OPEN), modeToText(mode = Mode.MAX_OPEN), modeToText(mode = Mode.UNKNOWN))
    val radioOptions = listOf(Mode.ECO_PRIVATE, Mode.MAX_PRIVATE, Mode.ECO_OPEN, Mode.MAX_OPEN, Mode.UNKNOWN)
    val modeString = modeToText(mode)
    Dialog (onDismissRequest = { onDismiss() })
    {
        Card (modifier = modifier
        ) {
            Column(modifier = modifier.padding(horizontal = 16.dp)) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = stringResource(R.string.choose_default_mode),
                    style = MaterialTheme.typography.titleMedium,
                )
                val (selectedOption, onOptionSelected) = remember { mutableStateOf(modeString) }
                Column( Modifier.selectableGroup() ) {
                    radioOptionsText.forEach { text ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (text == selectedOption),
                                    onClick = { onOptionSelected(text) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (text == selectedOption),
                                onClick = null // null recommended for accessibility with screen readers
                            )
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyMedium.merge() ,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Spacer(Modifier.weight(1f))
                    Button( onClick = {
                            Log.d(TAG, "ModeDialog dismissed")
                            onDismiss()
                        }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(Modifier.weight(1f))
                    Button(
                        enabled = selectedOption != radioOptionsText[4], // not UNKNOWN
                        onClick = {
                            val newValue = radioOptions[radioOptionsText.indexOf ( selectedOption )]
                            Log.d(TAG, "ModeDialog confirmed $newValue")
                            onConfirm(newValue)
                        }
                    )  {
                        Text(stringResource(R.string.ok))
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }

}

@Composable
fun TouTimeRow (
    name: String,
    time: TouTime,
    onDismissRequest: () -> Unit,
    onConfirmation: (TouTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimeDialog by remember { mutableStateOf(false) }
    var newTime by remember {mutableStateOf(time) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showTimeDialog = true },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = newTime.toString(),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.width(16.dp))
        Icon(
            painter = painterResource(R.drawable.outline_edit_24),
            contentDescription = stringResource(R.string.editable)
        )
        if ( showTimeDialog ) {
            DialTimePickerDialog(
                title = stringResource(R.string.select_time, name.lowercase()),
                touTime = time,
                onConfirm = {
                    touTime ->
                    Log.d(TAG, "showTimeDialog confirmed: $touTime")
                    newTime = touTime
                    onConfirmation( touTime )
                    showTimeDialog = false
                },
                onDismiss = {
                    Log.d(TAG, "showTimeDialog dismissed")
                    onDismissRequest()
                    showTimeDialog = false
                }
            )
       }
    }
}

@Composable
fun TouPeriodDialog(
    title: String,
    touPeriod: TouPeriod,
    onDismissRequest: () -> Unit,
    onConfirmation: (TouPeriod) -> Unit,
    modifier: Modifier = Modifier
){
//    var newTouPeriod by remember { mutableStateOf(touPeriod) }
    var newTouPeriodStart by remember { mutableStateOf(touPeriod.startTime) }
    var newTouPeriodEnd by remember { mutableStateOf(touPeriod.endTime) }

    Dialog (onDismissRequest = { onDismissRequest() }) {
        Card (modifier = modifier) {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            TouTimeRow(
                name = stringResource(R.string.start),
                time = touPeriod.startTime,
                onDismissRequest = {
                    Log.d(TAG, "touTimeRow start dismissed")
                },
                onConfirmation = {
                    touTime ->
                    newTouPeriodStart = touTime
//                    newTouPeriod = newTouPeriod.copy(startTime = touTime)
                    Log.d(TAG, "touTimeRow start confirmed: $touTime")
                },
                modifier = modifier
            )
            TouTimeRow(
                name = stringResource(R.string.end),
                time = touPeriod.endTime,
                onDismissRequest = {
                    Log.d(TAG, "touTimeRow end dismissed")
                },
                onConfirmation = {
                    touTime ->
//                        newTouPeriod = newTouPeriod.copy(endTime = touTime)
                    newTouPeriodEnd = touTime
                        Log.d(TAG, "touTimeRow end confirmed: $touTime")
                },
                modifier = modifier
            )

            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button( onClick = onDismissRequest) {
                    Text(stringResource(R.string.cancel))
                }
                Spacer(Modifier.weight(1f))
                Button( onClick = {
//                    onConfirmation(newTouPeriod)
                    onConfirmation(TouPeriod(newTouPeriodStart,newTouPeriodEnd ))
                } ){
                    Text(stringResource(R.string.ok))
                }
            }
        }
    }
}

@Composable
fun AmpereRow (
    name: String,
    value: UByte,
    minValue: UByte,
    maxValue: UByte,
    onConfirm : (UByte) -> Unit,
    onDismiss : () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSliderDialog by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .clickable { showSliderDialog = true },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = "$value A",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.width(16.dp))
        Icon(
            painter = painterResource(R.drawable.outline_edit_24),
            contentDescription = stringResource(R.string.editable)
        )
        if ( showSliderDialog )  {
            AmpereSliderDialog (
                name = name,
                value = value,
                minValue = minValue,
                maxValue = maxValue,
                onDismiss = {
                    Log.d(TAG, "ampereSliderDialog dismissed")
                    showSliderDialog = false
                    onDismiss()
                },
                onConfirm = {
                    newValue ->
                    Log.d(TAG, "ampereSliderDialog confirmed: $newValue")
                    showSliderDialog = false
                    onConfirm(newValue)
                },
                modifier =  Modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}

@Composable
fun  AmpereSliderDialog(
    name: String,
    value: UByte,
    minValue: UByte,
    maxValue: UByte,
    onConfirm : (UByte) -> Unit,
    onDismiss : () -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableFloatStateOf(value.toFloat()) }
    Dialog (onDismissRequest = { onDismiss() }) {
        Card (modifier = modifier) {
            Column(modifier = modifier.padding(horizontal = 16.dp)) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it.roundToInt().toFloat() },
                    steps = maxValue.toInt() - minValue.toInt() - 1,
                    valueRange = minValue.toFloat()..maxValue.toFloat()
                )
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "$minValue A")
                    Spacer(Modifier.weight(1f))
                    Text(text = "${sliderPosition.toInt()} A")
                    Spacer(Modifier.weight(1f))
                    Text(text = "$maxValue A")
                }
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        Log.d(TAG, "ampereSliderDialog dismissed")
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        val newValue = sliderPosition.toUInt().toUByte()
                        Log.d(TAG, "ampereSliderDialog confirmed $newValue")
                        onConfirm(newValue)
                    } ) {
                        Text(stringResource(R.string.ok))
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialTimePickerDialog(
    title: String,
    touTime: TouTime,
    onConfirm : (TouTime) -> Unit,
    onDismiss : () -> Unit,
    modifier: Modifier = Modifier
) {
    val timePickerState = rememberTimePickerState(
        initialHour = touTime.getHours(),
        initialMinute = touTime.getMinutes(),
        is24Hour = true,
    )
    Dialog(onDismissRequest = { onDismiss() }) {
        Card (modifier = modifier
            .width(IntrinsicSize.Min)
            .height(IntrinsicSize.Min)
        ) {
            Column {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                TimePicker(
                    state = timePickerState,
                )
                Row(
                    modifier = modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        Log.d(TAG, "dialTimePickerDialog dismissed")
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(Modifier.weight(1f))
                    Button(onClick = {
                        val newTouTime = TouTime(timePickerState)
                        Log.d(TAG, "dialTimePickerDialog confirmed $newTouTime")
                        onConfirm(newTouTime)
                    } ) {
                        Text(stringResource(R.string.ok))
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceDetailsPreview() {
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
                chargingCarData = ChargingCarData(timestamp=0x662D0EFBu, l1=1, l2=2, l3=-1, p1=1111, p2 =22222, p3=3333),
                chargingAdvancedData = ChargingAdvancedData(timestamp=0x662D0EFBu, iAvailable = 6, gridPower = 34, carPower= 32,
                    authorizationStatus = AuthorizationStatus(0), errorCode = 0 )
            ),
            onTouWeekChange = {},
            onTouWeekendChange = {},
            onMaxGridChange = {},
            onMaxDeviceChange = {},
            onModeChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DeviceInformationPreview() {
    GaaiTheme {
        GaaiDeviceInformationCard(
            deviceInformation= DeviceInformation(modelNumber = "12345", serialNumber= "67890",
                firmwareRevision = "1.23.4", hardwareRevision = "A1"),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small)
                )
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun ChargingBasicDataPreview() {
    GaaiTheme {
        GaaiChargingBasicDataCard(
            chargingBasicData = ChargingBasicData(seconds =123u, discriminator = Discriminator.STOPPED,
                    status = Status.PLUGGED, energy = 1234u, phaseCount = 2u),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small)
            )
        )
    }
}



@Preview(showBackground = true)
@Composable
private fun ChargingGridDataPreview() {
    GaaiTheme {
        GaaiChargingGridDataCard(
            chargingGridData = ChargingGridData(timestamp=0x662D0EFBu, l1=1, l2=2, l3=-1, consumed=12345, interval=345u),
            modifier = Modifier
                 .padding(dimensionResource(id = R.dimen.padding_small)
            )
        )
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

@Preview(showBackground = true)
@Composable
private fun GaaiChargingAdvancedDataCardPreview() {
    GaaiTheme {
        GaaiChargingAdvancedDataCard(
            chargingAdvancedData = ChargingAdvancedData(timestamp=0x662D0EFBu, iAvailable = 6, gridPower = 34, carPower= 32,
                authorizationStatus = AuthorizationStatus(0), errorCode = 0 ),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GaaiConfigDataCardPreview() {
    GaaiTheme {
        GaaiConfigDataCard(
            configData = ConfigData(maxGrid=50U, maxDevice=32U, mode = Mode.MAX_PRIVATE, safe=6U, networkType = NetWorkType.MONO_TRIN,
                touWeekStart = 123, touWeekEnd = 456, touWeekendStart = 789, touWeekendEnd = 333, minDevice = 11U, iCapacity = 22U,
                configVersion = ConfigVersion.CONFIG_1_1),
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small)),
            onTouWeekChange ={},
            onTouWeekendChange ={},
            onMaxGridChange = {},
            onMaxDeviceChange = {},
            onModeChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TouPeriodDialogPreview() {
    GaaiTheme {
        TouPeriodDialog(
            title = "Weekdays",
            touPeriod = TouPeriod(720,1234),
            onDismissRequest = {},
            onConfirmation = {},
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DialTimePickerDialogPreview() {
    GaaiTheme {
        DialTimePickerDialog(
            title = "Sample Time Picker Dialog",
            touTime = TouTime(13,45),
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AmpereSliderDialogPreview() {
    GaaiTheme {
        AmpereSliderDialog(
            name = "Sample Ampere x",
            value = 32u,
            minValue = 6u,
            maxValue = 50u,
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ModeDialogPreview() {
    GaaiTheme {
        ModeDialog(
            mode = Mode.ECO_PRIVATE,
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ModeDialogUnknownPreview() {
    GaaiTheme {
        ModeDialog(
            mode = Mode.UNKNOWN,
            onDismiss = {},
            onConfirm = {},
        )
    }
}
