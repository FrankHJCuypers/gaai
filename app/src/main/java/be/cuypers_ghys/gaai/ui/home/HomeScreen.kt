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

package be.cuypers_ghys.gaai.ui.home

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.ChargerType
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.permissions.RequireBluetooth
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.ui.device.GaaiDeviceCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice

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
  Log.v(TAG, "ENTRY HomeScreen()")

  RequireBluetooth {
    /**
     * viewModel.scanDevice() must be called inside a [RequireBluetooth].
     * But we only want to execute it once,
     * so make sure not to reexecute viewModel.scanDevice() on each state update.
     */
    var scanDeviceCalled by remember { mutableStateOf(false) }
    if ( !scanDeviceCalled) {
      viewModel.scanDevice()
      scanDeviceCalled = true
    }
    val homeUiState by viewModel.homeUiState.collectAsState()
    HomeScreenNoViewModel(
      navigateToDeviceEntry, navigateToDeviceDetails, viewModel::removeDevice, homeUiState, modifier
    )
  }
  Log.v(TAG, "RETURN HomeScreen()")
}

/**
 * Implements the screens, including app bars, for displaying all known Nexxtender charger devices,
 * including the case there are none yet, connect to them, delete them,
 * and allows to add new ones based on their SN and PN and to connect to it.
 * @param navigateToDeviceEntry Function to be called when [HomeScreen] wants to add a new device.
 * @param navigateToDeviceDetails Function to be called when [HomeScreen] wants to connect to a known device and show
 *  its details.
 * @param removeDevice Function to be called when [HomeScreen] wants to remove a known device.
 * @param homeUiState The[HomeUiState].
 * @param modifier The [Modifier] to be applied to this HomeScreen.
 *
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenNoViewModel(
  navigateToDeviceEntry: () -> Unit,
  navigateToDeviceDetails: (Int) -> Unit,
  removeDevice: (Device) -> Unit,
  homeUiState: HomeUiState,
  modifier: Modifier = Modifier,
) {
  Log.v(TAG, "ENTRY HomeScreenNoViewModel()")

  rememberCoroutineScope()
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

  Scaffold(
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      GaaiTopAppBar(
        title = stringResource(HomeDestination.titleRes),
        canNavigateUp = false,
        scrollBehavior = scrollBehavior, actions = { DropdownMenuWithDetails() }
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
    HomeBody(
      homeUiState = homeUiState,
      onDeviceClick = navigateToDeviceDetails,
      onDeviceRemove = {
        removeDevice(it)
      },
      modifier = modifier.fillMaxSize(),
      contentPadding = innerPadding,
    )
  }
  Log.v(TAG, "RETURN HomeScreenNoViewModel()")
}

/**
 * Implements the screens for displaying all known Nexxtender charger devices,
 * including the case there are none yet, connect to them, delete them,
 * and allows to add new ones based on their SN and PN.
 * @param homeUiState The[HomeUiState].
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
  homeUiState: HomeUiState,
  onDeviceClick: (Int) -> Unit,
  onDeviceRemove: (Device) -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
) {
  Log.v(TAG, "ENTRY HomeBody()")
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
    if (homeUiState.deviceList.isEmpty()) {
      Log.v(TAG, "HomeBody() deviceList is empty")

      Text(
        text = stringResource(R.string.no_item_description),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(contentPadding),
      )
    } else {
      DevicesList(
        homeUiState = homeUiState,
        onDeviceClick = { onDeviceClick(it.id) },
        onDeviceRemove = { onDeviceRemove(it) },
        contentPadding = contentPadding,
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
  Log.v(TAG, "RETURN HomeBody()")
}

/**
 * Implements a [LazyColumn] for displaying all known Nexxtender charger devices and
 * connect to them.
 * @param homeUiState The[HomeUiState].
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
  homeUiState: HomeUiState,
  onDeviceClick: (Device) -> Unit,
  onDeviceRemove: (Device) -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier
) {
  Log.v(TAG, "Entering DevicesList()")
  LazyColumn(
    modifier = modifier,
    contentPadding = contentPadding
  ) {
    items(items = homeUiState.deviceList, key = { it.id }) { device ->
      val isAdvertising = HomeViewModel.isAdvertising(device, homeUiState.advertisingDeviceList)
      GaaiDeviceItem(
        device = device,
        isAdvertising = isAdvertising,
        onDeviceClick = onDeviceClick,
        onDeviceRemove = onDeviceRemove,
        modifier = Modifier
          .clickable { onDeviceClick(device) })
    }
  }
  Log.v(TAG, "RETURN DevicesList()")
}

/**
 * Implements a [Card] displaying the details of the [device],
 * connect to it or delete it by swiping the card to the right using a [SwipeToDismissBox].
 * @param device The [Device] to display.
 * @param Is the [device] advertising itself?
 * @param onDeviceClick Function to be called when [GaaiDeviceItem] wants to connect to a known device and show
 *  its details.
 * @param onDeviceRemove Function to be called when [GaaiDeviceItem] wants to delete a known device from the list.
 * @param modifier the [Modifier] to be applied to this [GaaiDeviceItem]
 *
 * @author Frank HJ Cuypers
 */
