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
import android.graphics.drawable.AdaptiveIconDrawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers.RED_DOMINATED_EXAMPLE
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.R.string
import be.cuypers_ghys.gaai.ui.navigation.GaaiNavHost
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import androidx.compose.ui.platform.LocalResources

// Tag for logging
private const val TAG = "GaaiApp"

/**
 * Top level composable that represents screens for the application.
 */
@Composable
fun GaaiApp(navController: NavHostController = rememberNavController()) {
  Log.d(TAG, "ENTRY GaaiApp(navController = $navController)")
  GaaiNavHost(navController = navController)
  Log.v(TAG, "RETURN GaaiApp() ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GaaiTopAppBar(
  title: String,
  canNavigateUp: Boolean,
  modifier: Modifier = Modifier,
  scrollBehavior: TopAppBarScrollBehavior? = null,
  navigateUp: () -> Unit = {},
  actions: @Composable (RowScope.() -> Unit) = {},
) {
  Log.d(TAG, "ENTRY GaaiTopAppBar(title = $title)")
  CenterAlignedTopAppBar(
    title = {
      Row {
        Image(
          painter = adaptiveIconPainterResource(R.mipmap.ic_launcher_round), // Replace with your app icon
          contentDescription = stringResource(string.gaai_icon),
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(title)
      }
    },
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
    },
    actions = actions
  )
  Log.v(TAG, "ENTRY GaaiTopAppBar()")
}

// Found on https://gist.github.com/tkuenneth/ddf598663f041dc79960cda503d14448
@Composable
fun adaptiveIconPainterResource(@DrawableRes id: Int): Painter {
  Log.d(TAG, "ENTRY adaptiveIconPainterResource(id = $id)")

  val res = LocalResources.current
  val theme = LocalContext.current.theme

  // Android O supports adaptive icons, try loading this first (even though this is least likely to be the format).
  val adaptiveIcon = ResourcesCompat.getDrawable(res, id, theme) as? AdaptiveIconDrawable
  return if (adaptiveIcon != null) {
    BitmapPainter(adaptiveIcon.toBitmap().asImageBitmap())
  } else {
    // We couldn't load the drawable as an Adaptive Icon, just use painterResource
    painterResource(id)
  }
  Log.d(TAG, "RETURN adaptiveIconPainterResource()")
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
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiTopAppBar(
        title = "Gaai",
        canNavigateUp = true,
        scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
      )
    }
  }
}
