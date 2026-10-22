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

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import be.cuypers_ghys.gaai.R

private const val TAG = "AmpereRow"

/**
 * Implements a [Row] displaying the [value] in Ampere, allowing it to be changed.
 *
 * @param name The label to place with the [value]
 * @param value The value in Ampere to display and possibly change.
 * @param minValue The minimal value allowed for [value] when changing.
 * @param maxValue The maximum value allowed for [value] when changing.
 * @param onConfirm Function to be called when changing the [value] is confirmed by the user.
 * @param onDismiss Function to be called when changing the [value] is cancelled by the user.
 * @param modifier the [Modifier] to be applied to this AmpereRow
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun AmpereRow(
  name: String,
  value: UByte,
  minValue: UByte,
  maxValue: UByte,
  onConfirm: (UByte) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY AmpereRow(name=$name)")

  var showSliderDialog by remember { mutableStateOf(false) }
  Row(
    modifier = modifier
      .clickable { showSliderDialog = true },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = name,
      style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.weight(1f))
    Text(
      text = "$value A",
      style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.width(16.dp))
    Icon(
      painter = painterResource(R.drawable.outline_edit_24),
      contentDescription = stringResource(R.string.editable)
    )
    if (showSliderDialog) {
      AmpereSliderDialog(
        name = name,
        value = value,
        minValue = minValue,
        maxValue = maxValue,
        onDismiss = {
          Log.v(TAG, "ampereSliderDialog dismissed")
          showSliderDialog = false
          onDismiss()
        },
        onConfirm = { newValue ->
          Log.d(TAG, "ampereSliderDialog confirmed: $newValue")
          showSliderDialog = false
          onConfirm(newValue)
        },
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
  Log.v(TAG, "RETURN AmpereRow()")
}


