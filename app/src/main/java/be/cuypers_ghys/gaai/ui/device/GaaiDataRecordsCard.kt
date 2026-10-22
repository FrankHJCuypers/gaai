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
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme

private const val TAG = "GaaiDataRecordsCard"

/**
 * State of the GaaiDataRecordsCard ui
 *
 */
enum class GaaiRecordsState {
  NOTHING_TO_LOAD,
  SOMETHING_TO_LOAD,
  READ_REMAINING_RECORDS_ASKED,
  READ_REMAINING_RECORDS_CONFIRMED,
  HAS_LOG_REC_DIR,
  WAITING_FOR_COMPLETION
}

/**
 * Implements a [Card] displaying the [dataRecordsInformation] with the possibility to read the records
 * to a file.
 *
 * @param dataRecordsInformation The records state
 * @param onRecordsSync Function to be called when [GaaiDataRecordsCard] wants to read the
 *  device's records from the mobile phone.
 * @param modifier the [Modifier] to be applied to this [GaaiDataRecordsCard]
 *
 * @author Frank HJ Cuypers
 */
@Composable
internal fun GaaiDataRecordsCard(
  dataRecordsInformation: DataRecordsInformation,
  onRecordsSync: (Uri) -> Unit,
  modifier: Modifier = Modifier
) {
  Log.d(TAG, "ENTRY GaaiDataRecordsCard(dataRecordsInformation = $dataRecordsInformation)")

  var recordsState by remember {
    mutableStateOf<GaaiRecordsState?>(GaaiRecordsState.NOTHING_TO_LOAD)
  }

  var logRecDirUri by remember {
    mutableStateOf<Uri?>(null)
  }

  val logDirPicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocumentTree(),
    onResult = { uri ->
      recordsState = if (uri != null) GaaiRecordsState.HAS_LOG_REC_DIR else GaaiRecordsState.NOTHING_TO_LOAD
      logRecDirUri = uri
    }
  )

  if (recordsState == GaaiRecordsState.HAS_LOG_REC_DIR) {
    recordsState = GaaiRecordsState.WAITING_FOR_COMPLETION
    onRecordsSync(logRecDirUri!!)
  }

  if (recordsState == GaaiRecordsState.READ_REMAINING_RECORDS_ASKED) {
    ReadRemainingRecordsConfirmationDialog(
      onCancel = {
        recordsState = GaaiRecordsState.SOMETHING_TO_LOAD
      },
      onConfirm = {
        recordsState = GaaiRecordsState.READ_REMAINING_RECORDS_CONFIRMED
      }
    )
  }

  if (recordsState == GaaiRecordsState.READ_REMAINING_RECORDS_CONFIRMED) {
    val authority = "com.android.externalstorage.documents"
    val documentId = "primary:Documents"
    val initialUri = DocumentsContract.buildTreeDocumentUri(authority, documentId)
    logDirPicker.launch(initialUri)
  }

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
            text = stringResource(R.string.records),
            style = MaterialTheme.typography.titleLarge,
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.remaining_cdr_records),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = if (dataRecordsInformation.remainingCDRRecords == -1) stringResource(R.string.not_loaded_yet) else dataRecordsInformation.remainingCDRRecords.toString(),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.remaining_ccdt_records),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = if (dataRecordsInformation.remainingCCDTRecords == -1) stringResource(R.string.not_loaded_yet) else dataRecordsInformation.remainingCCDTRecords.toString(),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.remaining_event_records),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = if (dataRecordsInformation.remainingEventRecords == -1) stringResource(R.string.not_loaded_yet) else dataRecordsInformation.remainingEventRecords.toString(),
            style = MaterialTheme.typography.titleMedium
          )
        }
        Row(
          modifier = modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = stringResource(R.string.remaining_metrics_records),
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(Modifier.weight(1f))
          Text(
            text = if (dataRecordsInformation.remainingMetricRecords == -1) stringResource(R.string.not_loaded_yet) else dataRecordsInformation.remainingMetricRecords.toString(),
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
            Log.v(TAG, "GaaiDataRecordsCard Get Remaining Records pressed")
            recordsState = GaaiRecordsState.READ_REMAINING_RECORDS_ASKED
          }
          ) {
            Text(stringResource(R.string.read_remaining_records))
          }
          Spacer(Modifier.weight(1f))
        }

        if (recordsState == GaaiRecordsState.WAITING_FOR_COMPLETION) {
          if ((dataRecordsInformation.remainingCDRRecords == 0)
            && (dataRecordsInformation.remainingCCDTRecords == 0)
            && (dataRecordsInformation.remainingEventRecords == 0)
            && (dataRecordsInformation.remainingMetricRecords == 0)
          ) {
            recordsState = GaaiRecordsState.NOTHING_TO_LOAD
          } else {
            Row(
              modifier = modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = stringResource(R.string.wait_for_completion),
                style = MaterialTheme.typography.titleSmall
              )
            }
          }
        }
      }
    }
    Log.v(TAG, "RETURN GaaiDataRecordsCard()")
  }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "GaaiRecordsCardPreviewDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "GaaiRecordsCardPreviewLight")
@Composable
private fun GaaiDataRecordsCardPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      GaaiDataRecordsCard(
        dataRecordsInformation = DataRecordsInformation(2, 4, 6, 11),
        onRecordsSync = {},
        modifier = Modifier
          .padding(dimensionResource(id = R.dimen.padding_small)),
      )
    }
  }
}

