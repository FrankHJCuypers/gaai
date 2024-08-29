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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.ui.GaaiTopAppBar
import be.cuypers_ghys.gaai.ui.navigation.NavigationDestination
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
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
    navigateBack: () -> Unit,
    onNavigateUp: () -> Unit,
    canNavigateBack: Boolean = true
) {
    Scaffold(
        topBar = {
            GaaiTopAppBar(
                title = stringResource(MissingPermissionsDestination.titleRes),
                canNavigateBack = canNavigateBack,
                navigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        MissingPermissionsComponent(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MissingPermissionsComponent(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
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

    val permissionsState = rememberMultiplePermissionsState( // 5.
        permissions = permissions,
    )

    Log.d(TAG, "permissionState: $permissionsState.")
    if (permissionsState.allPermissionsGranted) { // 6.
//        content()
        Log.d(TAG, "All permissions granted")
        Text(text = "Permission Granted")
    } else {
        Column {
            val textToShow = if (permissionsState.allPermissionsGranted) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "The BLE permissions are required by this app. Please grant the permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "This app can only work with BLE permissions. Please grant the permission"
            }
            Text(textToShow, modifier = Modifier.padding(contentPadding))
            Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MissingPermissionsComponentPreview() {
    GaaiTheme {
        MissingPermissionsComponent()
    }
}