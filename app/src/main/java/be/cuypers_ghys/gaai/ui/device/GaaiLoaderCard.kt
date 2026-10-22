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
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_AUTO
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_DEFAULT
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_ECO
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_MAX
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_STOP_CHARGING
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme

private const val TAG = "GaaiLoaderCard"

/**
 * Implements a [Card] allowing to select one of the Nexxtender Home's loader operations.
 * @param onLoaderOperation Function to be called when [GaaiLoaderCard] wants to perform a loader operation.
 * @param modifier the [Modifier] to be applied to this [GaaiLoaderCard]
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiLoaderCard(
  onLoaderOperation: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.v(TAG, "ENTRY GaaiLoaderCard()")
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
            text = stringResource(R.string.loader),
            style = MaterialTheme.typography.titleLarge,
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Button(onClick = {
            Log.v(TAG, "GaaiLoaderCard Start Charge Default")
            onLoaderOperation(LOADER_OPERATION_START_CHARGING_DEFAULT)
          }
          ) {
            Text(stringResource(R.string.start_charge_default))
          }
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Button(onClick = {
            Log.v(TAG, "GaaiLoaderCard Start Charge Max")
            onLoaderOperation(LOADER_OPERATION_START_CHARGING_MAX)
          }
          ) {
            Text(stringResource(R.string.start_charge_max))
          }
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Button(onClick = {
            Log.v(TAG, "GaaiLoaderCard Start Charge Auto")
            onLoaderOperation(LOADER_OPERATION_START_CHARGING_AUTO)
          }
          ) {
            Text(stringResource(R.string.start_charge_auto))
          }
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Button(onClick = {
            Log.v(TAG, "GaaiLoaderCard Start Charge Eco")
            onLoaderOperation(LOADER_OPERATION_START_CHARGING_ECO)
          }
          ) {
            Text(stringResource(R.string.start_charge_eco))
          }
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        )
        {
          Button(onClick = {
            Log.v(TAG, "GaaiLoaderCard Stop Charge")
            onLoaderOperation(LOADER_OPERATION_STOP_CHARGING)
          }
          ) {
            Text(stringResource(R.string.stop_charging))
          }
        }

      }
    }
    Log.v(TAG, "RETURN GaaiLoaderCard()")
  }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiLoaderCardPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiLoaderCardPreviewLight")
@Composable
private fun GaaiLoaderCardPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiLoaderCard(
        onLoaderOperation = {},
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
      )
    }
  }
}


