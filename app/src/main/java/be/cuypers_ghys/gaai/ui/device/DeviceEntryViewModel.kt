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

import android.annotation.SuppressLint
import android.os.ParcelUuid
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.cuypers_ghys.gaai.ble.BleRepository
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.DevicesRepository
import be.cuypers_ghys.gaai.util.ProductNumberParser
import be.cuypers_ghys.gaai.util.SerialNumberParser
import be.cuypers_ghys.gaai.util.toUint32LE
import be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_SERVICE_DATA_SERVICE
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanFilter
import no.nordicsemi.android.kotlin.ble.core.scanner.FilteredServiceData
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator

// Tag for logging
private const val TAG = "DeviceEntryViewModel"

/**
 * ViewModel to validate and insert devices in the Room database.
 */
class DeviceEntryViewModel(private val devicesRepository: DevicesRepository, private val bleRepository: BleRepository) : ViewModel() {

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

    /**
     * Scans an [Device] using BLE
     */
    @SuppressLint("MissingPermission")
    fun scanDevice() {
        if (validateInput()) {
            val serviceData = getFilteredDataService()
            Log.d(TAG, "starting scan using filter $serviceData")
            val aggregator = BleScanResultAggregator()
            bleRepository.scanner.scan(listOf(BleScanFilter(serviceData= serviceData)))
                .map { aggregator.aggregateDevices(it) } //Add new device and return an aggregated list
                .onEach{ Log.d(TAG, "ble scanner found $it")}
                .launchIn(viewModelScope) //Scanning will stop after we leave the screen
        }
    }

    private fun getFilteredDataService(uiState: DeviceDetails = deviceUiState.deviceDetails): FilteredServiceData {
        return with(uiState) {
            val serialNumber = SerialNumberParser.parse(sn)
            val hexSerialNumber = SerialNumberParser.calcHexSerialNumber(serialNumber!!)
            val serviceData = ByteArray(4)
            serviceData.toUint32LE(0, hexSerialNumber)
            return FilteredServiceData(uuid = ParcelUuid(UUID_NEXXTENDER_HOME_SERVICE_DATA_SERVICE), data = DataByteArray(serviceData))
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
