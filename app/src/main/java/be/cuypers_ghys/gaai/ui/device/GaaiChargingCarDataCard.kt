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
import be.cuypers_ghys.gaai.data.ChargingCarData
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.util.Timestamp

private const val TAG = "GaaiChargingCarDataCard"

/**
 * Implements a [Card] displaying the [chargingCarData].
 * @param chargingCarData The information to display.
 * @param modifier the [Modifier] to be applied to this GaaiChargingCarDataCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiChargingCarDataCard(
  chargingCarData: ChargingCarData, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiChargingCarDataCard(chargingCarData = $chargingCarData)")
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
            text = stringResource(R.string.car_data),
            style = MaterialTheme.typography.titleLarge,
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.timestamp),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = Timestamp.toString(chargingCarData.timestamp),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.l1),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = "%.1f A".format(chargingCarData.l1.toFloat() / 10.0),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.l2),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = "%.1f A".format(chargingCarData.l2.toFloat() / 10.0),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.l3),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = "%.1f A".format(chargingCarData.l3.toFloat() / 10.0),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.l1_power),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = "%.3f kW".format(chargingCarData.p1.toFloat() / 1000.0),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.l2_power),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = "%.3f kW".format(chargingCarData.p2.toFloat() / 1000.0),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.l3_power),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = "%.3f kW".format(chargingCarData.p3.toFloat() / 1000.0),
            style = MaterialTheme.typography.titleMedium
          )
        }
      }
    }
    Log.v(TAG, "RETURN GaaiChargingCarDataCard()")
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "ChargingGridDataPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "ChargingGridDataPreviewLight")
@Composable
private fun GaaiChargingCarDataCardPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiChargingCarDataCard(
        chargingCarData = ChargingCarData(
          timestamp = 0x662D0EFBu,
          l1 = 1,
          l2 = 2,
          l3 = -1,
          p1 = 1111,
          p2 = 22222,
          p3 = 3333
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
}
