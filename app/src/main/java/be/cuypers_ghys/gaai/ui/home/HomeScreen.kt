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

package be.cuypers_ghys.gaai.ui.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import be.cuypers_ghys.gaai.BuildConfig
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.ChargerType
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.device.ConnectionState
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.permissions.RequireBluetooth
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.ui.theme.RedA400
import kotlinx.coroutines.launch

// Tag for logging
private const val TAG = "HomeScreen"

/**
 * The [NavigationDestination] information for the [HomeScreen].
 *
 * @author Frank HJ Cuypers
 */
object HomeDestination : NavigationDestination {
  override val route = "home"
  override val titleRes = R.string.app_name
}

/**
 * Implements the screens, including app bars, for displaying all known Nexxtender charger devices,
 * including the case there are none yet, connect to them, delete them,
 * and allows to add new ones based on their SN and PN and to connect to it.
 * @param navigateToDeviceEntry Function to be called when [HomeScreen] wants to add a new device.
 * @param navigateToDeviceDetails Function to be called when [HomeScreen] wants to connect to a known device and show
 *  its details.
 * @param modifier The [Modifier] to be applied to this HomeScreen.
 * @param viewModel The [HomeViewModel] to be associated with this [HomeScreen].
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun HomeScreen(
  navigateToDeviceEntry: () -> Unit,
  navigateToDeviceDetails: (Int) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
  Log.d(TAG, "Entered HomeScreen()")

  val homeUiState by viewModel.homeUiState.collectAsState()

  RequireBluetooth {
    HomeScreenNoViewModel(
      navigateToDeviceEntry, navigateToDeviceDetails, viewModel::removeDevice, homeUiState, modifier
    )
  }
}

/**
 * Implements the screens, including app bars, for displaying all known Nexxtender charger devices,
 * including the case there are none yet, connect to them, delete them,
 * and allows to add new ones based on their SN and PN and to connect to it.
 * @param navigateToDeviceEntry Function to be called when [HomeScreen] wants to add a new device.
 * @param navigateToDeviceDetails Function to be called when [HomeScreen] wants to connect to a known device and show
 *  its details.
 * @param removeDevice Function to be called when [HomeScreen] wants to remove a known device.
 * @param homeUiState
 * @param modifier The [Modifier] to be applied to this HomeScreen.
 *
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenNoViewModel(
  navigateToDeviceEntry: () -> Unit,
  navigateToDeviceDetails: (Int) -> Unit,
  removeDevice: suspend (Device) -> Unit,
  homeUiState: HomeUiState,
  modifier: Modifier = Modifier,
) {
  Log.d(TAG, "Entered HomeScreenNoViewModel()")

  val coroutineScope = rememberCoroutineScope()
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

  Scaffold(
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      GaaiTopAppBar(
        title = stringResource(HomeDestination.titleRes) + " " + BuildConfig.VERSION_NAME,
        canNavigateUp = false,
        scrollBehavior = scrollBehavior
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = navigateToDeviceEntry,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
          .padding(
            end = WindowInsets.safeDrawing.asPaddingValues()
              .calculateEndPadding(LocalLayoutDirection.current)
          )
      ) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = stringResource(R.string.device_entry_title)
        )
      }
    },
  ) { innerPadding ->
    Log.d(TAG, "Entering HomeBody)")
    HomeBody(
      deviceList = homeUiState.deviceList,
      onDeviceClick = navigateToDeviceDetails,
      onDeviceRemove = {
        coroutineScope.launch {
          removeDevice(it)
        }
      },
      modifier = modifier.fillMaxSize(),
      contentPadding = innerPadding,
    )
  }
  Log.d(TAG, "Exiting HomeScreenNoViewModel()")
}

/**
 * Implements the screens for displaying all known Nexxtender charger devices,
 * including the case there are none yet, connect to them, delete them,
 * and allows to add new ones based on their SN and PN.
 * @param deviceList The list of known [Devices][Device].
 * @param onDeviceClick Function to be called when [HomeBody] wants to connect to a known device and show
 *  its details.
 * @param onDeviceRemove Function to be called when [HomeBody] wants to delete a known device from the list.
 * @param modifier The [Modifier] to be applied to this [HomeBody].
 * @param contentPadding Padding value to apply internal.
 *
 * @author Frank HJ Cuypers
 */

