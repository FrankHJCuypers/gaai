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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
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
import be.cuypers_ghys.gaai.data.Badge
import be.cuypers_ghys.gaai.data.ChargerType
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
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_AUTO
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_DEFAULT
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_ECO
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_MAX
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_STOP_CHARGING
import be.cuypers_ghys.gaai.data.Status
import be.cuypers_ghys.gaai.data.TimeData
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.home.GaaiDeviceCard
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.permissions.RequireBluetooth
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.util.Timestamp
import be.cuypers_ghys.gaai.util.TouPeriod
import be.cuypers_ghys.gaai.util.TouTime
import kotlin.math.roundToInt

// TODO: Split this file im multiple files?
// Tag for logging
private const val TAG = "DeviceDetailsScreen"

/**
 * The [NavigationDestination] information for the [DeviceDetailsScreen].
 *
 * @author Frank HJ Cuypers
 */
object DeviceDetailsDestination : NavigationDestination {
  override val route = "device_detail"
  override val titleRes = R.string.device_details_title
  const val DEVICE_ID_ARG = "deviceId"
  val routeWithArgs = "$route/{$DEVICE_ID_ARG}"
}

/**
 * Implements the complete screen for displaying all the information received from the Nexxtender charger,
 * including app bars.
 * @param onNavigateUp Function to be called when [DeviceDetailsScreen] wants to navigate up.
 * @param navigateToBadgeList Function to be called when [DeviceDetailsScreen] wants to show list of badges.
 * @param canNavigateUp Is the [DeviceDetailsScreen] allowed to navigate back?
 * @param viewModel The [DeviceDetailsViewModel] to be associated with this [DeviceDetailsScreen].
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun DeviceDetailsScreen(
  // TODO: remove unused navigateBack? What is difference with onNavigateUp? Is correct one used?
  onNavigateUp: () -> Unit,
  navigateToBadgeList: (Int) -> Unit,
  canNavigateUp: Boolean = true,
  viewModel: DeviceDetailsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
  Log.v(TAG, "ENTRY DeviceDetailsScreen()")

  val deviceId = viewModel.deviceId
  val state by viewModel.state.collectAsStateWithLifecycle()
  val device by viewModel.device.collectAsStateWithLifecycle()

  RequireBluetooth {
    DeviceDetailsScreenNoViewModel(
      onNavigateUp = onNavigateUp,
      canNavigateUp = canNavigateUp,
      navigateUp = { viewModel.navigateUp(onNavigateUp) },
      device = device,
      state = state,
      onTouWeekChange = viewModel::sendConfigOperationSetTouWeek,
      onTouWeekendChange = viewModel::sendConfigOperationSetTouWeekend,
      onMaxGridChange = viewModel::sendConfigOperationSetMaxGrid,
      onMaxDeviceChange = viewModel::sendConfigOperationSetMaxDevice,
      onSafeChange = viewModel::sendConfigOperationSetSafe,
      onModeChange = viewModel::sendConfigOperationSetMode,
      onICapacityChange = viewModel::sendConfigOperationSetICapacity,
      onTimeGet = viewModel::sendTimeOperationGetTime,
      onTimeSync = viewModel::sendTimeOperationSyncTime,
      onLoaderOperation = viewModel::sendLoaderOperation,
      navigateToBadgeList = { navigateToBadgeList(deviceId) },
    )
  }
  Log.v(TAG, "RETURN DeviceDetailsScreen()")
}

/**
 * Implements the complete screen for displaying all the information received from the Nexxtender charger,
 * including app bars.
 * @param onNavigateUp Function to be called when [DeviceDetailsScreen] wants to navigate up.
 * @param navigateToBadgeList Function to be called when [DeviceDetailsScreen] wants to show list of badges.
 * @param device The information for the [Device] determined by the [DeviceDetailsViewModel].
 * @param state The state for the [Device] determined by the [DeviceDetailsViewModel].
 *  The state includes all information received from the device over BLE.
 * @param onTouWeekChange Function to be called when [DeviceDetailsBody] wants to change the
 *  device's [ConfigData.touWeekStart] and [ConfigData.touWeekEnd].
 * @param onTouWeekendChange Function to be called when [DeviceDetailsBody] wants to change the
 *  device's [ConfigData.touWeekendStart] and [ConfigData.touWeekendEnd].
 * @param onMaxGridChange Function to be called when [DeviceDetailsBody] wants to change the
 *  device's [ConfigData.maxGrid].
 * @param onMaxDeviceChange Function to be called when [DeviceDetailsBody] wants to change the
 *  device's [ConfigData.maxDevice].
 * @param onSafeChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.safe].
 * @param onModeChange Function to be called when [DeviceDetailsBody] wants to change the
 *  device's [ConfigData.mode].
 * @param onICapacityChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.iCapacity].
 * @param onTimeGet Function to be called when [DeviceDetailsBody] wants to read the
 *  device's [TimeData.time].
 * @param onTimeSync Function to be called when [DeviceDetailsBody] wants to sync the
 *  device's [TimeData.time] with the current time on the mobile phone.
 * @param onLoaderOperation Function to be called when [DeviceDetailsBody] wants to perform a loader operation.
 * @param modifier The [Modifier] to be applied to this [DeviceDetailsBody].
 * @param canNavigateUp Is the [DeviceDetailsScreen] allowed to navigate back?
 *
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsScreenNoViewModel(
  // TODO: remove unused navigateBack? What is difference with onNavigateUp? Is correct one used?
  onNavigateUp: () -> Unit,
  navigateToBadgeList: () -> Unit,
  navigateUp: () -> Unit,
  device: Device?,
  state: DeviceDetailsViewState,
  onTouWeekChange: (TouPeriod) -> Unit,
  onTouWeekendChange: (TouPeriod) -> Unit,
  onMaxGridChange: (UByte) -> Unit,
  onMaxDeviceChange: (UByte) -> Unit,
  onSafeChange: (UByte) -> Unit,
  onModeChange: (Mode) -> Unit,
  onICapacityChange: (UByte) -> Unit,
  onTimeGet: () -> Unit,
  onTimeSync: () -> Unit,
  onLoaderOperation: (Int) -> Unit,
  modifier: Modifier = Modifier,
  canNavigateUp: Boolean = true
) {
  Log.v(TAG, "ENTRY DeviceDetailsScreenNoViewModel()")
  rememberCoroutineScope()
  Scaffold(
    topBar = {
      GaaiTopAppBar(
        title = stringResource(DeviceDetailsDestination.titleRes),
        canNavigateUp = canNavigateUp,
        navigateUp = navigateUp
      )
    }
  ) { innerPadding ->
    DeviceDetailsBody(
      device,
      state,
      onTouWeekChange = onTouWeekChange,
      onTouWeekendChange = onTouWeekendChange,
      onMaxGridChange = onMaxGridChange,
      onMaxDeviceChange = onMaxDeviceChange,
      onSafeChange = onSafeChange,
      onModeChange = onModeChange,
      onICapacityChange = onICapacityChange,
      onTimeGet = onTimeGet,
      onTimeSync = onTimeSync,
      onLoaderOperation = onLoaderOperation,
      navigateToBadgeList = navigateToBadgeList,
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
  Log.v(TAG, "RETURN DeviceDetailsScreenNoViewModel()")
}

/**
 * Implements the body of the screen displaying all the information received from the Nexxtender charger.
 * @param device The information for the [Device] determined by the [DeviceDetailsViewModel].
 * @param state The state for the [Device] determined by the [DeviceDetailsViewModel].
 *  The state includes all information received from the device over BLE.
 * @param onTouWeekChange Function to be called when [DeviceDetailsBody] wants to change the
 *  device's [ConfigData.touWeekStart] and [ConfigData.touWeekEnd].
 * @param onTouWeekendChange Function to be called when [DeviceDetailsBody] wants to change the
 *  device's [ConfigData.touWeekendStart] and [ConfigData.touWeekendEnd].
 * @param onMaxGridChange Function to be called when [DeviceDetailsBody] wants to change the
 *  device's [ConfigData.maxGrid].
 * @param onMaxDeviceChange Function to be called when [DeviceDetailsBody] wants to change the
 *  device's [ConfigData.maxDevice].
 * @param onSafeChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.safe].
 * @param onModeChange Function to be called when [DeviceDetailsBody] wants to change the
 *  device's [ConfigData.mode].
 * @param onICapacityChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.iCapacity].
 * @param onTimeGet Function to be called when [DeviceDetailsBody] wants to read the
 *  device's [TimeData.time].
 * @param onTimeSync Function to be called when [DeviceDetailsBody] wants to sync the
 *  device's [TimeData.time] with the current time on the mobile phone.
 * @param onLoaderOperation Function to be called when [DeviceDetailsBody] wants to perform a loader operation.
 * @param navigateToBadgeList Function to be called when [DeviceDetailsBody] wants to show list of badges.
 * @param modifier The [Modifier] to be applied to this [DeviceDetailsBody].
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun DeviceDetailsBody(
  device: Device?,
  state: DeviceDetailsViewState,
  onTouWeekChange: (TouPeriod) -> Unit,
  onTouWeekendChange: (TouPeriod) -> Unit,
  onMaxGridChange: (UByte) -> Unit,
  onMaxDeviceChange: (UByte) -> Unit,
  onSafeChange: (UByte) -> Unit,
  onModeChange: (Mode) -> Unit,
  onICapacityChange: (UByte) -> Unit,
  onTimeGet: () -> Unit,
  onTimeSync: () -> Unit,
  onLoaderOperation: (Int) -> Unit,
  navigateToBadgeList: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY DeviceDetailsBody(device = $device)")

  Column(
    modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
  ) {
    device?.let {
      GaaiDeviceCard(
        device = it,
        state.deviceState.connectionState,
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }

    if ( state.deviceState.connectionState == ConnectionState.CONNECTED )
    {
      GaaiDeviceNameCard(
        deviceName = state.deviceName,
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )

      GaaiDeviceInformationCard(
        deviceInformation = state.deviceInformation,
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )

      GaaiChargingBasicDataCard(
        chargingBasicData = state.chargingBasicData,
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )

      if (device?.type != ChargerType.MOBILE) {
        GaaiChargingGridDataCard(
          chargingGridData = state.chargingGridData,
          modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_small))
        )

        GaaiChargingCarDataCard(
          chargingCarData = state.chargingCarData,
          modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_small))
        )

        GaaiChargingAdvancedDataCard(
          chargingAdvancedData = state.chargingAdvancedData,
          modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_small))
        )

        GaaiConfigDataCard(
          configData = state.configData,
          onTouWeekChange = onTouWeekChange,
          onTouWeekendChange = onTouWeekendChange,
          onMaxGridChange = onMaxGridChange,
          onSafeChange = onSafeChange,
          onMaxDeviceChange = onMaxDeviceChange,
          onModeChange = onModeChange,
          onICapacityChange = onICapacityChange,
          modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_small))
        )

        GaaiTimeDataCard(
          timeData = state.timeData,
          onTimeGet = onTimeGet,
          onTimeSync = onTimeSync,
          modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_small))
        )

        GaaiLoaderCard(
          onLoaderOperation = onLoaderOperation,
          modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_small))
        )

        GaaiBadgesCard(
          navigateToBadgeList = navigateToBadgeList,
          modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_small))
        )
      }
    }
  }
  Log.v(TAG, "RETURN DeviceDetailsBody()")
}

/**
 * Implements a [Card] displaying the [deviceName].
 * @param deviceName The name to display.
 * @param modifier the [Modifier] to be applied to this GaaiDeviceNameCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiDeviceNameCard(
  deviceName: String, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiDeviceNameCard(deviceName = $deviceName)")
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
    Log.v(TAG, "RETURN GaaiNameCard()")
  }
}

/**
 * Implements a [Card] displaying the [deviceInformation].
 * @param deviceInformation The information to display.
 * @param modifier the [Modifier] to be applied to this GaaiDeviceInformationCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiDeviceInformationCard(
  deviceInformation: DeviceInformation, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiDeviceInformationCard(deviceInformation = $deviceInformation)")
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
    Log.v(TAG, "RETURN GaaiDeviceInformationCard()")
  }
}

/**
 * Implements a [Card] displaying the [chargingBasicData].
 * @param chargingBasicData The information to display.
 * @param modifier the [Modifier] to be applied to this GaaiChargingBasicDataCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiChargingBasicDataCard(
  chargingBasicData: ChargingBasicData, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiChargingBasicDataCard(chargingBasicData = $chargingBasicData)")
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
            text = "%3d s".format(chargingBasicData.seconds.toInt()),
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
            text = "%.3f kWh".format(chargingBasicData.energy.toFloat() / 1000.0),
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
    Log.v(TAG, "RETURN GaaiChargingBasicDataCard()")
  }
}

/**
 * Implements a [Card] displaying the [chargingGridData].
 * @param chargingGridData The information to display.
 * @param modifier the [Modifier] to be applied to this GaaiChargingGridDataCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiChargingGridDataCard(
  chargingGridData: ChargingGridData, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiChargingGridDataCard(chargingGridData = $chargingGridData)")
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
          Text(
            text = Timestamp.toString(chargingGridData.timestamp),
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
            text = "%.1f A".format(chargingGridData.l1.toFloat() / 10.0),
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
            text = "%.1f A".format(chargingGridData.l2.toFloat() / 10.0),
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
            text = "%.1f A".format(chargingGridData.l3.toFloat() / 10.0),
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
            text = "%.3f kWh?".format(chargingGridData.consumed.toFloat() / 1000.0),
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
            text = "%3d s".format(chargingGridData.interval.toInt()),
            style = MaterialTheme.typography.titleMedium
          )
        }
      }
    }
    Log.v(TAG, "RETURN GaaiChargingGridDataCard()")
  }
}

/**
 * Implements a [Card] displaying the [chargingCarData].
 * @param chargingCarData The information to display.
 * @param modifier the [Modifier] to be applied to this GaaiChargingCarDataCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiChargingCarDataCard(
  chargingCarData: ChargingCarData, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiChargingCarDataCard(chargingCarData = $chargingCarData)")
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
          Text(
            text = Timestamp.toString(chargingCarData.timestamp),
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
            text = "%.1f A".format(chargingCarData.l1.toFloat() / 10.0),
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
            text = "%.1f A".format(chargingCarData.l2.toFloat() / 10.0),
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
            text = "%.1f A".format(chargingCarData.l3.toFloat() / 10.0),
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
            text = "%.3f kW".format(chargingCarData.p1.toFloat() / 1000.0),
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
            text = "%.3f kW".format(chargingCarData.p2.toFloat() / 1000.0),
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
            text = "%.3f kW".format(chargingCarData.p3.toFloat() / 1000.0),
            style = MaterialTheme.typography.titleMedium
          )
        }
      }
    }
    Log.v(TAG, "RETURN GaaiChargingCarDataCard()")
  }
}

/**
 * Implements a [Card] displaying the [chargingAdvancedData].
 * @param chargingAdvancedData The information to display.
 * @param modifier the [Modifier] to be applied to this GaaiChargingAdvancedDataCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
// TODO: factorize to its own file, since it is also used in DeviceEntryViewModel.kt
internal fun GaaiChargingAdvancedDataCard(
  chargingAdvancedData: ChargingAdvancedData, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiChargingAdvancedDataCard(chargingAdvancedData = $chargingAdvancedData)")
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
          Text(
            text = Timestamp.toString(chargingAdvancedData.timestamp),
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
            text = chargingAdvancedData.iAvailable.toString() + " A",
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
            text = "%.3f kW".format(chargingAdvancedData.gridPower.toFloat() / 1000.0),
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
            text = "%.3f kW".format(chargingAdvancedData.carPower.toFloat() / 1000.0),
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
            text = when (chargingAdvancedData.authorizationStatus.authStatus) {
              AuthorizationStatus.UNAUTHORIZED -> stringResource(R.string.unauthorized)
              AuthorizationStatus.AUTHORIZED_DEFAULT -> stringResource(R.string.authorized_default)
              AuthorizationStatus.AUTHORIZED_ECO -> stringResource(R.string.authorized_eco)
              AuthorizationStatus.AUTHORIZED_MAX -> stringResource(R.string.authorized_max)
              AuthorizationStatus.CHARGE_STOPPED_IN_APP -> stringResource(R.string.charge_stopped_in_app)
              AuthorizationStatus.CHARGE_PAUSED -> stringResource(R.string.charge_paused)
              else -> stringResource(R.string.unknown)
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
    Log.v(TAG, "RETURN GaaiChargingAdvancedDataCard()")
  }
}

/**
 * Implements a [Row] displaying the [touPeriod].
 * Also allows to change the displayed [TouPeriod.startTime] and [TouPeriod.endTime] in a dialog box.
 * @param title The label to display next to the [touPeriod] information.
 * @param touPeriod The period to display.
 * @param onDismissRequest Function to be called when changing the [TouPeriod.startTime] and [TouPeriod.endTime]
 * is cancelled by the user.
 * @param onConfirmation Function to be called when changing the [TouPeriod.startTime] and [TouPeriod.endTime]
 * is confirmed by the user.
 * @param modifier the [Modifier] to be applied to this GaaiChargingAdvancedDataCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun TouPeriodRow(
  title: String,
  touPeriod: TouPeriod,
  onDismissRequest: () -> Unit,
  onConfirmation: (TouPeriod) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY TouPeriodRow(touPeriod = $touPeriod)")
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

    if (showPeriodDialog) {
      TouPeriodDialog(
        title = title,
        touPeriod = touPeriod,
        onDismissRequest = {
          Log.v(TAG, "ShowPeriodDialog dismissed")
          showPeriodDialog = false
          onDismissRequest()
        },
        onConfirmation = { newTouPeriod ->
          Log.d(TAG, "ShowPeriodDialog confirmed: $newTouPeriod")
          showPeriodDialog = false
          onConfirmation(newTouPeriod)
        },
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
  Log.v(TAG, "RETURN TouPeriodRow()")
}

/**
 * Implements a [Card] displaying the [configData] with the possibility to change some of the values on
 * the Nexxtender Home device.
 *
 * @param configData The information to display and possibly change.
 * @param onTouWeekChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.touWeekStart] and [ConfigData.touWeekEnd].
 * @param onTouWeekendChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.touWeekendStart] and [ConfigData.touWeekendEnd].
 * @param onMaxGridChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.maxGrid].
 * @param onSafeChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.safe].
 * @param onMaxDeviceChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.maxDevice].
 * @param onModeChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.mode].
 * @param onICapacityChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.iCapacity].
 * @param modifier the [Modifier] to be applied to this GaaiConfigDataCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiConfigDataCard(
  configData: ConfigData,
  onTouWeekChange: (TouPeriod) -> Unit,
  onTouWeekendChange: (TouPeriod) -> Unit,
  onMaxGridChange: (UByte) -> Unit,
  onSafeChange: (UByte) -> Unit,
  onMaxDeviceChange: (UByte) -> Unit,
  onModeChange: (Mode) -> Unit,
  onICapacityChange: (UByte) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiConfigDataCard(configData = $configData)")
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
          val title = when (configData.configVersion) {
            ConfigVersion.CONFIG_1_0 -> " 1.0"
            ConfigVersion.CONFIG_1_1 -> " 1.1"
            ConfigVersion.CONFIG_CBOR -> " CBOR"
          }
          Text(
            text = stringResource(R.string.configuration) + title,
            style = MaterialTheme.typography.titleLarge,
          )
        }
        Row(
          modifier = modifier
            .fillMaxWidth()
            .clickable { showModeDialog = true },
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = if (configData.configVersion == ConfigVersion.CONFIG_CBOR) stringResource(R.string.charge_mode)
            else stringResource(R.string.mode),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = modeToText(configData.mode),
            style = MaterialTheme.typography.titleMedium
          )
          Spacer(Modifier.width(16.dp))
          Icon(
            painter = painterResource(R.drawable.outline_edit_24),
            contentDescription = stringResource(R.string.editable)
          )
          if (showModeDialog) {
            ModeDialog(
              mode = configData.mode,
              onConfirm = { newMode ->
                Log.d(TAG, "ModeDialog confirmed: $newMode")
                onModeChange(newMode)
                showModeDialog = false
              },
              onDismiss = {
                Log.v(TAG, "ModeDialog dismissed")
                showModeDialog = false
              },
              modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
            )
          }
        }

        AmpereRow(
          name = if (configData.configVersion == ConfigVersion.CONFIG_CBOR) stringResource(R.string.i_max)
          else stringResource(
            R.string.maxGrid
          ),
          value = configData.maxGrid,
          minValue = configData.safe,
          maxValue = 63u, // Largest allowed value in Belgium
          onConfirm = { newMaxGrid ->
            Log.d(TAG, "GaaiConfigDataCard Max Grid confirmed: $newMaxGrid")
            onMaxGridChange(newMaxGrid)
          },
          onDismiss = {
            Log.v(TAG, "GaaiConfigDataCard Max Grid dismissed")
          },
          modifier = modifier.fillMaxWidth()
        )

        AmpereRow(
          name = if (configData.configVersion == ConfigVersion.CONFIG_CBOR) stringResource(R.string.i_level_1)
          else stringResource(
            R.string.safe
          ),
          value = configData.safe,
          minValue = 0u,
          maxValue = configData.maxDevice,
          onConfirm = { newSafe ->
            Log.d(TAG, "GaaiConfigDataCard Safe confirmed: $newSafe")
            onSafeChange(newSafe)
          },
          onDismiss = {
            Log.v(TAG, "GaaiConfigDataCard Safe dismissed")
          },
          modifier = modifier.fillMaxWidth()
        )

        val weekDays = TouPeriod(configData.touWeekStart, configData.touWeekEnd)
        TouPeriodRow(
          title = stringResource(R.string.touWeek),
          touPeriod = weekDays,
          onDismissRequest = {
            Log.d(TAG, "GaaiConfigDataCard Weekdays TouPeriodRow dismissed")
          },
          onConfirmation = { touPeriod ->
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
            Log.v(TAG, "GaaiConfigDataCard Weekend days TouPeriodRow dismissed")
          },
          onConfirmation = { touPeriod ->
            Log.d(TAG, "GaaiConfigDataCard Weekend days TouPeriodRow confirmed: $touPeriod")
            onTouWeekendChange(touPeriod)
          },
          modifier = modifier
        )

        if (configData.configVersion != ConfigVersion.CONFIG_1_0) {
          AmpereRow(
            name = if (configData.configVersion == ConfigVersion.CONFIG_CBOR) stringResource(R.string.i_evse_max)
            else stringResource(
              R.string.maxDevice
            ),
            value = configData.maxDevice,
            minValue = configData.safe,
            maxValue = 32u,
            onConfirm = { newMaxDevice ->
              Log.d(TAG, "GaaiConfigDataCard Max Device confirmed: $newMaxDevice")
              onMaxDeviceChange(newMaxDevice)
            },
            onDismiss = {
              Log.v(TAG, "GaaiConfigDataCard Max Grid dismissed")
            },
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
              text = if (configData.configVersion == ConfigVersion.CONFIG_CBOR) "Phase Seq (Network Type)"
              else stringResource(
                R.string.networkType
              ),
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
        if (configData.configVersion == ConfigVersion.CONFIG_CBOR) {
          Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = stringResource(R.string.i_evse_min),
              style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.weight(1f))
            Text(
              text = configData.minDevice.toString() + " A",
              style = MaterialTheme.typography.titleMedium
            )
          }

          AmpereRow(
            name = stringResource(R.string.iCapacity),
            value = configData.iCapacity,
            minValue = configData.safe,
            maxValue = configData.maxGrid,
            onConfirm = { newICapacity ->
              Log.d(TAG, "GaaiConfigDataCard I Capacity confirmed: $newICapacity")
              onICapacityChange(newICapacity)
            },
            onDismiss = {
              Log.v(TAG, "GaaiConfigDataCard I Capacity dismissed")
            },
            modifier = modifier.fillMaxWidth()
          )
        }
      }
    }
    Log.v(TAG, "RETURN GaaiConfigDataCard()")
  }
}

/**
 * Implements a [Card] displaying the [timeData] with the possibility to get and set the time of
 * the Nexxtender Home device.
 *
 * @param timeData The information to get or set.
 * @param onTimeGet Function to be called when [GaaiTimeDataCard] wants to read the
 *  device's [TimeData.time].
 * @param onTimeSync Function to be called when [GaaiTimeDataCard] wants to sync the
 *  device's [TimeData.time] with the current time on the mobile phone.
 * @param modifier the [Modifier] to be applied to this [GaaiTimeDataCard]
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiTimeDataCard(
  timeData: TimeData,
  onTimeGet: () -> Unit,
  onTimeSync: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiTimeDataCard(timeData = $timeData)")
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
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.time),
            style = MaterialTheme.typography.titleLarge,
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.current_time),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = if (timeData.time == 0u) "Not loaded yet" else Timestamp.toString(timeData.time),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Spacer(Modifier.weight(1f))
          Button(onClick = {
            Log.v(TAG, "GaaiTimeDataCard Get Time pressed")
            onTimeGet()
          }
          ) {
            Text(stringResource(R.string.get_time))
          }
          Spacer(Modifier.weight(1f))
          Button(onClick = {
            Log.v(TAG, "GaaiTimeDataCard Sync Time pressed")
            onTimeSync()
          }
          ) {
            Text(stringResource(R.string.sync_time))
          }
          Spacer(Modifier.weight(1f))
        }
      }
    }
    Log.v(TAG, "RETURN GaaiTimeDataCard()")
  }
}

/**
 * Implements a [Card] allowing to select one of the Nexxtender Home's loader operations.
 * @param onLoaderOperation Function to be called when [GaaiLoaderCard] wants to perform a loader operation.
 * @param modifier the [Modifier] to be applied to this [GaaiLoaderCard]
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiLoaderCard(
  onLoaderOperation: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.v(TAG, "ENTRY GaaiLoaderCard()")
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
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.loader),
            style = MaterialTheme.typography.titleLarge,
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Button(onClick = {
            Log.v(TAG, "GaaiLoaderCard Start Charge Default")
            onLoaderOperation(LOADER_OPERATION_START_CHARGING_DEFAULT)
          }
          ) {
            Text(stringResource(R.string.start_charge_default))
          }
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Button(onClick = {
            Log.v(TAG, "GaaiLoaderCard Start Charge Max")
            onLoaderOperation(LOADER_OPERATION_START_CHARGING_MAX)
          }
          ) {
            Text(stringResource(R.string.start_charge_max))
          }
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Button(onClick = {
            Log.v(TAG, "GaaiLoaderCard Start Charge Auto")
            onLoaderOperation(LOADER_OPERATION_START_CHARGING_AUTO)
          }
          ) {
            Text(stringResource(R.string.start_charge_auto))
          }
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Button(onClick = {
            Log.v(TAG, "GaaiLoaderCard Start Charge Eco")
            onLoaderOperation(LOADER_OPERATION_START_CHARGING_ECO)
          }
          ) {
            Text(stringResource(R.string.start_charge_eco))
          }
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Button(onClick = {
            Log.v(TAG, "GaaiLoaderCard Stop Charge")
            onLoaderOperation(LOADER_OPERATION_STOP_CHARGING)
          }
          ) {
            Text(stringResource(R.string.stop_charging))
          }
        }

      }
    }
    Log.v(TAG, "RETURN GaaiLoaderCard()")
  }
}

/**
 * Implements a [Card] allowing to go to the list of [Badges][Badge].
 * @param navigateToBadgeList Function to be called when [DeviceDetailsScreen] wants to show list of badges.
 * @param modifier the [Modifier] to be applied to this [GaaiLoaderCard]
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiBadgesCard(
  navigateToBadgeList: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.v(TAG, "ENTRY GaaiBadgesCard()")
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
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.badges),
            style = MaterialTheme.typography.titleLarge,
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Button(onClick = {
            Log.v(TAG, "GaaiBadgesCard Badges")
            navigateToBadgeList()
          }
          ) {
            Text(stringResource(R.string.badges))
          }
        }
      }
    }
    Log.v(TAG, "RETURN GaaiLoaderCard()")
  }
}

/**
 * Converts [mode] to a string value to display.
 * @param mode
 * @return The corresponding string.
 *
 * @author Frank HJ Cuypers
 */
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

