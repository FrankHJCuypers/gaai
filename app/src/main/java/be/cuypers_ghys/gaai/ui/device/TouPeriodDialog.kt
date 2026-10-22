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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.util.TouPeriod
import be.cuypers_ghys.gaai.util.TouTime

private const val TAG = "TouPeriodDialog"

/**
 * Implements a [Row] displaying the [time],
 * allowing to change the displayed [TouTime] .
 * @param name The label to display next to the [time] information.
 * @param time The time to display.
 * @param onDismissRequest Function to be called when changing the [TouTime] is cancelled by the user.
 * @param onConfirmation Function to be called when changing the [TouTime] is confirmed by the user.
 * @param modifier the [Modifier] to be applied to this TouTimeRow.
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun TouTimeRow(
  name: String,
  time: TouTime,
  onDismissRequest: () -> Unit,
  onConfirmation: (TouTime) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY TouTimeRow(name=$name)")

  var showTimeDialog by remember { mutableStateOf(false) }
  var newTime by remember { mutableStateOf(time) }
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable { showTimeDialog = true },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = name,
      style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.weight(1f))
    Text(
      text = newTime.toString(),
      style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.width(16.dp))
    Icon(
      painter = painterResource(R.drawable.outline_edit_24),
      contentDescription = stringResource(R.string.editable)
    )
    if (showTimeDialog) {
      DialTimePickerDialog(
        title = stringResource(R.string.select_time, name.lowercase()),
        touTime = time,
        onConfirm = { touTime ->
          Log.d(TAG, "showTimeDialog confirmed: $touTime")
          newTime = touTime
          onConfirmation(touTime)
          showTimeDialog = false
        },
        onDismiss = {
          Log.v(TAG, "showTimeDialog dismissed")
          onDismissRequest()
          showTimeDialog = false
        }
      )
    }
  }
  Log.v(TAG, "RETURN TouTimeRow()")
}

/**
 * Implements a [Dialog] displaying the [touPeriod],
 * allowing to change the displayed [TouPeriod.startTime] and [TouPeriod.endTime].
 * @param title The label to display next to the [touPeriod] information.
 * @param touPeriod The period to display.
 * @param onDismissRequest Function to be called when changing the [TouPeriod.startTime] and [TouPeriod.endTime]
 * is cancelled by the user.
 * @param onConfirmation Function to be called when changing the [TouPeriod.startTime] and [TouPeriod.endTime]
 * is confirmed by the user.
 * @param modifier the [Modifier] to be applied to this TouPeriodDialog.
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun TouPeriodDialog(
  title: String,
  touPeriod: TouPeriod,
  onDismissRequest: () -> Unit,
  onConfirmation: (TouPeriod) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY TouPeriodDialog(title=$title)")

//    var newTouPeriod by remember { mutableStateOf(touPeriod) }
  var newTouPeriodStart by remember { mutableStateOf(touPeriod.startTime) }
  var newTouPeriodEnd by remember { mutableStateOf(touPeriod.endTime) }

  Dialog(onDismissRequest = { onDismissRequest() }) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
      Row(
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
        )
      }
      TouTimeRow(
        name = stringResource(R.string.start),
        time = touPeriod.startTime,
        onDismissRequest = {
          Log.v(TAG, "touTimeRow start dismissed")
        },
        onConfirmation = { touTime ->
          newTouPeriodStart = touTime
//                    newTouPeriod = newTouPeriod.copy(startTime = touTime)
          Log.d(TAG, "touTimeRow start confirmed: $touTime")
        },
        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
      )
      TouTimeRow(
        name = stringResource(R.string.end),
        time = touPeriod.endTime,
        onDismissRequest = {
          Log.v(TAG, "touTimeRow end dismissed")
        },
        onConfirmation = { touTime ->
//                        newTouPeriod = newTouPeriod.copy(endTime = touTime)
          newTouPeriodEnd = touTime
          Log.d(TAG, "touTimeRow end confirmed: $touTime")
        },
        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small))
      )

      Row(
        Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Button(onClick = onDismissRequest) {
          Text(stringResource(R.string.cancel))
        }
        Spacer(Modifier.weight(1f))
        Button(onClick = {
//                    onConfirmation(newTouPeriod)
          onConfirmation(TouPeriod(newTouPeriodStart, newTouPeriodEnd))
        }) {
          Text(stringResource(R.string.ok))
        }
      }
    }
  }
  Log.v(TAG, "RETURN TouPeriodDialog()")
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "TouPeriodDialogPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "TouPeriodDialogPreviewLight")
@Composable
private fun TouPeriodDialogPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
      TouPeriodDialog(
        title = "Weekdays",
        touPeriod = TouPeriod(720, 1234),
        onDismissRequest = {},
        onConfirmation = {},
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
}

