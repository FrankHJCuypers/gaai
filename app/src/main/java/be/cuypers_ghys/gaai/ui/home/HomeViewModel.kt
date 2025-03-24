/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2024, Frank HJ Cuypers
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

package be.cuypers_ghys.gaai.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.DevicesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel to manage all devices from the Room database, to be used by [HomeScreen].
 * @param devicesRepository The [DevicesRepository] to use.
 * @constructor Called by [AppViewModelProvider][be.cuypers_ghys.gaai.ui.AppViewModelProvider].
 *
 * @author Frank HJ Cuypers
 */
class HomeViewModel(private val devicesRepository: DevicesRepository) : ViewModel() {

  /**
   * Remove the [device] from the Room database.
   * @param device The [Device] to delete.
   */
  suspend fun removeDevice(device: Device) {
    devicesRepository.deleteDevice(device)
  }

  /**
   * Holds home ui state. The list of devices are retrieved from [DevicesRepository] and mapped to
   * [HomeUiState]
   */
  val homeUiState: StateFlow<HomeUiState> =
    devicesRepository.getAllDevicesStream().map { HomeUiState(it) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = HomeUiState()
      )

  companion object {
    /**
     * Delay (in milliseconds) between the disappearance of the last
     * subscriber and the stopping of the sharing coroutine.
     */
    private const val TIMEOUT_MILLIS = 5_000L
  }
}

/**
 * Ui State for HomeScreen
 */
data class HomeUiState(val deviceList: List<Device> = listOf())