@Composable
private fun HomeBody(
  deviceList: List<Device>,
  onDeviceClick: (Int) -> Unit,
  onDeviceRemove: (Device) -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
) {
  Log.d(TAG, "Entered HomeBody()")
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
    if (deviceList.isEmpty()) {
      Log.d(TAG, "HomeBody() deviceList is empty")

      Text(
        text = stringResource(R.string.no_item_description),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(contentPadding),
      )
    } else {
      DevicesList(
        deviceList = deviceList,
        onDeviceClick = { onDeviceClick(it.id) },
        onDeviceRemove = { onDeviceRemove(it) },
        contentPadding = contentPadding,
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
  Log.d(TAG, "Exiting HomeBody()")
}

/**
 * Implements a [LazyColumn] for displaying all known Nexxtender charger devices and
 * connect to them.
 * @param deviceList The list of known [Devices][Device].
 * @param onDeviceClick Function to be called when [HomeBody] wants to connect to a known device and show
 *  its details.
 * @param onDeviceRemove Function to be called when [HomeBody] wants to delete a known device from the list.
 * @param contentPadding Padding value to apply internal.
 * @param modifier The [Modifier] to be applied to this [HomeBody].
 *
 * @author Frank HJ Cuypers
 */
@Composable
private fun DevicesList(
  deviceList: List<Device>,
  onDeviceClick: (Device) -> Unit,
  onDeviceRemove: (Device) -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "Entering DevicesList()")
  LazyColumn(
    modifier = modifier,
    contentPadding = contentPadding
  ) {
    items(items = deviceList, key = { it.id }) { device ->
      GaaiDeviceItem(
        device = device,
        onDeviceClick = onDeviceClick,
        onDeviceRemove = onDeviceRemove,
        modifier = Modifier
          .clickable { onDeviceClick(device) })
    }
  }
  Log.d(TAG, "Exiting DevicesList()")
}

/**
 * Implements a [Card] displaying the details of the [device],
 * connect to it or delete it by swiping the card to the right using a [SwipeToDismissBox].
 * @param device The [Device] to display.
 * @param onDeviceClick Function to be called when [GaaiDeviceItem] wants to connect to a known device and show
 *  its details.
 * @param onDeviceRemove Function to be called when [GaaiDeviceItem] wants to delete a known device from the list.
 * @param modifier the [Modifier] to be applied to this [GaaiDeviceItem]
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun GaaiDeviceItem(
  device: Device,
  onDeviceClick: (Device) -> Unit,
  onDeviceRemove: (Device) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "Entering GaaiDeviceItem()")

  val context = LocalContext.current
  val currentDevice by rememberUpdatedState(device)
  val dismissState = rememberSwipeToDismissBoxState(
    confirmValueChange = {
      when (it) {
        SwipeToDismissBoxValue.StartToEnd -> {
          onDeviceRemove(currentDevice)
          Toast.makeText(
            context,
            context.getString(R.string.device_deleted), Toast.LENGTH_SHORT
          ).show()
        }

        SwipeToDismissBoxValue.EndToStart -> {
          // Disabled in call to SwipeToDismissBox()
        }

        SwipeToDismissBoxValue.Settled -> return@rememberSwipeToDismissBoxState false
      }
      return@rememberSwipeToDismissBoxState true
    },
    // positional threshold of 25%
    positionalThreshold = { it * .25f }
  )
  SwipeToDismissBox(
    state = dismissState,
    enableDismissFromEndToStart = false,
    modifier = modifier,
    backgroundContent = { DismissBackground(dismissState) }
  ) {
    GaaiDeviceCard(
      device, ConnectionState.NOT_CONNECTED, modifier = Modifier
        .padding(dimensionResource(id = R.dimen.padding_small))
        .clickable { onDeviceClick(device) })
  }
  Log.d(TAG, "Exiting GaaiDeviceItem()")
}

/**
 * Converts [ConnectionState] to a string value to display.
 * @param connectionState
 * @return The corresponding string.
 *
 * @author Frank HJ Cuypers
 */
@Composable
private fun connectionStateToText(connectionState: ConnectionState) = when (connectionState) {
  ConnectionState.CONNECTED -> stringResource(R.string.gatt_client_connected_services)
  ConnectionState.NOT_CONNECTED -> stringResource(R.string.gatt_client_not_connected)
  ConnectionState.CONNECTING -> stringResource(R.string.gatt_client_connecting)
  ConnectionState.DISCOVERING -> stringResource(R.string.gatt_client_discovering_services)
  else -> {
    stringResource(R.string.gatt_client_connection_status_unknown)
  }
}

/**
 * Converts [ConnectionState] to a corresponding icon to display.
 * @param connectionState
 * @return The corresponding icon.
 *
 * @author Frank HJ Cuypers
 */
@Composable
private fun connectionStateToPainter(connectionState: ConnectionState) = when (connectionState) {
  ConnectionState.CONNECTED -> painterResource(R.drawable.bluetooth_connected_24px)
  ConnectionState.NOT_CONNECTED -> painterResource(R.drawable.bluetooth_disabled_24px)
  ConnectionState.CONNECTING, ConnectionState.DISCOVERING -> painterResource(R.drawable.bluetooth_searching_24px)
  else -> {
    painterResource(R.drawable.bluetooth_disabled_24px)
  }
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
// TODO: factorize to its own file, since it is also used in DeviceEntryScreen.kt and DeviceDetailScreen.kt
internal fun GaaiDeviceCard(
  device: Device, connectionState: ConnectionState, modifier: Modifier = Modifier
) {
  Log.d(TAG, "Entered GaaiDeviceCard with device = $device")
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
            painter = connectionStateToPainter(connectionState),
            contentDescription = stringResource(id = R.string.ev_charger_content_desc),
            modifier = Modifier.size(MaterialTheme.typography.titleMedium.fontSize.value.dp)
          )
          Text(
            text = connectionStateToText(connectionState),
            style = MaterialTheme.typography.titleMedium,
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth()
        ) {
          Log.d(TAG, "GaaiDeviceCard printing first line")

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
    Log.d(TAG, "exiting GaaiDeviceCard with device = $device")
  }
}

/**
 * A [Row] with the background to show when swiping the [SwipeToDismissBox] displaying the [GaaiDeviceItem]
 * to the right.
 * @param dismissState State if the [SwipeToDismissBox].
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
  val color = when (dismissState.dismissDirection) {
    SwipeToDismissBoxValue.StartToEnd -> RedA400
    SwipeToDismissBoxValue.EndToStart -> RedA400
    SwipeToDismissBoxValue.Settled -> Color.Transparent
  }

  Row(
    modifier = Modifier
      .fillMaxSize()
      .background(color)
      .padding(12.dp, 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Icon(
      Icons.Default.Delete,
      contentDescription = stringResource(R.string.delete)
    )
    Spacer(modifier = Modifier)
  }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyPreview() {
  GaaiTheme {
    HomeBody(
      listOf(
        Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, type = ChargerType.HOME),
        Device(2, "12345-A2", "2222-22222-E3", "FA:CA:DE:22:22:22", 0x22222222, type = ChargerType.MOBILE),
        Device(3, "12345-A2", "3333-33333-E3", "FA:CA:DE:33:33:33", 0x33333333, type = ChargerType.HOME),
      ), onDeviceClick = {}, onDeviceRemove = {})
  }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenNoViewModelPreview() {
  GaaiTheme {
    HomeScreenNoViewModel(
      navigateToDeviceEntry = {}, navigateToDeviceDetails = {}, removeDevice = {}, homeUiState =
        HomeUiState(
          listOf(
            Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, type = ChargerType.HOME),
            Device(2, "12345-A2", "2222-22222-E3", "FA:CA:DE:22:22:22", 0x22222222, type = ChargerType.MOBILE),
            Device(3, "12345-A2", "3333-33333-E3", "FA:CA:DE:33:33:33", 0x33333333, type = ChargerType.HOME),
          )
        )
    )
  }
}

@Preview(showBackground = true)
@Composable
fun HomeBodyEmptyListPreview() {
  GaaiTheme {
    HomeBody(listOf(), onDeviceClick = {}, onDeviceRemove = {})
  }
}

@Preview(showBackground = true)
@Composable
fun DevicePreviewHOME() {
  GaaiTheme {
    GaaiDeviceCard(
      Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, ChargerType.HOME),
      ConnectionState.NOT_CONNECTED
    )
  }
}

@Preview(showBackground = true)
@Composable
fun DevicePreviewMOBILE() {
  GaaiTheme {
    GaaiDeviceCard(
      Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, ChargerType.MOBILE),
      ConnectionState.NOT_CONNECTED
    )
  }
}

@Preview(showBackground = true)
@Composable
fun DevicePreviewUNKNOWN() {
  GaaiTheme {
    GaaiDeviceCard(
      Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, ChargerType.UNKNOWN),
      ConnectionState.NOT_CONNECTED
    )
  }
}
