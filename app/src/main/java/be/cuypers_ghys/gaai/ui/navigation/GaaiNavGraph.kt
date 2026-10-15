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

package be.cuypers_ghys.gaai.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.device.BadgeListDestination
import be.cuypers_ghys.gaai.ui.device.BadgeListScreen
import be.cuypers_ghys.gaai.ui.device.DeviceDetailsDestination
import be.cuypers_ghys.gaai.ui.device.DeviceDetailsScreen
import be.cuypers_ghys.gaai.ui.device.DeviceDetailsViewModel
import be.cuypers_ghys.gaai.ui.device.DeviceEntryDestination
import be.cuypers_ghys.gaai.ui.device.DeviceEntryScreen
import be.cuypers_ghys.gaai.ui.home.HomeDestination
import be.cuypers_ghys.gaai.ui.home.HomeScreen

// Tag for logging
private const val TAG = "GaaiNavGraph"

/**
 * Provides the Navigation graph for the application.
 * @param navController The [NavHostController] for this host.
 * @param modifier The [Modifier] to be applied to this GaaiNavHost.
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun GaaiNavHost(
  navController: NavHostController,
  modifier: Modifier = Modifier,
) {
  Log.d(TAG, "ENTRY GaaiNavHost(navController = $navController)")


  NavHost(
    navController = navController,
    startDestination = HomeDestination.route,
    modifier = modifier
  ) {
    Log.d(TAG, "ENTRY NavHost lambda")

    composable(route = HomeDestination.route) {
      Log.d(TAG, "ENTRY composable HomeDestination")
      HomeScreen(
        navigateToDeviceEntry = {
          Log.d(TAG, "ENTRY HomeScreen navigateToDeviceEntry $DeviceEntryDestination.route")
          navController.navigate(DeviceEntryDestination.route)
          Log.v(TAG, "EXIT HomeScreen")
        },
        navigateToDeviceDetails = {
          Log.d(TAG, "ENTRY HomeScreen navigateToDeviceDetails ${DeviceDetailsDestination.route}/${it}")
          navController.navigate("${DeviceDetailsDestination.route}/${it}")
          Log.v(TAG, "EXIT HomeScreen navigateToDeviceDetails")
        }
      )
      Log.v(TAG, "EXIT composable HomeDestination")
    }

    composable(route = DeviceEntryDestination.route) {
      Log.d(TAG, "ENTRY composable DeviceEntryDestination")
      DeviceEntryScreen(
        navigateBack = {
          Log.d(TAG, "ENTRY DeviceEntryScreen navigateBack")
          navController.popBackStack()
          Log.v(TAG, "EXIT DeviceEntryScreen navigateBack")
        },
        onNavigateUp = {
          Log.d(TAG, "ENTRY DeviceEntryScreen onNavigateUp")
          navController.navigateUp()
          Log.v(TAG, "EXIT DeviceEntryScreen onNavigateUp")
        }
      )
      Log.v(TAG, "EXIT composable DeviceEntryDestination")
    }

    composable(
      route = DeviceDetailsDestination.routeWithArgs,
      arguments = listOf(navArgument(DeviceDetailsDestination.DEVICE_ID_ARG) {
        type = NavType.IntType
      })
    ) { backStackEntry ->
      Log.d(TAG, "ENTRY composable DeviceDetailsDestination")
      val viewModel =
        viewModel<DeviceDetailsViewModel>(viewModelStoreOwner = backStackEntry, factory = AppViewModelProvider.Factory)
      Log.i(TAG, "EXIT composable DeviceDetailsDestination; viewmodel created")
      DeviceDetailsScreen(
        onNavigateUp = {
          Log.d(TAG, "ENTRY DeviceDetailsScreen onNavigateUp")
          navController.navigateUp()
          Log.v(TAG, "EXIT DeviceDetailsScreen onNavigateUp")
        },
        navigateToBadgeList = {
          Log.d(TAG, "ENTRY DeviceDetailsScreen navigateToBadgeList ${BadgeListDestination.route}/${it}")
          navController.navigate("${BadgeListDestination.route}/$it")
          Log.v(TAG, "EXIT DeviceDetailsScreen navigateToBadgeList")
        },
        viewModel = viewModel
      )
      Log.v(TAG, "EXIT composable DeviceDetailsDestination")
    }

    composable(
      route = BadgeListDestination.routeWithArgs,
      arguments = listOf(navArgument(DeviceDetailsDestination.DEVICE_ID_ARG) {
        type = NavType.IntType
      })

    ) { backStackEntry ->
      Log.d(TAG, "ENTRY composable BadgeListDestination")
      val deviceId = backStackEntry.arguments?.getInt(BadgeListDestination.DEVICE_ID_ARG)
      val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry("${DeviceDetailsDestination.route}/$deviceId")
      }
      val viewModel = viewModel<DeviceDetailsViewModel>(
        viewModelStoreOwner = parentEntry,
        factory = AppViewModelProvider.Factory
      )
      Log.i(TAG, "EXIT composable BadgeListDestination; viewmodel retrieved")

      BadgeListScreen(
        onNavigateUp = {
          Log.d(TAG, "ENTRY BadgeListScreen onNavigateUp")
          navController.navigateUp()
          Log.v(TAG, "EXIT BadgeListScreen onNavigateUp")
        },
        viewModel = viewModel
      )
      Log.v(TAG, "EXIT composable BadgeListDestination")
    }
    Log.v(TAG, "RETURN NavHost lambda")
  }
  Log.v(TAG, "RETURN GaaiNavHost()")
}