// Delete confirmation:
// see Answer from https://stackoverflow.com/questions/78638403/reset-of-swipetodismissboxstate-not-working
@Composable
fun GaaiDeviceItem(
  device: Device,
  isAdvertising: Boolean,
  onDeviceClick: (Device) -> Unit,
  onDeviceRemove: (Device) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.v(TAG, "Entering GaaiDeviceItem()")
  val scope = rememberCoroutineScope()
  SwipeToDismissContainer(
    device,
    stringResource(R.string.device),
    onDismiss = { _, onError ->
      scope.launch {
        delay(1000)
        onError()
        onDeviceRemove(device)
      }
    }
  ) {
    GaaiDeviceCard(
      device, isAdvertising, modifier = Modifier
        .padding(dimensionResource(id = R.dimen.padding_small))
        .clickable { onDeviceClick(device) })
  }
  Log.v(TAG, "RETURN GaaiDeviceItem()")
}



@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "HomeBodyPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "HomeBodyPreviewLight")
@Composable
fun HomeBodyPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      HomeBody(
        HomeUiState(
          listOf(
            Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, type = ChargerType.HOME),
            Device(2, "12345-A2", "2222-22222-E3", "FA:CA:DE:22:22:22", 0x22222222, type = ChargerType.MOBILE),
            Device(3, "12345-A2", "3333-33333-E3", "FA:CA:DE:33:33:33", 0x33333333, type = ChargerType.HOME),
          ),
          advertisingDeviceList = listOf(
            MockServerDevice("A1", "FA:CA:DE:22:22:22"),
            MockServerDevice("B2", "22:22:22:22:22:22"),
          )
        ), onDeviceClick = {}, onDeviceRemove = {})
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "HomeScreenNoViewModelPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "HomeScreenNoViewModelPreviewLight")
@Composable
fun HomeScreenNoViewModelPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface(modifier = Modifier.fillMaxSize()) {
      HomeScreenNoViewModel(
        navigateToDeviceEntry = {}, navigateToDeviceDetails = {}, removeDevice = {}, homeUiState =
          HomeUiState(
            listOf(
              Device(1, "12345-A2", "6789-12345-E3", "FA:CA:DE:12:34:56", 0x12345678, type = ChargerType.HOME),
              Device(2, "12345-A2", "2222-22222-E3", "FA:CA:DE:22:22:22", 0x22222222, type = ChargerType.MOBILE),
              Device(3, "12345-A2", "3333-33333-E3", "FA:CA:DE:33:33:33", 0x33333333, type = ChargerType.HOME),
            ),
            advertisingDeviceList = listOf(
              MockServerDevice("A1", "FA:CA:DE:22:22:22"),
              MockServerDevice("B2", "22:22:22:22:22:22"),
            )
          )
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "HomeScreenNoViewModelPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "HomeScreenNoViewModelPreviewLight")
@Composable
fun HomeScreenEmptyListNoViewModelPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface(modifier = Modifier.fillMaxSize()) {
      HomeScreenNoViewModel(
        navigateToDeviceEntry = {}, navigateToDeviceDetails = {}, removeDevice = {}, homeUiState =
          HomeUiState(
            listOf()
          )
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "HomeBodyEmptyListPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "HomeBodyEmptyListPreviewLight")
@Composable
fun HomeBodyEmptyListPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      HomeBody(HomeUiState(listOf()), onDeviceClick = {}, onDeviceRemove = {})
    }
  }
}

