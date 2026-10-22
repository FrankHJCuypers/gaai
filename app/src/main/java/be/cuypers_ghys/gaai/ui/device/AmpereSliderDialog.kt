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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import kotlin.math.roundToInt

private const val TAG = "AmpereSliderDialog"

/**
 * Implements a [Dialog] with a slider, allowing [value] to be changed.
 *
 * @param name The label to place with the [value]
 * @param value The initial value in Ampere to display and possibly change.
 * @param minValue The minimal value allowed for [value] by the slider.
 * @param maxValue The maximum value allowed for [value] by the slider.
 * @param onConfirm Function to be called when changing the [value] is confirmed by the user.
 * @param onDismiss Function to be called when changing the [value] is cancelled by the user.
 * @param modifier the [Modifier] to be applied to this AmpereSliderDialog
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun AmpereSliderDialog(
  name: String,
  value: UByte,
  minValue: UByte,
  maxValue: UByte,
  onConfirm: (UByte) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY AmpereSliderDialog(name=$name)")

  var sliderPosition by remember { mutableFloatStateOf(value.toFloat()) }
  Dialog(onDismissRequest = { onDismiss() }) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
          text = name,
          style = MaterialTheme.typography.titleMedium,
        )
        Slider(
          value = sliderPosition,
          onValueChange = { sliderPosition = it.roundToInt().toFloat() },
          steps = maxValue.toInt() - minValue.toInt() - 1,
          valueRange = minValue.toFloat()..maxValue.toFloat()
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "$minValue A")
          Spacer(Modifier.weight(1f))
          Text(text = "${sliderPosition.toInt()} A")
          Spacer(Modifier.weight(1f))
          Text(text = "$maxValue A")
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Spacer(Modifier.weight(1f))
          Button(onClick = {
            Log.v(TAG, "ampereSliderDialog dismissed")
            onDismiss()
          }) {
            Text(stringResource(R.string.cancel))
          }
          Spacer(Modifier.weight(1f))
          Button(onClick = {
            val newValue = sliderPosition.toUInt().toUByte()
            Log.d(TAG, "ampereSliderDialog confirmed $newValue")
            onConfirm(newValue)
          }) {
            Text(stringResource(R.string.ok))
          }
          Spacer(Modifier.weight(1f))
        }
      }
    }
  }
  Log.v(TAG, "RETURN AmpereSliderDialog()")
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "AmpereSliderDialogPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "AmpereSliderDialogPreviewLight")
@Composable
private fun AmpereSliderDialogPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(20.dp)
    ) {
      AmpereSliderDialog(
        name = "Sample Ampere x",
        value = 32u,
        minValue = 6u,
        maxValue = 50u,
        onDismiss = {},
        onConfirm = {},
      )
    }
  }
}


