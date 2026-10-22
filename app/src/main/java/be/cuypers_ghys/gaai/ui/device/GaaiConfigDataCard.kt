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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.data.ConfigData
import be.cuypers_ghys.gaai.data.ConfigVersion
import be.cuypers_ghys.gaai.data.Mode
import be.cuypers_ghys.gaai.data.NetWorkType
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import be.cuypers_ghys.gaai.util.TouPeriod

private const val TAG = "GaaiConfigDataCard"

/**
 * Implements a [Card] displaying the [configData] with the possibility to change some of the values on
 * the Nexxtender Home device.
 *
 * @param configData The information to display and possibly change.
 * @param onTouWeekChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.touWeekStart] and [ConfigData.touWeekEnd].
 * @param onTouWeekendChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.touWeekendStart] and [ConfigData.touWeekendEnd].
 * @param onMaxGridChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.maxGrid].
 * @param onSafeChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.safe].
 * @param onMaxDeviceChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.maxDevice].
 * @param onModeChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.mode].
 * @param onICapacityChange Function to be called when [GaaiConfigDataCard] wants to change the
 *  device's [ConfigData.iCapacity].
 * @param modifier the [Modifier] to be applied to this GaaiConfigDataCard
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiConfigDataCard(
  configData: ConfigData,
  onTouWeekChange: (TouPeriod) -> Unit,
  onTouWeekendChange: (TouPeriod) -> Unit,
  onMaxGridChange: (UByte) -> Unit,
  onSafeChange: (UByte) -> Unit,
  onMaxDeviceChange: (UByte) -> Unit,
  onModeChange: (Mode) -> Unit,
  onICapacityChange: (UByte) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiConfigDataCard(configData = $configData)")
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
        var showModeDialog by remember { mutableStateOf(false) }

        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          val title = when (configData.configVersion) {
            ConfigVersion.CONFIG_1_0 -> " 1.0"
            ConfigVersion.CONFIG_1_1 -> " 1.1"
            ConfigVersion.CONFIG_CBOR -> " CBOR"
          }
          Text(
            text = stringResource(R.string.configuration) + title,
            style = MaterialTheme.typography.titleLarge,
          )
        }
        Row(
          modifier = modifier
            .fillMaxWidth()
            .clickable { showModeDialog = true },
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = if (configData.configVersion == ConfigVersion.CONFIG_CBOR) stringResource(R.string.charge_mode)
            else stringResource(R.string.mode),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = modeToText(configData.mode),
            style = MaterialTheme.typography.titleMedium
          )
          Spacer(Modifier.width(16.dp))
          Icon(
            painter = painterResource(R.drawable.outline_edit_24),
            contentDescription = stringResource(R.string.editable)
          )
          if (showModeDialog) {
            ModeDialog(
              mode = configData.mode,
              onConfirm = { newMode ->
                Log.d(TAG, "ModeDialog confirmed: $newMode")
                onModeChange(newMode)
                showModeDialog = false
              },
              onDismiss = {
                Log.v(TAG, "ModeDialog dismissed")
                showModeDialog = false
              },
              modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
            )
          }
        }

        AmpereRow(
          name = if (configData.configVersion == ConfigVersion.CONFIG_CBOR) stringResource(R.string.i_max)
          else stringResource(
            R.string.maxGrid
          ),
          value = configData.maxGrid,
          minValue = configData.safe,
          maxValue = 63u, // Largest allowed value in Belgium
          onConfirm = { newMaxGrid ->
            Log.d(TAG, "GaaiConfigDataCard Max Grid confirmed: $newMaxGrid")
            onMaxGridChange(newMaxGrid)
          },
          onDismiss = {
            Log.v(TAG, "GaaiConfigDataCard Max Grid dismissed")
          },
          modifier = modifier.fillMaxWidth()
        )

        AmpereRow(
          name = if (configData.configVersion == ConfigVersion.CONFIG_CBOR) stringResource(R.string.i_level_1)
          else stringResource(
            R.string.safe
          ),
          value = configData.safe,
          minValue = 0u,
          maxValue = configData.maxDevice,
          onConfirm = { newSafe ->
            Log.d(TAG, "GaaiConfigDataCard Safe confirmed: $newSafe")
            onSafeChange(newSafe)
          },
          onDismiss = {
            Log.v(TAG, "GaaiConfigDataCard Safe dismissed")
          },
          modifier = modifier.fillMaxWidth()
        )

        val weekDays = TouPeriod(configData.touWeekStart, configData.touWeekEnd)
        TouPeriodRow(
          title = stringResource(R.string.touWeek),
          touPeriod = weekDays,
          onDismissRequest = {
            Log.d(TAG, "GaaiConfigDataCard Weekdays TouPeriodRow dismissed")
          },
          onConfirmation = { touPeriod ->
            Log.d(TAG, "GaaiConfigDataCard Weekdays TouPeriodRow confirmed: $touPeriod")
            onTouWeekChange(touPeriod)
          },
          modifier = modifier
        )

        val weekendDays = TouPeriod(configData.touWeekendStart, configData.touWeekendEnd)
        TouPeriodRow(
          title = stringResource(R.string.touWeekend),
          touPeriod = weekendDays,
          onDismissRequest = {
            Log.v(TAG, "GaaiConfigDataCard Weekend days TouPeriodRow dismissed")
          },
          onConfirmation = { touPeriod ->
            Log.d(TAG, "GaaiConfigDataCard Weekend days TouPeriodRow confirmed: $touPeriod")
            onTouWeekendChange(touPeriod)
          },
          modifier = modifier
        )

        if (configData.configVersion != ConfigVersion.CONFIG_1_0) {
          AmpereRow(
            name = if (configData.configVersion == ConfigVersion.CONFIG_CBOR) stringResource(R.string.i_evse_max)
            else stringResource(
              R.string.maxDevice
            ),
            value = configData.maxDevice,
            minValue = configData.safe,
            maxValue = 32u,
            onConfirm = { newMaxDevice ->
              Log.d(TAG, "GaaiConfigDataCard Max Device confirmed: $newMaxDevice")
              onMaxDeviceChange(newMaxDevice)
            },
            onDismiss = {
              Log.v(TAG, "GaaiConfigDataCard Max Grid dismissed")
            },
            modifier = modifier.fillMaxWidth()
          )

          /**
           * Only show Network Time; don't allow to change it.
           * Too dangerous to change it.
           */
          Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = if (configData.configVersion == ConfigVersion.CONFIG_CBOR) "Phase Seq (Network Type)"
              else stringResource(
                R.string.networkType
              ),
              style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.weight(1f))
            Text(
              text = when (configData.networkType) {
                NetWorkType.MONO_TRIN -> stringResource(R.string.mono_tri_n)
                NetWorkType.TRI -> stringResource(R.string.tri)
                else -> stringResource(R.string.unknown)
              },
              style = MaterialTheme.typography.titleMedium,
            )
          }
        }
        if (configData.configVersion == ConfigVersion.CONFIG_CBOR) {
          Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = stringResource(R.string.i_evse_min),
              style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.weight(1f))
            Text(
              text = configData.minDevice.toString() + " A",
              style = MaterialTheme.typography.titleMedium
            )
          }

          AmpereRow(
            name = stringResource(R.string.iCapacity),
            value = configData.iCapacity,
            minValue = configData.safe,
            maxValue = configData.maxGrid,
            onConfirm = { newICapacity ->
              Log.d(TAG, "GaaiConfigDataCard I Capacity confirmed: $newICapacity")
              onICapacityChange(newICapacity)
            },
            onDismiss = {
              Log.v(TAG, "GaaiConfigDataCard I Capacity dismissed")
            },
            modifier = modifier.fillMaxWidth()
          )
        }
      }
    }
    Log.v(TAG, "RETURN GaaiConfigDataCard()")
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiConfigDataCardPreview_1_0Dark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiConfigDataCardPreview_1_0Light")
@Composable
private fun GaaiConfigDataCardPreview_1_0() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiConfigDataCard(
        configData = ConfigData(
          maxGrid = 50U,
          maxDevice = 0U,
          mode = Mode.MAX_PRIVATE,
          safe = 6U,
          networkType = NetWorkType.UNKNOWN,
          touWeekStart = 123,
          touWeekEnd = 456,
          touWeekendStart = 789,
          touWeekendEnd = 333,
          minDevice = 11U,
          iCapacity = 22U,
          configVersion = ConfigVersion.CONFIG_1_0
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {}
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiConfigDataCardPreview_1_1_Mono_TRINDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiConfigDataCardPreview_1_1_Mono_TRINLight")
@Composable
private fun GaaiConfigDataCardPreview_1_1_Mono_TRIN() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiConfigDataCard(
        configData = ConfigData(
          maxGrid = 50U,
          maxDevice = 32U,
          mode = Mode.MAX_PRIVATE,
          safe = 6U,
          networkType = NetWorkType.MONO_TRIN,
          touWeekStart = 123,
          touWeekEnd = 456,
          touWeekendStart = 789,
          touWeekendEnd = 333,
          minDevice = 11U,
          iCapacity = 22U,
          configVersion = ConfigVersion.CONFIG_1_1
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {}
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiConfigDataCardPreview_1_1_TRIDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiConfigDataCardPreview_1_1_TRILight")
@Composable
private fun GaaiConfigDataCardPreview_1_1_TRI() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiConfigDataCard(
        configData = ConfigData(
          maxGrid = 50U,
          maxDevice = 32U,
          mode = Mode.MAX_PRIVATE,
          safe = 6U,
          networkType = NetWorkType.TRI,
          touWeekStart = 123,
          touWeekEnd = 456,
          touWeekendStart = 789,
          touWeekendEnd = 333,
          minDevice = 11U,
          iCapacity = 22U,
          configVersion = ConfigVersion.CONFIG_1_1
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {}
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiConfigDataCardCborPreview_MONO_TRINDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiConfigDataCardCborPreview_MONO_TRINLight")
@Composable
private fun GaaiConfigDataCardCborPreview_MONO_TRIN() {
  GaaiTheme(dynamicColor = false) {
    Surface()
    {
      GaaiConfigDataCard(
        configData = ConfigData(
          maxGrid = 50U,
          maxDevice = 32U,
          mode = Mode.MAX_PRIVATE,
          safe = 6U,
          networkType = NetWorkType.MONO_TRIN,
          touWeekStart = 123,
          touWeekEnd = 456,
          touWeekendStart = 789,
          touWeekendEnd = 333,
          minDevice = 11U,
          iCapacity = 22U,
          configVersion = ConfigVersion.CONFIG_CBOR
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {}
      )
    }
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiConfigDataCardCborPreview_TRIDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiConfigDataCardCborPreview_TRILight")
@Composable
private fun GaaiConfigDataCardCborPreview_TRI() {
  GaaiTheme(dynamicColor = false) {
    Surface()
    {
      GaaiConfigDataCard(
        configData = ConfigData(
          maxGrid = 50U,
          maxDevice = 32U,
          mode = Mode.MAX_PRIVATE,
          safe = 6U,
          networkType = NetWorkType.TRI,
          touWeekStart = 123,
          touWeekEnd = 456,
          touWeekendStart = 789,
          touWeekendEnd = 333,
          minDevice = 11U,
          iCapacity = 22U,
          configVersion = ConfigVersion.CONFIG_CBOR
        ),
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
        onTouWeekChange = {},
        onTouWeekendChange = {},
        onMaxGridChange = {},
        onMaxDeviceChange = {},
        onICapacityChange = {},
        onSafeChange = {},
        onModeChange = {}
      )
    }
  }
}
