/*
 * Project Gaai: one app to control the Nexxtender Home charger.
 * Copyright © 2025, Frank HJ Cuypers
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
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.Badge
import be.cuypers_ghys.gaai.data.ChargeType
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_STATUS_WAIT_ADDED
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_STATUS_WAIT_EXISTS
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.home.DismissBackground
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.permissions.RequireBluetooth
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.util.toColonHex
import kotlinx.coroutines.launch

// Tag for logging
private const val TAG = "BadgeListScreen"

/**
 * The [NavigationDestination] information for the [DeviceDetailsScreen].
 *
 * @author Frank HJ Cuypers
 */
object BadgeListDestination : NavigationDestination {
  override val route = "badge_list"
  override val titleRes = R.string.badge_list
  const val DEVICE_ID_ARG = "deviceId"
  val routeWithArgs = "$route/{$DEVICE_ID_ARG}"
}

/**
 * Implements the screens, including app bars, for displaying all known badges,
 * including the case there are none yet, delete them,
 * and allows to add new ones.
 * @param onNavigateUp Function to be called when [BadgeListScreen] wants to navigate up.
 * @param modifier The [Modifier] to be applied to this HomeScreen.
 * @param viewModel The [BadgeListViewModel] to be associated with this [BadgeListScreen].
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun BadgeListScreen(
  onNavigateUp: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: BadgeListViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
  Log.d(TAG, "Entered BadgeListScreen()")

  val badgeListUiState by viewModel.badgeListUiState.collectAsState()

  RequireBluetooth {
    BadgeListScreenNoViewModel(
      onNavigateUp,
      viewModel::addBadge,
      viewModel::deleteBadge,
      badgeListUiState,
      viewModel.badgeDeviceUiState,
      modifier
    )
  }
  Log.d(TAG, "Exiting BadgeListScreen()")
}

/**
 * Implements the screens, including app bars, for displaying all known badges,
 * including the case there are none yet, delete them,
 * and allows to add new ones.
 * @param onNavigateUp Function to be called when [BadgeListScreenNoViewModel] wants to navigate up.
 * @param addBadge Function to be called when [BadgeListScreenNoViewModel] wants to add a new badge.
 * @param deleteBadge Function to be called when [BadgeListScreenNoViewModel] wants to delete a new badge.
 * @param badgeListUiState
 * @param badgeDeviceUiState The device state determined by the [BadgeListViewModel].
 * @param modifier The [Modifier] to be applied to this [BadgeListScreenNoViewModel].
 *
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeListScreenNoViewModel(
  onNavigateUp: () -> Unit,
  addBadge: (ChargeType) -> Unit,
  deleteBadge: suspend (Badge) -> Unit,
  badgeListUiState: BadgeListUiState,
  badgeDeviceUiState: BadgeDeviceUiState,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "Entered BadgeListScreenNoViewModel()")

  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState)
    },
    modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      GaaiTopAppBar(
        title = stringResource(BadgeListDestination.titleRes),
        canNavigateUp = true,
        navigateUp = onNavigateUp,
        scrollBehavior = scrollBehavior
      )
    },
    floatingActionButton = {
      Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Spacer(Modifier.weight(1f))
        Column(
          modifier = modifier
        ) {
          val msg = stringResource(R.string.present_badge_to_add_in_max_mode)
          ExtendedFloatingActionButton(
            onClick = {
              addBadge(ChargeType.MAX)
              coroutineScope.launch {
                snackbarHostState.showSnackbar(msg)
              }
            },
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
            Text(text = "MAX")
          }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
          modifier = modifier
        ) {
          val msg = stringResource(R.string.present_badge_to_add_in_default_mode)
          ExtendedFloatingActionButton(
            onClick = {
              addBadge(ChargeType.DEFAULT)
              coroutineScope.launch {
                snackbarHostState.showSnackbar(msg)
              }
            },
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
            Text(text = "DEFAULT")
          }
        }
      }
    },
  ) { innerPadding ->
    Log.d(TAG, "Entering BadgeListBody")
    when (badgeDeviceUiState.statusId) {
      BADGE_STATUS_WAIT_EXISTS -> Toast.makeText(
        context,
        context.getString(R.string.badge_already_registered), Toast.LENGTH_SHORT
      ).show()

      BADGE_STATUS_WAIT_ADDED -> Toast.makeText(
        context,
        context.getString(R.string.badge_added), Toast.LENGTH_SHORT
      ).show()
    }

    BadgeListBody(
      badgeList = badgeListUiState.badgeList,
      onBadgeRemove = deleteBadge,
      modifier = modifier.fillMaxSize(),
      contentPadding = innerPadding,
    )
  }
  Log.d(TAG, "Exiting BadgeListScreenNoViewModel()")
}

/**
 * Implements the screens for displaying all known badges,
 * including the case there are none yet, delete them,
 * and allows to add new ones.
 * @param badgeList The list of known [Badges][Badge].
 * @param onBadgeRemove Function to be called when [BadgeListScreenNoViewModel] wants to delete a known [Badge].
 * @param modifier The [Modifier] to be applied to this [BadgeListScreenNoViewModel].
 * @param contentPadding Padding value to apply internal.
 *
 * @author Frank HJ Cuypers
 */
