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
import be.cuypers_ghys.gaai.data.ChargerType
import be.cuypers_ghys.gaai.data.ChargingBasicData
import be.cuypers_ghys.gaai.data.Discriminator
import be.cuypers_ghys.gaai.data.Status
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme

private const val TAG = "GaaiChargingBasicDataCard"

/**
 * Implements a [Card] displaying the [chargingBasicData].
 * @param chargingBasicData The information to display.
 * @param chargerType
 * @param modifier the [Modifier] to be applied to this GaaiChargingBasicDataCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiChargingBasicDataCard(
  chargingBasicData: ChargingBasicData, chargerType: ChargerType, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiChargingBasicDataCard(chargingBasicData = $chargingBasicData)")
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
            text = stringResource(R.string.basic_data),
            style = MaterialTheme.typography.titleLarge,
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.seconds),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = "%3d s".format(chargingBasicData.seconds.toInt()),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.discriminator),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = chargingBasicData.discriminator.toString(),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.status),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = chargingBasicData.status.toString(),
            style = MaterialTheme.typography.titleMedium
          )
        }
        if (chargerType != ChargerType.HOME) {
          Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = stringResource(R.string.car_power),
              style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.weight(1f))
            Text(
              text = "%.3f kW".format(chargingBasicData.rfu1CarPower.toFloat() / 1000.0),
              style = MaterialTheme.typography.titleMedium
            )
          }
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.energy),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = "%.3f kWh".format(chargingBasicData.energy.toFloat() / 1000.0),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.phasecount),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = chargingBasicData.phaseCount.toString(),
            style = MaterialTheme.typography.titleMedium
          )
        }
      }
    }
    Log.v(TAG, "RETURN GaaiChargingBasicDataCard()")
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "ChargingBasicDataPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "ChargingBasicDataPreviewLight")
@Composable
private fun ChargingBasicDataPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiChargingBasicDataCard(
        chargingBasicData = ChargingBasicData(
          seconds = 123u, discriminator = Discriminator.STOPPED,
          status = Status.PLUGGED, rfu1CarPower = 2345u, energy = 1234u, phaseCount = 2u
        ),
        ChargerType.MOBILE_BLACK,
        modifier = Modifier
          .padding(
            dimensionResource(id = R.dimen.padding_small)
          )
      )
    }
  }
}

