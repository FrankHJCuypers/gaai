/*
* Project Gaai: one app to control the Nexxtender chargers.
* Copyright © 2024-2026, Frank HJ Cuypers
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.util.TouTime

private const val TAG = "DialTimePickerDialog"

/**
 * Implements a [Dialog] with a [TimePicker] displaying the [touTime],
 * allowing to change the displayed [TouTime] .
 * @param title The label to display next to the [touTime] information.
 * @param touTime The initial time to display.
 * @param onDismiss Function to be called when changing the [TouTime] is cancelled by the user.
 * @param onConfirm Function to be called when changing the [TouTime] is confirmed by the user.
 * @param modifier the [Modifier] to be applied to this DialTimePickerDialog
 *
 * @author Frank HJ Cuypers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialTimePickerDialog(
  title: String,
  touTime: TouTime,
  onConfirm: (TouTime) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY DialTimePickerDialog(title=$title)")

  val timePickerState = rememberTimePickerState(
    initialHour = touTime.getHours(),
    initialMinute = touTime.getMinutes(),
    is24Hour = true,
  )
  Dialog(onDismissRequest = { onDismiss() }) {
    Card(
      modifier = modifier
        .width(IntrinsicSize.Min)
        .height(IntrinsicSize.Min),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column {
        Text(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
          text = title,
          style = MaterialTheme.typography.labelMedium
        )
        TimePicker(
          state = timePickerState,
        )
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Spacer(Modifier.weight(1f))
          Button(onClick = {
            Log.v(TAG, "DialTimePickerDialog() dismissed")
            onDismiss()
          }) {
            Text(stringResource(R.string.cancel))
          }
          Spacer(Modifier.weight(1f))
          Button(onClick = {
            val newTouTime = TouTime(timePickerState)
            Log.d(TAG, "DialTimePickerDialog() confirmed $newTouTime")
            onConfirm(newTouTime)
          }) {
            Text(stringResource(R.string.ok))
          }
          Spacer(Modifier.weight(1f))
        }
      }
    }
  }
  Log.v(TAG, "RETURN DialTimePickerDialog()")
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DialTimePickerDialogPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DialTimePickerDialogPreviewLight")
@Composable
private fun DialTimePickerDialogPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface(modifier = Modifier
      .fillMaxSize()
      .padding(20.dp)) {
      DialTimePickerDialog(
        title = "Sample Time Picker Dialog",
        touTime = TouTime(13, 45),
        onDismiss = {},
        onConfirm = {},
      )
    }
  }
}


