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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme

@Composable
fun DeleteConfirmationDialog(
  itemName: String,
  onCancel: () -> Unit,
  onConfirm: () -> Unit,
) {

  AlertDialog(
    onDismissRequest = {
      onCancel()
    },
    title = {
      Text(
        text = "Are you sure you want to delete this $itemName?",
        style = MaterialTheme.typography.titleLarge
      )
    },
    text = {
      Text(
        "This can not be undone!",
        fontSize = 16.sp
      )
    },
    confirmButton = {
      TextButton(
        onClick = { onConfirm() },
        modifier = Modifier.padding(8.dp),
      ) {
        Text("Delete it")
      }
    },
    dismissButton = {
      TextButton(
        onClick = {
          onCancel()
        },
        modifier = Modifier.padding(8.dp)
      ) {
        Text("Cancel")
      }
    },
  )
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeleteConfirmDialogDevicePreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeleteConfirmDialogDevicePreviewLight")
@Composable
fun DeleteConfirmDialogDevicePreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DeleteConfirmationDialog(
        "Device",
        onCancel = { }, onConfirm = {}
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeleteConfirmDialoBadgePreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeleteConfirmDialogBadgePreviewLight")
@Composable
fun DeleteConfirmDialogBadgePreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      DeleteConfirmationDialog(
        "Badge",
        onCancel = { }, onConfirm = {}
      )
    }
  }
}