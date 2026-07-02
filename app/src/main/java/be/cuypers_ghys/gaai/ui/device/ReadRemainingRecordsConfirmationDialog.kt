/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2026, Frank HJ Cuypers
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

package be.cuypers_ghys.gaai.ui.device

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
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

// Tag for logging
private const val TAG = "ReadRemainingRecordsConfirmationDialog"

@Composable
fun ReadRemainingRecordsConfirmationDialog(
  onCancel: () -> Unit,
  onConfirm: () -> Unit,
) {
  Log.v(TAG, "ENTRY ReadRemainingRecordsConfirmationDialog()")

  AlertDialog(
    onDismissRequest = {
      onCancel()
    },
    title = {
      Text(
        text = "Do you really want to read records from the charger?",
        style = MaterialTheme.typography.titleLarge
      )
    },
    text = {
      Text(
        "If you do, Nexxtmove can no longer read them!",
        fontSize = 16.sp
      )
    },
    confirmButton = {
      TextButton(
        onClick = { onConfirm() },
        modifier = Modifier.padding(8.dp),
      ) {
        Text("Continue")
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
    }
  )
  Log.v(TAG, "RETURN ReadRemainingRecordsConfirmationDialog()")
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "ReadRemainingRecordsConfirmationDialogPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "ReadRemainingRecordsConfirmationDialogPreviewLight")
@Composable
fun ReadRemainingRecordsConfirmationDialogPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface (
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp),
    ){
      ReadRemainingRecordsConfirmationDialog(
        onCancel = { }, onConfirm = {}
      )
    }
  }
}
