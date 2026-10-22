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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.Mode
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme

private const val TAG = "ModeDialog"

/**
 * Converts [mode] to a string value to display.
 * @param mode
 * @return The corresponding string.
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun modeToText(mode: Mode) = when (mode) {
  Mode.MAX_OPEN -> stringResource(R.string.max_open)
  Mode.MAX_PRIVATE -> stringResource(R.string.max_private)
  Mode.ECO_OPEN -> stringResource(R.string.eco_open)
  Mode.ECO_PRIVATE -> stringResource(R.string.eco_private)
  else -> {
    stringResource(R.string.unknown)
  }
}

/**
 * Implements a [Dialog] displaying the [mode], allowing it to be changed.
 *
 * @param mode The [Mode] to display and possibly change
 * @param onDismiss Function to be called when changing the [mode] is cancelled by the user.
 * @param onConfirm Function to be called when changing the [mode] is confirmed by the user.
 * @param modifier the [Modifier] to be applied to this ModeDialog.
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun ModeDialog(
  mode: Mode,
  onConfirm: (Mode) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY ModeDialog(mode=$mode)")

  val radioOptionsText = listOf(
    modeToText(mode = Mode.ECO_PRIVATE), modeToText(mode = Mode.MAX_PRIVATE),
    modeToText(mode = Mode.ECO_OPEN), modeToText(mode = Mode.MAX_OPEN), modeToText(mode = Mode.UNKNOWN)
  )
  val radioOptions = listOf(Mode.ECO_PRIVATE, Mode.MAX_PRIVATE, Mode.ECO_OPEN, Mode.MAX_OPEN, Mode.UNKNOWN)
  val modeString = modeToText(mode)
  Dialog(onDismissRequest = { onDismiss() })
  {
    Card(
      modifier = modifier
    ) {
      Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Text(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
          text = stringResource(R.string.choose_default_mode),
          style = MaterialTheme.typography.titleMedium,
        )
        val (selectedOption, onOptionSelected) = remember { mutableStateOf(modeString) }
        Column(Modifier.selectableGroup()) {
          radioOptionsText.forEach { text ->
            Row(
              Modifier
                .fillMaxWidth()
                .height(56.dp)
                .selectable(
                  selected = (text == selectedOption),
                  onClick = { onOptionSelected(text) },
                  role = Role.RadioButton
                )
                .padding(horizontal = 16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = (text == selectedOption),
                onClick = null // null recommended for accessibility with screen readers
              )
              Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.merge(),
                modifier = Modifier.padding(start = 16.dp)
              )
            }
          }
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Spacer(Modifier.weight(1f))
          Button(onClick = {
            Log.v(TAG, "ModeDialog dismissed")
            onDismiss()
          }
          ) {
            Text(stringResource(R.string.cancel))
          }
          Spacer(Modifier.weight(1f))
          Button(
            enabled = selectedOption != radioOptionsText[4], // not UNKNOWN
            onClick = {
              val newValue = radioOptions[radioOptionsText.indexOf(selectedOption)]
              Log.d(TAG, "ModeDialog confirmed $newValue")
              onConfirm(newValue)
            }
          ) {
            Text(stringResource(R.string.ok))
          }
          Spacer(Modifier.weight(1f))
        }
      }
    }
  }
  Log.v(TAG, "RETURN ModeDialog()")
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "ModeDialogPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "ModeDialogPreviewLight")
@Composable
private fun ModeDialogPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      ModeDialog(
        mode = Mode.ECO_PRIVATE,
        onDismiss = {},
        onConfirm = {},
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "ModeDialogUnknownPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "ModeDialogUnknownPreviewLight")
@Composable
private fun ModeDialogUnknownPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      ModeDialog(
        mode = Mode.UNKNOWN,
        onDismiss = {},
        onConfirm = {},
      )
    }
  }
}