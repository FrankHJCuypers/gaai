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

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import be.cuypers_ghys.gaai.BuildConfig
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.R.string
import be.cuypers_ghys.gaai.ui.adaptiveIconPainterResource
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme

// Tag for logging
private const val TAG = "About"

@Composable
fun AboutDialog(onDismissRequest: () -> Unit) {
  Log.v(TAG, "ENTRY AboutDialog()")

  Dialog(onDismissRequest = { onDismissRequest() }) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .height(400.dp)
        .padding(16.dp),
      shape = RoundedCornerShape(16.dp),
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Image(
          painter = adaptiveIconPainterResource(R.mipmap.ic_launcher_round), // Replace with your app icon
          contentDescription = stringResource(string.gaai_icon),
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(HomeDestination.titleRes), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
          Text(text = stringResource(string.version) + " ", style = MaterialTheme.typography.bodyMedium)
          Text(text = BuildConfig.VERSION_NAME, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "© 2024-2026 Frank HJ Cuypers", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "AGPL-3.0 license", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = buildAnnotatedString {
          withLink(
            LinkAnnotation.Url(
              "https://github.com/FrankHJCuypers/gaai",
              TextLinkStyles(style = SpanStyle( color = MaterialTheme.colorScheme.primary ))
            )
          ) {
            append("https://github.com/FrankHJCuypers/gaai")
          }
        } , style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(
          onClick = { onDismissRequest() },
          modifier = Modifier.padding(8.dp),
        ) {
          Text("Dismiss")
        }
      }
    }
  }
  Log.v(TAG, "RETURN AboutDialog()")
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "AboutDialogPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "AboutDialogPreviewLight")
@Composable
fun AboutDialogPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      AboutDialog({})
    }
  }
}