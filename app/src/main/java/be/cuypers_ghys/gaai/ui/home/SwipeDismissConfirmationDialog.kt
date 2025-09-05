/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2025, Frank HJ Cuypers
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
 *
 */
package be.cuypers_ghys.gaai.ui.home

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import be.cuypers_ghys.gaai.R
import be.cuypers_ghys.gaai.ui.theme.GaaiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * This function is mostly copied from
 * https://stackoverflow.com/a/79055503/1366319.
 * See answer from https://stackoverflow.com/questions/78638403/reset-of-swipetodismissboxstate-not-working
 */
@Composable
fun <T> SwipeToDismissContainer(
  item: T,
  itemName: String,
  confirmationDialog: Boolean = true,
  onDismiss: (T, onError: () -> Unit) -> Unit,
  content: @Composable (T) -> Unit
) {
  var potentialDelete by remember { mutableStateOf(false) }
  var deleteItem by remember { mutableStateOf(false) }
  var stateToMaintain by remember { mutableStateOf<SwipeToDismissBoxValue?>(null) }

  val state = rememberSwipeToDismissBoxState(
    confirmValueChange = { dismissValue ->
      when (dismissValue) {
        SwipeToDismissBoxValue.EndToStart,
        SwipeToDismissBoxValue.StartToEnd -> {
          if (confirmationDialog) {
            potentialDelete = true
          } else {
            potentialDelete = false
            deleteItem = true
          }
          stateToMaintain = dismissValue
        }

        else -> {}
      }
      false //Immediately resets the state so we can swipe it again if confirmation is canceled or if deletion fails
    }
  )

  //Maintains the row's swiped state while it waits for confirmation and for AnimatedVisibility to hide the item
  LaunchedEffect(stateToMaintain) {
    stateToMaintain?.let {
      state.snapTo(it)
      stateToMaintain = null
    }
  }

  LaunchedEffect(deleteItem) {
    if (deleteItem) {
      state.reset()
      onDismiss(item) {
        deleteItem = false
      }
    } else {
      //In our app, the onDismiss function also takes in an onError: () -> Unit,
      //which allows us to bring the item back if deletion fails
      state.reset()
    }
  }

  SwipeToDismissBox(
    state = state,
    backgroundContent = { DismissBackground(state) },
    content = {
      content(item)
    }
  )

  val scope = rememberCoroutineScope()
  if (confirmationDialog && potentialDelete) {
    DeleteConfirmationDialog(
      itemName = itemName,
      onCancel = {
        potentialDelete = false
        scope.launch { state.reset() } //reset() seems to only reset the visual state, not the full state object
      },
      onConfirm = {
        potentialDelete = false
        deleteItem = true
      }
    )
  }
}

/**
 * A [androidx.compose.foundation.layout.Row] with the background to show when swiping the [SwipeToDismissBox] displaying the [GaaiDeviceItem]
 * to the right.
 * @param dismissState State if the [SwipeToDismissBox].
 *
 * @author Frank HJ Cuypers
 */
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
  val color = when (dismissState.dismissDirection) {
    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.error
    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
    SwipeToDismissBoxValue.Settled -> Color.Transparent
  }

  Row(
    modifier = Modifier
      .fillMaxSize()
      .background(color)
      .padding(12.dp, 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Icon(
      Icons.Default.Delete,
      contentDescription = stringResource(R.string.delete)
    )
    Spacer(modifier = Modifier)
  }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES, name = "DevicePreviewUNKNOWNDark")
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, name = "DevicePreviewUNKNOWNLight")
@Composable
fun SwipeToDismissContainerPreview() {
  GaaiTheme(dynamicColor = false) {
    Surface {
      val list = (1..100).toList()
      val scope = rememberCoroutineScope()
      Column {
        list.forEach {
          SwipeToDismissContainer(
            it,
            it.toString(),
            onDismiss = { _, onError ->
              scope.launch {
                delay(1000)
                onError()
              }
            }
          ) { item ->
            Text(
              text = item.toString(),
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            )
          }
        }
      }
    }
  }
}

