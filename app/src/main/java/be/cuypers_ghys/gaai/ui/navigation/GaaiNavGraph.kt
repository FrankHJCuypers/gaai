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

/**
 * Provides Navigation graph for the application.
 */
@Composable
fun GaaiNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = MissingPermissionsDestination.route,
        modifier = modifier
    ) {
        composable(route = HomeDestination.route) {
            HomeScreen(
                navigateToDeviceEntry = { navController.navigate(DeviceEntryDestination.route) },
                navigateToDeviceDetails = {
                    navController.navigate("${DeviceDetailsDestination.route}/${it}")
                }
            )
        }
        composable(route = DeviceEntryDestination.route) {
            DeviceEntryScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(
            route = DeviceDetailsDestination.routeWithArgs,
            arguments = listOf(navArgument(DeviceDetailsDestination.deviceIdArg) {
                type = NavType.IntType
            })
        ) {
            DeviceDetailsScreen(
                navigateBack = { navController.popBackStack() },
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(route = MissingPermissionsDestination.route) {
            MissingPermissionsScreen(
                navigateToHome = { navController.navigate(HomeDestination.route)  },
            )
        }
    }
}
