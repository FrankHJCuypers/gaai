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
import be.cuypers_ghys.gaai.data.AuthorizationStatus
import be.cuypers_ghys.gaai.data.ChargingAdvancedData
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.util.Timestamp

private const val TAG = "GaaiChargingAdvancedDataCard"

/**
 * Implements a [Card] displaying the [chargingAdvancedData].
 * @param chargingAdvancedData The information to display.
 * @param modifier the [Modifier] to be applied to this GaaiChargingAdvancedDataCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiChargingAdvancedDataCard(
  chargingAdvancedData: ChargingAdvancedData, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiChargingAdvancedDataCard(chargingAdvancedData = $chargingAdvancedData)")
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
            text = stringResource(R.string.advanced_data),
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
            text = Timestamp.toString(chargingAdvancedData.timestamp),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.i_available),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = chargingAdvancedData.iAvailable.toString() + " A",
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.grid_power),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = "%.3f kW".format(chargingAdvancedData.gridPower.toFloat() / 1000.0),
            style = MaterialTheme.typography.titleMedium
          )
        }
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
            text = "%.3f kW".format(chargingAdvancedData.carPower.toFloat() / 1000.0),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.authorization_status),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = when (chargingAdvancedData.authorizationStatus.authStatus) {
              AuthorizationStatus.UNAUTHORIZED -> stringResource(R.string.unauthorized)
              AuthorizationStatus.AUTHORIZED_DEFAULT -> stringResource(R.string.authorized_default)
              AuthorizationStatus.AUTHORIZED_ECO -> stringResource(R.string.authorized_eco)
              AuthorizationStatus.AUTHORIZED_MAX -> stringResource(R.string.authorized_max)
              AuthorizationStatus.CHARGE_STOPPED_IN_APP -> stringResource(R.string.charge_stopped_in_app)
              AuthorizationStatus.CHARGE_PAUSED -> stringResource(R.string.charge_paused)
              else -> stringResource(R.string.unknown)
            },
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.error_code),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = chargingAdvancedData.errorCode.toString(),
            style = MaterialTheme.typography.titleMedium
          )
        }
      }
    }
    Log.v(TAG, "RETURN GaaiChargingAdvancedDataCard()")
  }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiChargingAdvancedDataCardPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiChargingAdvancedDataCardPreviewLight")
@Composable
private fun GaaiChargingAdvancedDataCardPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiChargingAdvancedDataCard(
        chargingAdvancedData = ChargingAdvancedData(
          timestamp = 0x662D0EFBu, iAvailable = 6, gridPower = 34, carPower = 32,
          authorizationStatus = AuthorizationStatus(0), errorCode = 0
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small))
      )
    }
  }
}


