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
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
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
import be.cuypers_ghys.gaai.data.AuthorizationStatus
import be.cuypers_ghys.gaai.data.ChargerType
import be.cuypers_ghys.gaai.data.ChargingAdvancedData
import be.cuypers_ghys.gaai.data.ChargingBasicData
import be.cuypers_ghys.gaai.data.ChargingCarData
import be.cuypers_ghys.gaai.data.ChargingGridData
import be.cuypers_ghys.gaai.data.ConfigData
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.Discriminator
import be.cuypers_ghys.gaai.data.Mode
import be.cuypers_ghys.gaai.data.Status
import be.cuypers_ghys.gaai.data.TimeData
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.permissions.RequireBluetooth
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.util.TouPeriod
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus

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
      onTimeGet = viewModel::getTime,
      onTimeSync = viewModel::syncTime,
      onLoaderOperation = viewModel::sendLoaderOperation,
      navigateToBadgeList = { navigateToBadgeList(deviceId) },
      onRecordsSync = viewModel::startRecordsSync
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
 * @param onRecordsSync Function to be called when [DeviceDetailsBody] wants to sync records.
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
  onRecordsSync: (Uri) -> Unit,
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
      onRecordsSync = onRecordsSync,
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
 * @param onRecordsSync Function to be called when [DeviceDetailsBody] wants to sync records.
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
  onRecordsSync: (Uri) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY DeviceDetailsBody(device = $device)")

  Column(
    modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large))
  ) {
    device?.let {
      GaaiDeviceCardDeviceDetails(
        device = it,
        gattConnectionStateWithStatus = state.gattConnectionStateWithStatus,
        bondState = state.bondState,
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }

    if (state.gattConnectionStateWithStatus.status == BleGattConnectionStatus.SUCCESS) {
      GaaiDeviceNameCard(
        deviceName = state.deviceName,
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )

      GaaiDeviceInformationCard(
        deviceInformation = state.deviceInformation,
        chargerType = device?.type!!,
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )

      GaaiChargingBasicDataCard(
        chargingBasicData = state.chargingBasicData,
        chargerType = device.type,
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )

      if (device.type == ChargerType.HOME) {
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
      }

      if ((device.type == ChargerType.HOME) || state.supportsDateUtc) {
        GaaiTimeDataCard(
          timeData = state.timeData,
          onTimeGet = onTimeGet,
          onTimeSync = onTimeSync,
          modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_small))
        )
      }

      if (device.type == ChargerType.HOME) {
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

      GaaiDataRecordsCard(
        dataRecordsInformation = state.dataRecordsInformation,
        onRecordsSync = onRecordsSync,
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )

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
          gattConnectionStateWithStatus = GattConnectionStateWithStatus(
            GattConnectionState.STATE_CONNECTED,
            BleGattConnectionStatus.SUCCESS
          ),
          deviceInformation = DeviceInformation(
            modelNumber = "12345", serialNumber = "12345",
            firmwareRevision = "1.23.4", hardwareRevision = "A2", manufacturerName = "?"
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
        navigateUp = { },
        onRecordsSync = { }
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
          type = ChargerType.MOBILE_BLACK
        ),
        state = DeviceDetailsViewState(
          deviceName = "Mobile2_",
          gattConnectionStateWithStatus = GattConnectionStateWithStatus(
            GattConnectionState.STATE_CONNECTED,
            BleGattConnectionStatus.SUCCESS
          ), deviceInformation = DeviceInformation(
            modelNumber = "12345", serialNumber = "12345",
            firmwareRevision = "1.23.4", hardwareRevision = "A2", manufacturerName = "Powerdale"
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
        onRecordsSync = { }
      )
    }
  }
}