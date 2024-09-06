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
import be.cuypers_ghys.gaai.util.fromUint32BE
import be.cuypers_ghys.gaai.util.toUint32BE
import be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_SERVICE_DATA_SERVICE
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult

// Tag for logging
private const val TAG = "DeviceEntryViewModel"

/**
 * Possible entry states for this screen.
 */
enum class EntryState {
    /**
     * The user can input PN ans SN.
     * Buttons are still grayed out.
     */
    INPUTTING,

    /**
     * The user has specified valid PN and SN numbers. Scanning is now possible.
     */
    ENTRY_VALID,

    /**
     * The app is scanning for a match, but none found yet.
     */
    SCANNING,

    /**
     * The app found a match and will continue scanning.
     */
    DEVICE_FOUND,

    /**
     * The app found a match, but the device ia already registered.
     * The app will continue scanning.
     */
    DUPLICATE_DEVICE_FOUND
}

/**
 * Base scanner filter class.
 * @property filter The predicate applied to scanned devices.
 */
sealed class Filter(
    open val filter: (result: BleScanResult) -> Boolean,
)

/**
 * Filter by service data.
 * Service data consists of a Service UUID and the data associated with the service.
 * BleScanResultData's serviceData returns a map of service UUID and its corresponding service data.
 * The filter passes only devices that have the given service data uuid and data.
 * @property uuid The service data UUID to filter by.
 * @property data The service data to filter by.
 */
data class WithServiceData(
    val uuid: ParcelUuid, val data: ByteArray
): Filter(
    filter = { result ->
        result.data?.scanRecord?.serviceData?.get(uuid)?.value?.contentEquals(data)  == true
    }
)

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
     * @param deviceDetails The initial device details from which to compute the state.
     */
    fun updateUiState(deviceDetails: DeviceDetails) {
        cancelScanDevice()
        val isSnValid =  validateSn(deviceDetails)
        val isPnValid =  validatePn(deviceDetails)
        deviceUiState =
            DeviceUiState(deviceDetails = deviceDetails, isSnValid = isSnValid, isPnValid = isPnValid,
                entryState = if (isSnValid && isPnValid)  EntryState.ENTRY_VALID else EntryState.INPUTTING)
    }

    /**
     * Updates the [deviceUiState] with the BLE scan result.
     */
    suspend fun updateUiState(scanResult: BleScanResult) {
        val deviceDetails = deviceUiState.deviceDetails.copy(
            mac = scanResult.device.address,
            serviceDataValue = serviceDataFilter.data.fromUint32BE(0).toInt()
        )
        val canInsert = devicesRepository.canInsert(deviceDetails.toDevice())
        deviceUiState = deviceUiState.copy(deviceDetails=deviceDetails, entryState = if (canInsert) EntryState.DEVICE_FOUND else EntryState.DUPLICATE_DEVICE_FOUND)
    }

    /**
     * Updates the [deviceUiState] with the EntryState.
     */
    fun updateUiState(entryState: EntryState) {
        deviceUiState = deviceUiState.copy( entryState = entryState )
    }

    /**
     * Inserts an [Device] in the Room database
     */
    suspend fun saveDevice() {
        if (validateInput()) {
            devicesRepository.insertDevice(deviceUiState.deviceDetails.toDevice())
        }
    }

    private lateinit var serviceDataFilter : WithServiceData
    private var currentJob: Job? = null

    /**
     * Scans an [Device] using BLE
     */
    @SuppressLint("MissingPermission")
    fun scanDevice() {
        if (validateInput()) {
            currentJob?.cancel()
            serviceDataFilter = getWithServiceUuidFilter()
            Log.d(TAG, "starting scan using filter $serviceDataFilter")

            /* Note from https://github.com/iDevicesInc/SweetBlue/wiki/Android-BLE-Issues:
             * "Built-in scan filtering, at least pre-lollipop, does not work. You have to scan for all devices and do filtering yourself."
             * So don't pass filters to BleScanner.scan() !
             * scan() returns a Flow of BleScanResult, which consists of ServerDevice and BleScanResultData.
             */
            currentJob = bleRepository.scanner.scan()
                .filter{
                    filterServiceData(it)}
                .onEach{
                    Log.d(TAG, "ble scanner found $it")
                    updateUiState(it)
                }
                .cancellable()
                .launchIn(viewModelScope) //Scanning will stop after we leave the screen
        }
    }

    fun cancelScanDevice()
    {
        currentJob?.cancel()
    }

    private fun filterServiceData(result : BleScanResult)  : Boolean {
        val retVal = serviceDataFilter.filter( result )
//        Log.d(TAG, "Filter uuid: ${serviceDataFilter.uuid}, found uuid ${result.data?.scanRecord?.serviceData}, result = $retVal");
        return retVal
    }

    private fun getWithServiceUuidFilter(uiState: DeviceDetails = deviceUiState.deviceDetails): WithServiceData =
        with(uiState) {
            val serialNumber = SerialNumberParser.parse(sn)
            val hexSerialNumber = SerialNumberParser.calcHexSerialNumber(serialNumber!!)
            val serviceData = ByteArray(4)
            serviceData.toUint32BE(0, hexSerialNumber)
            return WithServiceData(uuid = ParcelUuid(UUID_NEXXTENDER_HOME_SERVICE_DATA_SERVICE), data= serviceData)
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
    val isSnValid: Boolean = false,
    val isPnValid: Boolean = false,
    val entryState : EntryState = EntryState.INPUTTING
)

/**
 * TODO: merge with [Device]?
 */
data class DeviceDetails(
    val id: Int = 0,
    val pn: String = "",
    val sn: String = "",
    val mac: String = "",
    val serviceDataValue: Int = 0
)

/**
 * Extension function to convert [DeviceDetails] to [Device].
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
fun Device.toDeviceUiState(entryState: EntryState = EntryState.INPUTTING): DeviceUiState = DeviceUiState(
    deviceDetails = this.toDeviceDetails(),
    entryState = entryState
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
