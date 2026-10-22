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
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme

private const val TAG = "GaaiDeviceInformationCard"

/**
 * Implements a [Card] displaying the [deviceInformation].
 * @param deviceInformation The information to display.
 * @param chargerType
 * @param modifier the [Modifier] to be applied to this GaaiDeviceInformationCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiDeviceInformationCard(
  deviceInformation: DeviceInformation, chargerType: ChargerType, modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiDeviceInformationCard(deviceInformation = $deviceInformation)")
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
            text = stringResource(R.string.device_information),
            style = MaterialTheme.typography.titleLarge,
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.model_number),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = deviceInformation.modelNumber,
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.serial_number),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = deviceInformation.serialNumber,
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.firmware_revision),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = deviceInformation.firmwareRevision,
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.hardware_revision),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = deviceInformation.hardwareRevision,
            style = MaterialTheme.typography.titleMedium
          )
        }

        if (chargerType != ChargerType.HOME) {
          Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = stringResource(R.string.manufacturer_name),
              style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.weight(1f))
            Text(
              text = deviceInformation.manufacturerName,
              style = MaterialTheme.typography.titleMedium
            )
          }
        }
      }
    }
    Log.v(TAG, "RETURN GaaiDeviceInformationCard()")
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DeviceInformationPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DeviceInformationPreviewLight")
@Composable
private fun DeviceInformationPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiDeviceInformationCard(
        deviceInformation = DeviceInformation(
          modelNumber = "12345", serialNumber = "67890",
          firmwareRevision = "1.23.4", hardwareRevision = "A1", manufacturerName = "Frank"
        ),
        ChargerType.MOBILE_BLACK,
        modifier = Modifier
          .padding(
            dimensionResource(id = R.dimen.padding_small)
          ),
      )
    }
  }
}

