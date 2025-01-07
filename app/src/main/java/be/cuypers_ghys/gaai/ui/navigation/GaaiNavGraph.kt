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

package be.cuypers_ghys.gaai.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import be.cuypers_ghys.gaai.ui.device.DeviceDetailsDestination
import be.cuypers_ghys.gaai.ui.device.DeviceDetailsScreen
import be.cuypers_ghys.gaai.ui.device.DeviceEntryDestination
import be.cuypers_ghys.gaai.ui.device.DeviceEntryScreen
import be.cuypers_ghys.gaai.ui.home.HomeDestination
import be.cuypers_ghys.gaai.ui.home.HomeScreen
import be.cuypers_ghys.gaai.ui.permissions.MissingPermissionsDestination
import be.cuypers_ghys.gaai.ui.permissions.MissingPermissionsScreen

// Tag for logging
private const val TAG = "GaaiNavGraph"

/**
 * Provides the Navigation graph for the application.
 * @param navController The []NavHostController] for this host.
 * @param modifier The [Modifier] to be applied to this GaaiNavHost.
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun GaaiNavHost(
  navController: NavHostController,
  modifier: Modifier = Modifier,
) {
  Log.d(TAG, "Entered GaaiNavHost with navController = ${navController.toString()}")

  NavHost(
    navController = navController,
//    startDestination = HomeDestination.route,
    startDestination = MissingPermissionsDestination.route,
    modifier = modifier
  ) {
    composable(route = HomeDestination.route) {
      Log.d(TAG, "Composable ${HomeDestination.route} is calling HomeScreen()")
      HomeScreen(
        navigateToDeviceEntry = { navController.navigate(DeviceEntryDestination.route) },
        navigateToDeviceDetails = {
          navController.navigate("${DeviceDetailsDestination.route}/${it}")
        }
      )
      Log.d(TAG, "Composable ${HomeDestination.route} has exited HomeScreen()")
    }

    composable(route = DeviceEntryDestination.route) {
      Log.d(TAG, "Composable ${DeviceEntryDestination.route} is calling DeviceEntryScreen()")
      DeviceEntryScreen(
        navigateBack = { navController.popBackStack() },
        onNavigateUp = { navController.navigateUp() }
      )
      Log.d(TAG, "Composable ${DeviceEntryDestination.route} has exited DeviceEntryScreen()")
    }

    composable(
      route = DeviceDetailsDestination.routeWithArgs,
      arguments = listOf(navArgument(DeviceDetailsDestination.DEVICE_ID_ARG) {
        type = NavType.IntType
      })
    ) {
      Log.d(TAG, "Composable ${DeviceDetailsDestination.routeWithArgs} is calling DeviceDetailsScreen()")
      DeviceDetailsScreen(
        navigateBack = { navController.popBackStack() },
        onNavigateUp = { navController.navigateUp() }
      )
      Log.d(TAG, "Composable ${DeviceDetailsDestination.routeWithArgs} has exited DeviceDetailScreen()")
    }

    composable(route = MissingPermissionsDestination.route) {
      Log.d(TAG, "Composable ${MissingPermissionsDestination.route} is calling MissingPermissionsScreen()")
      MissingPermissionsScreen(
        navigateToHome = {
          Log.d(TAG, "Navigating to HomeDestination.route")
          navController.navigate(HomeDestination.route) {
            // Make sure user can not return to MissingPermissionsScreen
            Log.d(TAG, "Popped up to MissingPermissionsDestination.route")
            popUpTo(MissingPermissionsDestination.route) { inclusive = true }
          }
          Log.d(TAG, "Navigated to HomeDestination.route")
        }
      )
      Log.d(TAG, "Composable ${MissingPermissionsDestination.route} has exited MissingPermissionsScreen()")
    }
  }

  Log.d(TAG, "Exited GaaiNavHost with navController = ${navController.toString()}")
}
