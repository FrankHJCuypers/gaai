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
 * Possible entry states for the ViewModel.
 *
 * @author Frank HJ Cuypers
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
 * Service data consists of a Service UUID and the Service Data associated with the service.
 * BleScanResultData's [no.nordicsemi.android.kotlin.ble.core.scanner.BleScanRecord.serviceData] returns a map of
 * service UUID and its corresponding Service Data.
 * The filter passes only devices that have the given Service UUID uuid and Service Data.
 * @property uuid The Service UUID to filter by.
 * @property data The Service Data to filter by.
 */
@Suppress("ArrayInDataClass")
data class WithServiceData(
  val uuid: ParcelUuid, val data: ByteArray
) : Filter(
  filter = { result ->
    result.data?.scanRecord?.serviceData?.get(uuid)?.value?.contentEquals(data) == true
  }
)

/**
 * ViewModel to validate and insert new devices in the Room database.
 *
 * @param devicesRepository The [DevicesRepository] to use.
 * @param bleRepository The [BleRepository] to use.
 * @constructor Called by [AppViewModelProvider][be.cuypers_ghys.gaai.ui.AppViewModelProvider].
 *
 * @author Frank HJ Cuypers
 */
class DeviceEntryViewModel(private val devicesRepository: DevicesRepository, private val bleRepository: BleRepository) :
  ViewModel() {

  /**
   * Holds current device ui state.
   */
  var deviceUiState by mutableStateOf(DeviceUiState())
    private set

  /**
   * Updates the [deviceUiState] with the value provided in the argument.
   * This method also triggers a validation for input values.
   * @param deviceDetails The initial device details from which to compute the state.
   */
  fun updateUiState(deviceDetails: DeviceDetails) {
    cancelScanDevice()
    val isSnValid = validateSn(deviceDetails)
    val isPnValid = validatePn(deviceDetails)
    deviceUiState =
      DeviceUiState(
        deviceDetails = deviceDetails, isSnValid = isSnValid, isPnValid = isPnValid,
        entryState = if (isSnValid && isPnValid) EntryState.ENTRY_VALID else EntryState.INPUTTING
      )
  }

  /**
   * Updates the [deviceUiState] with the BLE scan result.
   * @param scanResult The BLE scan result.
   */
  private suspend fun updateUiState(scanResult: BleScanResult) {
    val deviceDetails = deviceUiState.deviceDetails.copy(
      mac = scanResult.device.address,
      serviceDataValue = serviceDataFilter.data.fromUint32BE(0).toInt()
    )
    val canInsert = devicesRepository.canInsert(deviceDetails.toDevice())
    deviceUiState = deviceUiState.copy(
      deviceDetails = deviceDetails,
      entryState = if (canInsert) EntryState.DEVICE_FOUND else EntryState.DUPLICATE_DEVICE_FOUND
    )
  }

  /**
   * Updates the [deviceUiState] with the new [entryState].
   * @param entryState New state.
   */
  fun updateUiState(entryState: EntryState) {
    deviceUiState = deviceUiState.copy(entryState = entryState)
  }

  /**
   * Inserts the [Device] from [deviceUiState] in the Room database.
   */
  suspend fun saveDevice() {
    if (validateInput()) {
      devicesRepository.insertDevice(deviceUiState.deviceDetails.toDevice())
    }
  }

  /**
   * The [WithServiceData] used to filter BLE scan results.
   */
  private lateinit var serviceDataFilter: WithServiceData

  /**
   * The [Job] that is currently performing the BLE Scan.
   */
  private var currentJob: Job? = null

  /**
   * Perform a BLE scan for the [Device] that matches the [filterServiceData] filter and update the [deviceUiState]
   * when found.
   */
  @SuppressLint("MissingPermission")
  fun scanDevice() {
    if (validateInput()) {
      currentJob?.cancel()
      serviceDataFilter = getWithServiceUuidFilter()
      Log.d(TAG, "starting scan using filter $serviceDataFilter")

      /* Note from https://github.com/iDevicesInc/SweetBlue/wiki/Android-BLE-Issues:
       * "Built-in scan filtering, at least pre-lollipop, does not work.
       * You have to scan for all devices and do filtering yourself."
       * So don't pass filters to BleScanner.scan() !
       * scan() returns a Flow of BleScanResult, which consists of ServerDevice and BleScanResultData.
       */
      currentJob = bleRepository.getScannerState()
        .filter {
          filterServiceData(it)
        }
        .onEach {
          Log.d(TAG, "ble scanner found $it")
          updateUiState(it)
        }
        .cancellable()
        .launchIn(viewModelScope) //Scanning will stop after we leave the screen
    }
  }

  /**
   * Cancel the [Job] performing the BLE scan.
   */
  fun cancelScanDevice() {
    currentJob?.cancel()
  }

  /**
   * Verifies if the BLE scan result [result] matches the [serviceDataFilter].
   * @return true if the [result] matches the [serviceDataFilter].
   */
  private fun filterServiceData(result: BleScanResult): Boolean {
    val retVal = serviceDataFilter.filter(result)
//        Log.d(TAG,
//        "Filter uuid: ${serviceDataFilter.uuid}, found uuid ${result.data?.scanRecord?.serviceData}, result = $retVal");
    return retVal
  }

  /**
   * Returns the [WithServiceData] filter with the Service Data matching the [DeviceDetails.sn] specified by [uiState].
   * @param uiState [DeviceUiState] from which the [DeviceDetails.sn] is used.
   */
  private fun getWithServiceUuidFilter(uiState: DeviceDetails = deviceUiState.deviceDetails): WithServiceData =
    with(uiState) {
      val serialNumber = SerialNumberParser.parse(sn)
      val hexSerialNumber = SerialNumberParser.calcHexSerialNumber(serialNumber!!)
      val serviceData = ByteArray(4)
      serviceData.toUint32BE(0, hexSerialNumber)
      return WithServiceData(uuid = ParcelUuid(UUID_NEXXTENDER_HOME_SERVICE_DATA_SERVICE), data = serviceData)
    }

  /**
   * Validates if the device sn specified by [uiState] is a correctly formatted sn.
   * @param uiState [DeviceUiState] from which the [DeviceDetails.sn] is used.
   */
  private fun validateSn(uiState: DeviceDetails = deviceUiState.deviceDetails): Boolean {
    return with(uiState) {
      SerialNumberParser.parse(sn) != null
    }
  }

  /**
   * Validates if the device pn specified by [uiState] is a correctly formatted pn.
   * @param uiState [DeviceUiState] from which the [DeviceDetails.pn] is used.
   */
  private fun validatePn(uiState: DeviceDetails = deviceUiState.deviceDetails): Boolean {
    return with(uiState) {
      ProductNumberParser.parse(pn) != null
    }
  }

  /**
   * Validates if the device sn and pn specified by [uiState] are correctly formatted sn and pn.
   * @param uiState [DeviceUiState] from which the [DeviceDetails.sn] and [DeviceDetails.pn] aer used.
   */
  private fun validateInput(uiState: DeviceDetails = deviceUiState.deviceDetails): Boolean {
    return validateSn(uiState) && validatePn(uiState)
  }
}

/**
 * Represents the UI State for the new [Device].
 */
data class DeviceUiState(
  val deviceDetails: DeviceDetails = DeviceDetails(),
  val isSnValid: Boolean = false,
  val isPnValid: Boolean = false,
  val entryState: EntryState = EntryState.INPUTTING
)

/**
 * TODO: merge with [Device]? Note that [Device] has special annotations for Room.
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
