/*
 * Project Gaai: one app to control the Nexxtender chargers.
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

package be.cuypers_ghys.gaai.ui.home

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme

@Composable
fun DropdownMenuWithDetails(initialExpanded: Boolean = false) {
  var expanded by remember { mutableStateOf(initialExpanded) }
  var showAboutWindow by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .padding(16.dp)
  ) {
    IconButton(onClick = { expanded = !expanded }) {
      Icon(Icons.Default.MoreVert, contentDescription = "More options")
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = !expanded }
    ) {
      DropdownMenuItem(
        text = { Text("About") },
        leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
        onClick = { showAboutWindow = true }
      )
      val context = LocalContext.current
      val url = "https://frankhjcuypers.github.io/gaai/" // Replace with your desired
      DropdownMenuItem(
        text = { Text("Help") },
        leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Help, contentDescription = null) },
        trailingIcon = { Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null) },
        onClick = {
          val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
          }
          // Start the activity
          context.startActivity(intent)
        }
      )
    }
    if (showAboutWindow) {
      AboutDialog { showAboutWindow = false }
    }
  }
}

@Composable
fun DropdownMenuExpandedWithDetails(expanded: Boolean, onDismissRequest: () -> Unit) {
  var showAboutWindow by remember { mutableStateOf(false) }
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest
  ) {

    DropdownMenuItem(
      text = { Text("About") },
      leadingIcon = { Icon(Icons.Outlined.Info, contentDescription = null) },
      onClick = { showAboutWindow = true }
    )
    val context = LocalContext.current
    val url = "https://frankhjcuypers.github.io/gaai/" // Replace with your desired
    DropdownMenuItem(
      text = { Text("Help") },
      leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Help, contentDescription = null) },
      trailingIcon = { Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null) },
      onClick = {
        val intent = Intent(Intent.ACTION_VIEW).apply {
          data = Uri.parse(url)
        }
        // Start the activity
        context.startActivity(intent)
      }
    )
  }
  if (showAboutWindow) {
    AboutDialog { showAboutWindow = false }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DropdownMenuWithDetailsPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DropdownMenuWithDetailsPreviewLight")
@Composable
fun DropdownMenuWithDetailsPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DropdownMenuWithDetails()
    }
  }
}

// Only one @Preview is successfull
//@Preview(widthDp = 320, heightDp = 320,showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DropdownMenuWithDetailsExpandedPreviewDark")
@Preview(
  widthDp = 180,
  heightDp = 230,
  showBackground = true,
  uiMode = UI_MODE_NIGHT_NO,
  name = "DropdownMenuWithDetailsExpandedPreviewLight"
)
@Preview(
  widthDp = 180,
  heightDp = 230,
  showBackground = true,
  uiMode = UI_MODE_NIGHT_YES,
  name = "DropdownMenuWithDetailsExpandedPreviewDark"
)
//@Preview(
//  widthDp = 320, heightDp = 320,
//  showBackground = true,
//  uiMode = UI_MODE_NIGHT_NO,
//  name = "DropdownMenuWithDetailsExpandedPreviewDynamic",
//  wallpaper = RED_DOMINATED_EXAMPLE
//)
@Composable
fun DropdownMenuWithDetailsExpandedPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface(modifier = Modifier.fillMaxSize()) {
      Card {
        DropdownMenuWithDetails(true)
      }
    }
  }
}