@Composable
private fun BadgeListBody(
  badgeList: List<Badge>,
  onBadgeRemove: suspend (Badge) -> Unit,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(0.dp),
) {
  Log.d(TAG, "Entered BadgeListBody()")
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier,
  ) {
    if (badgeList.isEmpty()) {
      Log.d(TAG, "BadgeListBody() badgeList is empty")

      Text(
        text = stringResource(R.string.oops_no_badges_registered_yet_tap_to_add),
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(contentPadding),
      )
    } else {
      BadgesList(
        badgeList = badgeList,
        onBadgeRemove = onBadgeRemove,
        contentPadding = contentPadding,
        modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
  Log.d(TAG, "Exiting BadgeListBody()")
}

/**
 * Implements a [LazyColumn] for displaying all known badges.
 * @param badgeList The list of known [Badges][Badge].
 * @param onBadgeRemove Function to be called when [BadgeListScreenNoViewModel] wants to delete  a known [Badge].
 * @param contentPadding Padding value to apply internal.
 * @param modifier The [Modifier] to be applied to this [BadgesList].
 *
 * @author Frank HJ Cuypers
 *
 */
@Composable
private fun BadgesList(
  badgeList: List<Badge>,
  onBadgeRemove: suspend (Badge) -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "Entering BadgesList()")
  LazyColumn(
    modifier = modifier,
    contentPadding = contentPadding
  ) {
    items(items = badgeList) { badge ->
      GaaiBadgeItem(
        badge = badge,
        onBadgeRemove = onBadgeRemove,
        modifier = modifier
      )
    }
  }
  Log.d(TAG, "Exiting BadgesList()")
}

/**
 * Implements a [Card] displaying the details of the [badge] and delete it by swiping the card to the right using
 * a [SwipeToDismissBox].
 * @param badge The [Badge] to display.
 * @param onBadgeRemove Function to be called when [GaaiBadgeItem] wants to delete a known device from the list.
 * @param modifier the [Modifier] to be applied to this [GaaiBadgeItem]
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun GaaiBadgeItem(
  badge: Badge,
  onBadgeRemove: suspend (Badge) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "Entering GaaiBadgeItem()")

  val coroutineScope = rememberCoroutineScope()

  val context = LocalContext.current
  val currentBadge by rememberUpdatedState(badge)
  val dismissState = rememberSwipeToDismissBoxState(
    confirmValueChange = {
      // TODO: for some reason, the log shows that this is called twice after a swipe StartToEnd,
      //  resulting in onBadgeRemove() called twice. Needs more investigation.
      Log.d(TAG, "GaaiBadgeItem(): SwipeToDismissBoxValue: $it")
      when (it) {
        SwipeToDismissBoxValue.StartToEnd -> {
          coroutineScope.launch {
            onBadgeRemove(currentBadge)
          }
          Toast.makeText(
            context,
            context.getString(R.string.badge_deleted), Toast.LENGTH_SHORT
          ).show()
          true
        }

        else -> false
      }
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
    GaaiBadgeCard(
      badge, modifier = Modifier
        .padding(dimensionResource(id = R.dimen.padding_small))
    )
  }
  Log.d(TAG, "Exiting GaaiBadgeItem()")
}

/**
 * Implements a [Card] displaying the details of the [badge].
 * @param badge The [Badge] to display.
 * @param modifier The [Modifier] to be applied to this [GaaiBadgeCard]
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiBadgeCard(
  badge: Badge, modifier: Modifier = Modifier
) {
  Log.d(TAG, "Entered GaaiBadgeCard with device = $badge")
  Card(
    modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        modifier = Modifier.fillMaxWidth()
      ) {
        Log.d(TAG, "GaaiBadgeCard printing first line")
        Icon(
          painter = painterResource(R.drawable.contactless_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
          contentDescription = stringResource(id = R.string.ev_charger_content_desc)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
          text = badge.uuid.toColonHex(),
          style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.weight(1f))
        Text(
          text = badge.chargeType.toString(),
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }
  }
  Log.d(TAG, "exiting GaaiBadgeCard with badge = $badge")
}

@OptIn(ExperimentalStdlibApi::class)
@Preview(showBackground = true)
@Composable
fun BadgeListBodyPreview() {
  GaaiTheme {
    BadgeListBody(
      listOf(
        Badge("11223344556677".hexToByteArray(), ChargeType.DEFAULT),
        Badge("11223344".hexToByteArray(), ChargeType.MAX),
        Badge("112233445566778899AA".hexToByteArray(), ChargeType.UNKNOWN),
      ), onBadgeRemove = {})
  }
}


@OptIn(ExperimentalStdlibApi::class)
@Preview(showBackground = true)
@Composable
fun BadgeListScreenNoViewModelPreview() {
  GaaiTheme {
    BadgeListScreenNoViewModel(
      onNavigateUp = {}, addBadge = {}, deleteBadge = {}, badgeListUiState = BadgeListUiState(
        listOf(
          Badge("11223344556677".hexToByteArray(), ChargeType.DEFAULT),
          Badge("11223344".hexToByteArray(), ChargeType.MAX),
          Badge("112233445566778899AA".hexToByteArray(), ChargeType.UNKNOWN),
        )
      ), badgeDeviceUiState = BadgeDeviceUiState()
    )
  }
}

@Preview(showBackground = true)
@Composable
fun BadgeListScreenNoViewModelEmptyListPreview() {
  GaaiTheme {
    BadgeListScreenNoViewModel(
      onNavigateUp = {}, addBadge = {}, deleteBadge = {}, badgeListUiState = BadgeListUiState(
        listOf()
      ), badgeDeviceUiState = BadgeDeviceUiState()
    )
  }
}