/**
 * Implements a [Dialog] displaying the [mode], allowing it to be changed.
 *
 * @param mode The [Mode] to display and possibly change
 * @param onDismiss Function to be called when changing the [mode] is cancelled by the user.
 * @param onConfirm Function to be called when changing the [mode] is confirmed by the user.
 * @param modifier the [Modifier] to be applied to this ModeDialog.
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun ModeDialog(
  mode: Mode,
  onConfirm: (Mode) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY ModeDialog(mode=$mode)")

  val radioOptionsText = listOf(
    modeToText(mode = Mode.ECO_PRIVATE), modeToText(mode = Mode.MAX_PRIVATE),
    modeToText(mode = Mode.ECO_OPEN), modeToText(mode = Mode.MAX_OPEN), modeToText(mode = Mode.UNKNOWN)
  )
  val radioOptions = listOf(Mode.ECO_PRIVATE, Mode.MAX_PRIVATE, Mode.ECO_OPEN, Mode.MAX_OPEN, Mode.UNKNOWN)
  val modeString = modeToText(mode)
  Dialog(onDismissRequest = { onDismiss() })
  {
    Card(
      modifier = modifier
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
        Column(Modifier.selectableGroup()) {
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
                style = MaterialTheme.typography.bodyMedium.merge(),
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
          Button(onClick = {
            Log.v(TAG, "ModeDialog dismissed")
            onDismiss()
          }
          ) {
            Text(stringResource(R.string.cancel))
          }
          Spacer(Modifier.weight(1f))
          Button(
            enabled = selectedOption != radioOptionsText[4], // not UNKNOWN
            onClick = {
              val newValue = radioOptions[radioOptionsText.indexOf(selectedOption)]
              Log.d(TAG, "ModeDialog confirmed $newValue")
              onConfirm(newValue)
            }
          ) {
            Text(stringResource(R.string.ok))
          }
          Spacer(Modifier.weight(1f))
        }
      }
    }
  }
  Log.v(TAG, "RETURN ModeDialog()")
}

/**
 * Implements a [Row] displaying the [time],
 * allowing to change the displayed [TouTime] .
 * @param name The label to display next to the [time] information.
 * @param time The time to display.
 * @param onDismissRequest Function to be called when changing the [TouTime] is cancelled by the user.
 * @param onConfirmation Function to be called when changing the [TouTime] is confirmed by the user.
 * @param modifier the [Modifier] to be applied to this TouTimeRow.
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun TouTimeRow(
  name: String,
  time: TouTime,
  onDismissRequest: () -> Unit,
  onConfirmation: (TouTime) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY TouTimeRow(name=$name)")

  var showTimeDialog by remember { mutableStateOf(false) }
  var newTime by remember { mutableStateOf(time) }
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
    if (showTimeDialog) {
      DialTimePickerDialog(
        title = stringResource(R.string.select_time, name.lowercase()),
        touTime = time,
        onConfirm = { touTime ->
          Log.d(TAG, "showTimeDialog confirmed: $touTime")
          newTime = touTime
          onConfirmation(touTime)
          showTimeDialog = false
        },
        onDismiss = {
          Log.v(TAG, "showTimeDialog dismissed")
          onDismissRequest()
          showTimeDialog = false
        }
      )
    }
  }
  Log.v(TAG, "RETURN TouTimeRow()")
}

/**
 * Implements a [Dialog] displaying the [touPeriod],
 * allowing to change the displayed [TouPeriod.startTime] and [TouPeriod.endTime].
 * @param title The label to display next to the [touPeriod] information.
 * @param touPeriod The period to display.
 * @param onDismissRequest Function to be called when changing the [TouPeriod.startTime] and [TouPeriod.endTime]
 * is cancelled by the user.
 * @param onConfirmation Function to be called when changing the [TouPeriod.startTime] and [TouPeriod.endTime]
 * is confirmed by the user.
 * @param modifier the [Modifier] to be applied to this TouPeriodDialog.
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun TouPeriodDialog(
  title: String,
  touPeriod: TouPeriod,
  onDismissRequest: () -> Unit,
  onConfirmation: (TouPeriod) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY TouPeriodDialog(title=$title)")

//    var newTouPeriod by remember { mutableStateOf(touPeriod) }
  var newTouPeriodStart by remember { mutableStateOf(touPeriod.startTime) }
  var newTouPeriodEnd by remember { mutableStateOf(touPeriod.endTime) }

  Dialog(onDismissRequest = { onDismissRequest() }) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
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
          Log.v(TAG, "touTimeRow start dismissed")
        },
        onConfirmation = { touTime ->
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
          Log.v(TAG, "touTimeRow end dismissed")
        },
        onConfirmation = { touTime ->
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
        Button(onClick = onDismissRequest) {
          Text(stringResource(R.string.cancel))
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = {
//                    onConfirmation(newTouPeriod)
          onConfirmation(TouPeriod(newTouPeriodStart, newTouPeriodEnd))
        }) {
          Text(stringResource(R.string.ok))
        }
      }
    }
  }
  Log.v(TAG, "RETURN TouPeriodDialog()")
}

/**
 * Implements a [Row] displaying the [value] in Ampere, allowing it to be changed.
 *
 * @param name The label to place with the [value]
 * @param value The value in Ampere to display and possibly change.
 * @param minValue The minimal value allowed for [value] when changing.
 * @param maxValue The maximum value allowed for [value] when changing.
 * @param onConfirm Function to be called when changing the [value] is confirmed by the user.
 * @param onDismiss Function to be called when changing the [value] is cancelled by the user.
 * @param modifier the [Modifier] to be applied to this AmpereRow
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun AmpereRow(
  name: String,
  value: UByte,
  minValue: UByte,
  maxValue: UByte,
  onConfirm: (UByte) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY AmpereRow(name=$name)")

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
    if (showSliderDialog) {
      AmpereSliderDialog(
        name = name,
        value = value,
        minValue = minValue,
        maxValue = maxValue,
        onDismiss = {
          Log.v(TAG, "ampereSliderDialog dismissed")
          showSliderDialog = false
          onDismiss()
        },
        onConfirm = { newValue ->
          Log.d(TAG, "ampereSliderDialog confirmed: $newValue")
          showSliderDialog = false
          onConfirm(newValue)
        },
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
  Log.v(TAG, "RETURN AmpereRow()")
}

/**
 * Implements a [Dialog] with a slider, allowing [value] to be changed.
 *
 * @param name The label to place with the [value]
 * @param value The initial value in Ampere to display and possibly change.
 * @param minValue The minimal value allowed for [value] by the slider.
 * @param maxValue The maximum value allowed for [value] by the slider.
 * @param onConfirm Function to be called when changing the [value] is confirmed by the user.
 * @param onDismiss Function to be called when changing the [value] is cancelled by the user.
 * @param modifier the [Modifier] to be applied to this AmpereSliderDialog
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun AmpereSliderDialog(
  name: String,
  value: UByte,
  minValue: UByte,
  maxValue: UByte,
  onConfirm: (UByte) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY AmpereSliderDialog(name=$name)")

  var sliderPosition by remember { mutableFloatStateOf(value.toFloat()) }
  Dialog(onDismissRequest = { onDismiss() }) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
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
            Log.v(TAG, "ampereSliderDialog dismissed")
            onDismiss()
          }) {
            Text(stringResource(R.string.cancel))
          }
          Spacer(Modifier.weight(1f))
          Button(onClick = {
            val newValue = sliderPosition.toUInt().toUByte()
            Log.d(TAG, "ampereSliderDialog confirmed $newValue")
            onConfirm(newValue)
          }) {
            Text(stringResource(R.string.ok))
          }
          Spacer(Modifier.weight(1f))
        }
      }
    }
  }
  Log.v(TAG, "RETURN AmpereSliderDialog()")
}

/**
 * Implements a [Dialog] with a [TimePicker] displaying the [touTime],
 * allowing to change the displayed [TouTime] .
 * @param title The label to display next to the [touTime] information.
 * @param touTime The initial time to display.
 * @param onDismiss Function to be called when changing the [TouTime] is cancelled by the user.
 * @param onConfirm Function to be called when changing the [TouTime] is confirmed by the user.
 * @param modifier the [Modifier] to be applied to this DialTimePickerDialog
 *
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialTimePickerDialog(
  title: String,
  touTime: TouTime,
  onConfirm: (TouTime) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY DialTimePickerDialog(title=$title)")

  val timePickerState = rememberTimePickerState(
    initialHour = touTime.getHours(),
    initialMinute = touTime.getMinutes(),
    is24Hour = true,
  )
  Dialog(onDismissRequest = { onDismiss() }) {
    Card(
      modifier = modifier
        .width(IntrinsicSize.Min)
        .height(IntrinsicSize.Min),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
            Log.v(TAG, "DialTimePickerDialog() dismissed")
            onDismiss()
          }) {
            Text(stringResource(R.string.cancel))
          }
          Spacer(Modifier.weight(1f))
          Button(onClick = {
            val newTouTime = TouTime(timePickerState)
            Log.d(TAG, "DialTimePickerDialog() confirmed $newTouTime")
            onConfirm(newTouTime)
          }) {
            Text(stringResource(R.string.ok))
          }
          Spacer(Modifier.weight(1f))
        }
      }
    }
  }
  Log.v(TAG, "RETURN DialTimePickerDialog()")
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceDetailsHomeCompletePreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceDetailsHomeCompletePreviewLight")
@Composable
private fun DeviceDetailsHomeCompletePreview() {
  GaaiTheme(dynamicColor = false) {
    Surface(
      modifier = Modifier.fillMaxSize()
    ) {
      DeviceDetailsScreenNoViewModel(
        device = Device(
          pn = "12345-A2", sn = "6789-12345-E3", mac = "FA:CA:DE:12:34:56", serviceDataValue = 0x12345678,
          type = ChargerType.HOME
        ),
        state = DeviceDetailsViewState(
          deviceName = "HOME2_",
          deviceState = DeviceState(ConnectionState.CONNECTED),
          deviceInformation = DeviceInformation(
            modelNumber = "12345", serialNumber = "12345",
            firmwareRevision = "1.23.4", hardwareRevision = "A2"
          ),
          chargingBasicData = ChargingBasicData(
            seconds = 123u, discriminator = Discriminator.STOPPED,
            status = Status.PLUGGED, energy = 1234u, phaseCount = 2u
          ),
          chargingGridData = ChargingGridData(
            timestamp = 0x662D0EFBu,
            l1 = 1,
            l2 = 2,
            l3 = -1,
            consumed = 12345,
            interval = 345u
          ),
          chargingCarData = ChargingCarData(
            timestamp = 0x662D0EFBu,
            l1 = 1,
            l2 = 2,
            l3 = -1,
            p1 = 1111,
            p2 = 22222,
            p3 = 3333
          ),
          chargingAdvancedData = ChargingAdvancedData(
            timestamp = 0x662D0EFBu, iAvailable = 6, gridPower = 34, carPower = 32,
            authorizationStatus = AuthorizationStatus(0), errorCode = 0
          )
        ),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {},
        onTimeGet = {},
        onTimeSync = {},
        onLoaderOperation = {},
        navigateToBadgeList = { },
        onNavigateUp = { },
        canNavigateUp = true,
        navigateUp = { }
      )
    }
  }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceDetailsMobilePreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceDetailsMobilePreviewLight")
@Composable
private fun DeviceDetailsMobilePreview() {
  GaaiTheme(dynamicColor = false) {
    Surface(
      modifier = Modifier.fillMaxSize()
    ) {

      DeviceDetailsBody(
        device = Device(
          pn = "12345-A2", sn = "6789-12345-E3", mac = "FA:CA:DE:12:34:56", serviceDataValue = 0x12345678,
          type = ChargerType.MOBILE
        ),
        state = DeviceDetailsViewState(
          deviceName = "Mobile2_",
          deviceState = DeviceState(ConnectionState.CONNECTED),
          deviceInformation = DeviceInformation(
            modelNumber = "12345", serialNumber = "12345",
            firmwareRevision = "1.23.4", hardwareRevision = "A2"
          ),
          chargingBasicData = ChargingBasicData(
            seconds = 123u, discriminator = Discriminator.STOPPED,
            status = Status.PLUGGED, energy = 1234u, phaseCount = 2u
          ),
          chargingGridData = ChargingGridData(
            timestamp = 0x662D0EFBu,
            l1 = 1,
            l2 = 2,
            l3 = -1,
            consumed = 12345,
            interval = 345u
          ),
          chargingCarData = ChargingCarData(
            timestamp = 0x662D0EFBu,
            l1 = 1,
            l2 = 2,
            l3 = -1,
            p1 = 1111,
            p2 = 22222,
            p3 = 3333
          ),
          chargingAdvancedData = ChargingAdvancedData(
            timestamp = 0x662D0EFBu, iAvailable = 6, gridPower = 34, carPower = 32,
            authorizationStatus = AuthorizationStatus(0), errorCode = 0
          )
        ),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {},
        onTimeGet = {},
        onTimeSync = {},
        onLoaderOperation = {},
        navigateToBadgeList = { }
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceInformationPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceInformationPreviewLight")
@Composable
private fun DeviceInformationPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiDeviceInformationCard(
        deviceInformation = DeviceInformation(
          modelNumber = "12345", serialNumber = "67890",
          firmwareRevision = "1.23.4", hardwareRevision = "A1"
        ),
        modifier = Modifier
          .padding(
            dimensionResource(id = R.dimen.padding_small)
          )
      )
    }
  }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "ChargingBasicDataPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "ChargingBasicDataPreviewLight")
@Composable
private fun ChargingBasicDataPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiChargingBasicDataCard(
        chargingBasicData = ChargingBasicData(
          seconds = 123u, discriminator = Discriminator.STOPPED,
          status = Status.PLUGGED, energy = 1234u, phaseCount = 2u
        ),
        modifier = Modifier
          .padding(
            dimensionResource(id = R.dimen.padding_small)
          )
      )
    }
  }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "ChargingGridDataPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "ChargingGridDataPreviewLight")
@Composable
private fun ChargingGridDataPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiChargingGridDataCard(
        chargingGridData = ChargingGridData(
          timestamp = 0x662D0EFBu,
          l1 = 1,
          l2 = 2,
          l3 = -1,
          consumed = 123,
          interval = 345u
        ),
        modifier = Modifier
          .padding(
            dimensionResource(id = R.dimen.padding_small)
          )
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "ChargingGridDataPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "ChargingGridDataPreviewLight")
@Composable
private fun GaaiChargingCarDataCardPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiChargingCarDataCard(
        chargingCarData = ChargingCarData(
          timestamp = 0x662D0EFBu,
          l1 = 1,
          l2 = 2,
          l3 = -1,
          p1 = 1111,
          p2 = 22222,
          p3 = 3333
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiChargingAdvancedDataCardPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiChargingAdvancedDataCardPreviewLight")
@Composable
private fun GaaiChargingAdvancedDataCardPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiChargingAdvancedDataCard(
        chargingAdvancedData = ChargingAdvancedData(
          timestamp = 0x662D0EFBu, iAvailable = 6, gridPower = 34, carPower = 32,
          authorizationStatus = AuthorizationStatus(0), errorCode = 0
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiConfigDataCardPreview_1_0Dark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiConfigDataCardPreview_1_0Light")
@Composable
private fun GaaiConfigDataCardPreview_1_0() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiConfigDataCard(
        configData = ConfigData(
          maxGrid = 50U,
          maxDevice = 0U,
          mode = Mode.MAX_PRIVATE,
          safe = 6U,
          networkType = NetWorkType.UNKNOWN,
          touWeekStart = 123,
          touWeekEnd = 456,
          touWeekendStart = 789,
          touWeekendEnd = 333,
          minDevice = 11U,
          iCapacity = 22U,
          configVersion = ConfigVersion.CONFIG_1_0
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {}
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiConfigDataCardPreview_1_1_Mono_TRINDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiConfigDataCardPreview_1_1_Mono_TRINLight")
@Composable
private fun GaaiConfigDataCardPreview_1_1_Mono_TRIN() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiConfigDataCard(
        configData = ConfigData(
          maxGrid = 50U,
          maxDevice = 32U,
          mode = Mode.MAX_PRIVATE,
          safe = 6U,
          networkType = NetWorkType.MONO_TRIN,
          touWeekStart = 123,
          touWeekEnd = 456,
          touWeekendStart = 789,
          touWeekendEnd = 333,
          minDevice = 11U,
          iCapacity = 22U,
          configVersion = ConfigVersion.CONFIG_1_1
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {}
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiConfigDataCardPreview_1_1_TRIDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiConfigDataCardPreview_1_1_TRILight")
@Composable
private fun GaaiConfigDataCardPreview_1_1_TRI() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiConfigDataCard(
        configData = ConfigData(
          maxGrid = 50U,
          maxDevice = 32U,
          mode = Mode.MAX_PRIVATE,
          safe = 6U,
          networkType = NetWorkType.TRI,
          touWeekStart = 123,
          touWeekEnd = 456,
          touWeekendStart = 789,
          touWeekendEnd = 333,
          minDevice = 11U,
          iCapacity = 22U,
          configVersion = ConfigVersion.CONFIG_1_1
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {}
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiConfigDataCardCborPreview_MONO_TRINDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiConfigDataCardCborPreview_MONO_TRINLight")
@Composable
private fun GaaiConfigDataCardCborPreview_MONO_TRIN() {
  GaaiTheme(dynamicColor = false) {
    Surface()
    {
      GaaiConfigDataCard(
        configData = ConfigData(
          maxGrid = 50U,
          maxDevice = 32U,
          mode = Mode.MAX_PRIVATE,
          safe = 6U,
          networkType = NetWorkType.MONO_TRIN,
          touWeekStart = 123,
          touWeekEnd = 456,
          touWeekendStart = 789,
          touWeekendEnd = 333,
          minDevice = 11U,
          iCapacity = 22U,
          configVersion = ConfigVersion.CONFIG_CBOR
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {}
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiConfigDataCardCborPreview_TRIDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiConfigDataCardCborPreview_TRILight")
@Composable
private fun GaaiConfigDataCardCborPreview_TRI() {
  GaaiTheme(dynamicColor = false) {
    Surface()
    {
      GaaiConfigDataCard(
        configData = ConfigData(
          maxGrid = 50U,
          maxDevice = 32U,
          mode = Mode.MAX_PRIVATE,
          safe = 6U,
          networkType = NetWorkType.TRI,
          touWeekStart = 123,
          touWeekEnd = 456,
          touWeekendStart = 789,
          touWeekendEnd = 333,
          minDevice = 11U,
          iCapacity = 22U,
          configVersion = ConfigVersion.CONFIG_CBOR
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {}
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "TouPeriodDialogPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "TouPeriodDialogPreviewLight")
@Composable
private fun TouPeriodDialogPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      TouPeriodDialog(
        title = "Weekdays",
        touPeriod = TouPeriod(720, 1234),
        onDismissRequest = {},
        onConfirmation = {},
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DialTimePickerDialogPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DialTimePickerDialogPreviewLight")
@Composable
private fun DialTimePickerDialogPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DialTimePickerDialog(
        title = "Sample Time Picker Dialog",
        touTime = TouTime(13, 45),
        onDismiss = {},
        onConfirm = {},
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "AmpereSliderDialogPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "AmpereSliderDialogPreviewLight")
@Composable
private fun AmpereSliderDialogPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
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
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "ModeDialogPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "ModeDialogPreviewLight")
@Composable
private fun ModeDialogPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      ModeDialog(
        mode = Mode.ECO_PRIVATE,
        onDismiss = {},
        onConfirm = {},
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "ModeDialogUnknownPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "ModeDialogUnknownPreviewLight")
@Composable
private fun ModeDialogUnknownPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      ModeDialog(
        mode = Mode.UNKNOWN,
        onDismiss = {},
        onConfirm = {},
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiTimeDataCardPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiTimeDataCardPreviewLight")
@Composable
private fun GaaiTimeDataCardPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiTimeDataCard(
        timeData = TimeData(0x662D0EFBu),
        onTimeGet = {},
        onTimeSync = {},
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),

        )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiTimeDataCardTime0PreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiTimeDataCardTime0PreviewLight")
@Composable
private fun GaaiTimeDataCardTime0Preview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiTimeDataCard(
        timeData = TimeData(0u),
        onTimeGet = {},
        onTimeSync = {},
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),

        )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiLoaderCardPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiLoaderCardPreviewLight")
@Composable
private fun GaaiLoaderCardPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiLoaderCard(
        onLoaderOperation = {},
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiBadgesCardPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiBadgesCardPreviewLight")
@Composable
private fun GaaiBadgesCardPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiBadgesCard(
        navigateToBadgeList = {},
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
      )
    }
  }
}

