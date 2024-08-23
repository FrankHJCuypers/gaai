/*
 * Project Gaai: one app to control the Nexxtender Home charger.
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

package be.cuypers_ghys.gaai.ui.device

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.DevicesRepository
import be.cuypers_ghys.gaai.util.ProductNumberParser
import be.cuypers_ghys.gaai.util.SerialNumberParser

/**
 * ViewModel to validate and insert devices in the Room database.
 */
class DeviceEntryViewModel(private val devicesRepository: DevicesRepository) : ViewModel() {

    /**
     * Holds current device ui state
     */
    var deviceUiState by mutableStateOf(DeviceUiState())
        private set

    /**
     * Updates the [deviceUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(deviceDetails: DeviceDetails) {
        val isSnValid =  validateSn(deviceDetails)
        val isPnValid =  validatePn(deviceDetails)
        deviceUiState =
            DeviceUiState(deviceDetails = deviceDetails, isSnValid = isSnValid, isPnValid = isPnValid, isEntryValid = isSnValid && isPnValid )
    }

    /**
     * Inserts an [Device] in the Room database
     */
    suspend fun saveDevice() {
        if (validateInput()) {
            devicesRepository.insertDevice(deviceUiState.deviceDetails.toDevice())
        }
    }

    private fun validateSn(uiState: DeviceDetails = deviceUiState.deviceDetails): Boolean {
        return with(uiState) {
            SerialNumberParser.parse(sn) != null
        }
    }

    private fun validatePn(uiState: DeviceDetails = deviceUiState.deviceDetails): Boolean {
        return with(uiState) {
            ProductNumberParser.parse(pn) != null
        }
    }

    private fun validateInput(uiState: DeviceDetails = deviceUiState.deviceDetails): Boolean {
        return validateSn(uiState) && validatePn(uiState)
    }
}

/**
 * Represents Ui State for a Device.
 */
data class DeviceUiState(
    val deviceDetails: DeviceDetails = DeviceDetails(),
    val isEntryValid: Boolean = false,
    val isSnValid: Boolean = false,
    val isPnValid: Boolean = false
)

data class DeviceDetails(
    val id: Int = 0,
    val pn: String = "",
    val sn: String = "",
    val mac: String = "",
    val serviceDataValue: Int = 0
)

/**
 * Extension function to convert [DeviceUiState] to [Device].
 */
fun DeviceDetails.toDevice(): Device = Device(
    id = id,
    pn = pn,
    sn = sn,
    mac = mac,
    serviceDataValue = serviceDataValue
)

/**
 * Extension function to convert [Device] to [DeviceUiState]
 */
fun Device.toDeviceUiState(isEntryValid: Boolean = false): DeviceUiState = DeviceUiState(
    deviceDetails = this.toDeviceDetails(),
    isEntryValid = isEntryValid
)

/**
 * Extension function to convert [Device] to [DeviceDetails]
 */
fun Device.toDeviceDetails(): DeviceDetails = DeviceDetails(
    id = id,
    pn = pn,
    sn = sn,
    mac = mac,
    serviceDataValue = serviceDataValue
)
