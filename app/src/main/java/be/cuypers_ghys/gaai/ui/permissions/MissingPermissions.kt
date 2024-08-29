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

import android.Manifest
import android.os.Build
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
import be.cuypers_ghys.gaai.R
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingPermissionsScreen(
    navigateToHome: () -> Unit,
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
            navigateToHome,
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
    modifier: Modifier = Modifier

//    content: @Composable () -> Unit, // 2.
) {
    var permissions :List<String> = emptyList()
    if (Build.VERSION.SDK_INT <= 30)
    {
        Log.d(TAG, "SDK <= 30")
        permissions = listOf( // 3.
        Manifest.permission.ACCESS_FINE_LOCATION)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // 4.
        Log.d(TAG, "SDK >= 30")
        permissions = permissions.plus(
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            ),
        )
    }
    Log.d(TAG, "Required permissions: $permissions")

    val multiplePermissionsState = rememberMultiplePermissionsState( // 5.
        permissions = permissions,
    )

    Log.d(TAG, "permissionState: $multiplePermissionsState.")
    if (multiplePermissionsState.allPermissionsGranted) { // 6.
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
                Text("Request permissions")
            }
        }
    }
}

/**
 * Based on [RequestMultiplePermissionsSample](https://github.com/google/accompanist/blob/main/sample/src/main/java/com/google/accompanist/sample/permissions/RequestMultiplePermissionsSample.kt)
 */
// TODO: move to viewmodel?
@OptIn(ExperimentalPermissionsApi::class)
private fun getTextToShowGivenPermissions(
    permissions: List<PermissionState>,
    shouldShowRationale: Boolean
): String {
    val revokedPermissionsSize = permissions.size
    if (revokedPermissionsSize == 0) return ""

    val textToShow = StringBuilder().apply {
        append("The ")
    }

    for (i in permissions.indices) {
        textToShow.append(permissions[i].permission)
        when {
            revokedPermissionsSize > 1 && i == revokedPermissionsSize - 2 -> {
                textToShow.append(", and ")
            }
            i == revokedPermissionsSize - 1 -> {
                textToShow.append(" ")
            }
            else -> {
                textToShow.append(", ")
            }
        }
    }
    textToShow.append(if (revokedPermissionsSize == 1) "permission is" else "permissions are")
    textToShow.append(
        if (shouldShowRationale) {
            " required by the app.\nPlease grant all of them."
        } else {
            " denied. The app cannot function without them.\n Please provide them in the App Settings."
        }
    )
    Log.d(TAG, "Returned text: $textToShow")
    return textToShow.toString()
}
@Preview(showBackground = true)
@Composable
fun MissingPermissionsComponentPreview() {
    GaaiTheme {
        MissingPermissionsComponent({})
    }
}