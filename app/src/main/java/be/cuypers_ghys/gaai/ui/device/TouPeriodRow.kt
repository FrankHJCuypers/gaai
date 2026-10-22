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
import androidx.compose.foundation.layout.fillMaxWidth
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
import be.cuypers_ghys.gaai.util.TouPeriod

private const val TAG = "TouPeriodRow"

/**
 * Implements a [Row] displaying the [touPeriod].
 * Also allows to change the displayed [TouPeriod.startTime] and [TouPeriod.endTime] in a dialog box.
 * @param title The label to display next to the [touPeriod] information.
 * @param touPeriod The period to display.
 * @param onDismissRequest Function to be called when changing the [TouPeriod.startTime] and [TouPeriod.endTime]
 * is cancelled by the user.
 * @param onConfirmation Function to be called when changing the [TouPeriod.startTime] and [TouPeriod.endTime]
 * is confirmed by the user.
 * @param modifier the [Modifier] to be applied to this GaaiChargingAdvancedDataCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun TouPeriodRow(
  title: String,
  touPeriod: TouPeriod,
  onDismissRequest: () -> Unit,
  onConfirmation: (TouPeriod) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY TouPeriodRow(touPeriod = $touPeriod)")
  var showPeriodDialog by remember { mutableStateOf(false) }
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickable { showPeriodDialog = true },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
    )
    Spacer(Modifier.weight(1f))
    Text(
      text = touPeriod.toString(),
      style = MaterialTheme.typography.titleMedium
    )
    Spacer(Modifier.width(16.dp))
    Icon(
      painter = painterResource(R.drawable.outline_edit_24),
      contentDescription = stringResource(R.string.editable)
    )

    if (showPeriodDialog) {
      TouPeriodDialog(
        title = title,
        touPeriod = touPeriod,
        onDismissRequest = {
          Log.v(TAG, "ShowPeriodDialog dismissed")
          showPeriodDialog = false
          onDismissRequest()
        },
        onConfirmation = { newTouPeriod ->
          Log.d(TAG, "ShowPeriodDialog confirmed: $newTouPeriod")
          showPeriodDialog = false
          onConfirmation(newTouPeriod)
        },
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
  Log.v(TAG, "RETURN TouPeriodRow()")
}

