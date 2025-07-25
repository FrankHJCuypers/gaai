/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2024-2025, Frank HJ Cuypers
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

package be.cuypers_ghys.gaai.ui

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers.RED_DOMINATED_EXAMPLE
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import be.cuypers_ghys.gaai.R.string
import be.cuypers_ghys.gaai.ui.navigation.GaaiNavHost
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme

// Tag for logging
private const val TAG = "GaaiApp"

/**
 * Top level composable that represents screens for the application.
 */
@Composable
fun GaaiApp(navController: NavHostController = rememberNavController()) {
  Log.d(TAG, "Entered GaaiApp with navController = $navController")
  GaaiNavHost(navController = navController)
  Log.d(TAG, "Exiting GaaiApp ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaaiTopAppBar(
  title: String,
  canNavigateUp: Boolean,
  modifier: Modifier = Modifier,
  scrollBehavior: TopAppBarScrollBehavior? = null,
  navigateUp: () -> Unit = {}
) {
  CenterAlignedTopAppBar(
    title = { Text(title) },
    modifier = modifier,
    scrollBehavior = scrollBehavior,
    navigationIcon = {
      if (canNavigateUp) {
        IconButton(onClick = navigateUp) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(string.up)
          )
        }
      }
    }
  )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiTopAppBarPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiTopAppBarPreviewLight")
@Preview(
  showBackground = true,
  uiMode = UI_MODE_NIGHT_NO,
  name = "GaaiTopAppBarPreviewDynamic",
  wallpaper = RED_DOMINATED_EXAMPLE
)
@Composable
fun GaaiTopAppBarPreview() {
  GaaiTheme(dynamicColor = true) {
    Surface {
      GaaiTopAppBar(
        title = "Gaai",
        canNavigateUp = true,
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
      )
    }
  }
}
