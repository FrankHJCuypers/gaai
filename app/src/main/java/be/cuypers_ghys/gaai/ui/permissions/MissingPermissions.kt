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
package be.cuypers_ghys.gaai.ui.permissions

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.ui.AppViewModelProvider
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberMultiplePermissionsState

// Tag for logging
private const val TAG = "MissingPermissions"

object MissingPermissionsDestination : NavigationDestination {
    override val route = "permission"
    override val titleRes = R.string.permissions
}

/**
 * Screen handling the required Bluetooth permissions.
 * See [MissingPermissionsViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingPermissionsScreen(
    navigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    canNavigateBack: Boolean = false
) {
    Scaffold(
        topBar = {
            GaaiTopAppBar(
                title = stringResource(MissingPermissionsDestination.titleRes),
                canNavigateBack = canNavigateBack,
            )
        }
    ) { innerPadding ->
        MissingPermissionsComponent(
            navigateToHome = navigateToHome,
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
                    top = innerPadding.calculateTopPadding(),
                    end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
                )
                .fillMaxWidth()
        )
    }
}

/**
 * Based on [RequestMultiplePermissionsSample](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/permissions/RequestMultiplePermissionsSample.kt)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MissingPermissionsComponent(
    navigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MissingPermissionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    Log.d(TAG, "Required permissions: $viewModel.permissions")

    // rememberMultiplePermissionsState) is a composable, so can not be called from a ViewModel.
    val multiplePermissionsState = rememberMultiplePermissionsState(
        permissions = viewModel.permissions,
    )

    Log.d(TAG, "permissionState: $multiplePermissionsState.")
    if (multiplePermissionsState.allPermissionsGranted) {
        Log.d(TAG, "All permissions granted")
        navigateToHome()
    } else {
        Column (
            modifier = modifier.padding(dimensionResource(id = R.dimen.padding_medium)),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = getTextToShowGivenPermissions(
                    multiplePermissionsState.revokedPermissions,
                    multiplePermissionsState.shouldShowRationale
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { multiplePermissionsState.launchMultiplePermissionRequest() }) {
                Text(stringResource(R.string.request_permissions))
            }
        }
    }
}

/**
 * @param permissions List of required permission states.
 * @param shouldShowRationale Do we need to present a rationale for the permissions to the user?
 *
 * This function is made @Composable for easier string resource handling.
 *
 * Based on [RequestMultiplePermissionsSample](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/permissions/RequestMultiplePermissionsSample.kt)
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun getTextToShowGivenPermissions(
    permissions: List<PermissionState>,
    shouldShowRationale: Boolean
): String {
    val revokedPermissionsSize = permissions.size
    if (revokedPermissionsSize == 0) return ""

    val textToShow = StringBuilder().apply {
        append(stringResource(R.string.the_space))
    }

    for (i in permissions.indices) {
        textToShow.append(permissions[i].permission)
        when {
            revokedPermissionsSize > 1 && i == revokedPermissionsSize - 2 -> {
                textToShow.append(stringResource(R.string.command_and_space))
            }
            i == revokedPermissionsSize - 1 -> {
                textToShow.append(" ")
            }
            else -> {
                textToShow.append(", ")
            }
        }
    }
    textToShow.append(if (revokedPermissionsSize == 1) stringResource(R.string.permission_is) else stringResource(
        R.string.permissions_are
    )
    )
    textToShow.append(
        if (shouldShowRationale) {
            stringResource(R.string.permissions_required)
        } else {
            stringResource(R.string.permissions_denied)
        }
    )
    Log.d(TAG, "Returned text: $textToShow")
    return textToShow.toString()
}


@Preview(showBackground = true)
@Composable
fun MissingPermissionsComponentPreview() {
    GaaiTheme {
        MissingPermissionsComponent(navigateToHome = {})
    }
}