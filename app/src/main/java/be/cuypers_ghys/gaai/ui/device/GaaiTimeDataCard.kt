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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.TimeData
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.util.Timestamp

private const val TAG = "GaaiTimeDataCard"

/**
 * Implements a [Card] displaying the [timeData] with the possibility to get and set the time of
 * the Nexxtender Home device.
 *
 * @param timeData The information to get or set.
 * @param onTimeGet Function to be called when [GaaiTimeDataCard] wants to read the
 *  device's [TimeData.time].
 * @param onTimeSync Function to be called when [GaaiTimeDataCard] wants to sync the
 *  device's [TimeData.time] with the current time on the mobile phone.
 * @param modifier the [Modifier] to be applied to this [GaaiTimeDataCard]
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiTimeDataCard(
  timeData: TimeData,
  onTimeGet: () -> Unit,
  onTimeSync: () -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiTimeDataCard(timeData = $timeData)")
  Card(
    modifier = modifier, elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.time),
            style = MaterialTheme.typography.titleLarge,
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.current_time),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = if (timeData.time == 0u) "Not loaded yet" else Timestamp.toString(timeData.time),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Spacer(Modifier.weight(1f))
          Button(onClick = {
            Log.v(TAG, "GaaiTimeDataCard Get Time pressed")
            onTimeGet()
          }
          ) {
            Text(stringResource(R.string.get_time))
          }
          Spacer(Modifier.weight(1f))
          Button(onClick = {
            Log.v(TAG, "GaaiTimeDataCard Sync Time pressed")
            onTimeSync()
          }
          ) {
            Text(stringResource(R.string.sync_time))
          }
          Spacer(Modifier.weight(1f))
        }
      }
    }
    Log.v(TAG, "RETURN GaaiTimeDataCard()")
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiTimeDataCardPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiTimeDataCardPreviewLight")
@Composable
private fun GaaiTimeDataCardPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiTimeDataCard(
        timeData = TimeData(0x662D0EFBu),
        onTimeGet = {},
        onTimeSync = {},
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),

        )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiTimeDataCardTime0PreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiTimeDataCardTime0PreviewLight")
@Composable
private fun GaaiTimeDataCardTime0Preview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiTimeDataCard(
        timeData = TimeData(0u),
        onTimeGet = {},
        onTimeSync = {},
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),

        )
    }
  }
}



