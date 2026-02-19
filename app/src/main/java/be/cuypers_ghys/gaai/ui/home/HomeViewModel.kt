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

package be.cuypers_ghys.gaai.ui.home

import android.annotation.SuppressLint
import android.os.ParcelUuid
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.cuypers_ghys.gaai.ble.BleRepository
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.DevicesRepository
import be.cuypers_ghys.gaai.ui.device.GaaiBondState
import be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_SERVICE_DATA_SERVICE
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BondState
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult
import kotlin.time.Duration.Companion.seconds

// Tag for logging
private const val TAG = "HomeViewModel"

/**
 * ViewModel to manage all devices from the Room database, to be used by [HomeScreen].
 * @param devicesRepository The [DevicesRepository] to use.
 * @param bleRepository The [BleRepository] to use.
 * @constructor Called by [AppViewModelProvider][be.cuypers_ghys.gaai.ui.AppViewModelProvider].
 *
 * @author Frank HJ Cuypers
 */
class HomeViewModel(private val devicesRepository: DevicesRepository, private val bleRepository: BleRepository) :
  ViewModel() {

  init {
    Log.v(TAG, "ENTRY init()")
//    scanDevice()
    Log.v(TAG, "RETURN init()")
  }

  /**
   * Remove the [device] from the Room database.
   * @param device The [Device] to delete.
   */
  fun removeDevice(device: Device) {
    Log.d(TAG, "ENTRY removeDevice($device)")

    viewModelScope.launch {
      Log.d(TAG, "ENTRY removeDevice($device) body")
      devicesRepository.deleteDevice(device)
      Log.v(TAG, "EXIT removeDevice() body")
    }
    Log.v(TAG, "EXIT removeDevice()")
  }

  /**
   * Base scanner filter class.
   * @property filter The predicate applied to scanned devices.
   */
  sealed class Filter(
    open val filter: (result: BleScanResult) -> Boolean,
  )

  /**
   * Filter by Service UUID.
   * BleScanResultData's [no.nordicsemi.android.kotlin.ble.core.scanner.BleScanRecord.serviceUuids] returns a list of
   * service UUID.
   * The filter passes only devices that have the given Service UUID uuid .
   * @property uuid The Service UUID to filter by.
   */
  @Suppress("ArrayInDataClass")
  data class WithServiceUuid(
    val uuid: ParcelUuid
  ) : Filter(
    filter = { result ->
      result.data?.scanRecord?.serviceData?.containsKey(uuid) == true
    }
  )

  /**
   * The list of devices retrieved from [DevicesRepository].
   */
  private val deviceList: Flow<List<Device>> =
    devicesRepository.getAllDevicesStream()

  private var _advertisingDeviceList = MutableStateFlow<List<ServerDevice>>(emptyList())
  val advertisingDeviceList = _advertisingDeviceList.asStateFlow()

  /**
   * The [WithServiceUuid] used to filter BLE scan results.
   */
  private lateinit var serviceUuidFilter: WithServiceUuid

  /**
   * The [Job] that is currently performing the BLE Scan.
   */
  private var currentJob: Job? = null

  /**
   * The [Job] that performs the [aggregator] cleaning
   */
  private var cleaningJob: Job? = null

  /**
   * Holds home ui state. Combines
   * The list of devices are retrieved from [DevicesRepository] and mapped to [HomeUiState].
   */
  @SuppressLint("MissingPermission")
  val homeUiState: StateFlow<HomeUiState> =
    deviceList.combine(advertisingDeviceList) { deviceList, advertisingDeviceList ->
      val deviceStateList = mutableStateListOf<DeviceState>()
      deviceList.forEach {
        deviceStateList.add(DeviceState(it, GaaiBondState.getBondState(bleRepository.context, it)))
      }
      HomeUiState(deviceStateList, advertisingDeviceList)
    }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = HomeUiState()
      )

  // Create aggregator which will concat scan records with a device
  private var aggregator: BleScanResultAggregatorCleaner? = null


  /**
   * Perform a BLE scan for the [Device]s that matches the [filterServiceUuid] filter and update the [_advertisingDeviceList]
   * when found.
   */
  @SuppressLint("MissingPermission")
  fun scanDevice() {
    Log.v(TAG, "ENTRY scanDevice()")
    currentJob?.cancel()
    cleaningJob?.cancel()
    serviceUuidFilter = getWithServiceUuidFilter()
    Log.d(TAG, "starting scan using filter $serviceUuidFilter")

//    aggregator?.stopCleaning()
    aggregator = BleScanResultAggregatorCleaner()
//    aggregator!!.startCleaning { _advertisingDeviceList.emit(it) }

    /* Note from https://github.com/iDevicesInc/SweetBlue/wiki/Android-BLE-Issues:
     * "Built-in scan filtering, at least pre-lollipop, does not work.
     * You have to scan for all devices and do filtering yourself."
     * So don't pass filters to BleScanner.scan() !
     * scan() returns a Flow of BleScanResult, which consists of ServerDevice and BleScanResultData.
     */
    currentJob = bleRepository.getScannerState()
      .filter {
        filterServiceUuid(it)
      }
      .map { aggregator!!.aggregateDevices(it) } //Add new device and return an aggregated list
      .onEach {
        Log.d(TAG, "ble scanner found $it")
        _advertisingDeviceList.emit(it)
      }
      .cancellable()
      .launchIn(viewModelScope) //Scanning will stop after we leave the screen

    cleaningJob = tickerFlow(1.seconds, 2.seconds)
      .onEach {
        Log.v(TAG, "cleaning job $it")
        val cleaned = aggregator?.clean()
        val results = aggregator?.results
        Log.v(TAG, "cleaned = $cleaned, results = $results")
        if ((cleaned == true) && (results != null)) {
          val listOfServerDevices = results.map { it.device }
          Log.v(TAG, "listOfServerDevices = $listOfServerDevices")
          _advertisingDeviceList.emit(listOfServerDevices)
        }
      }.launchIn(viewModelScope)
    Log.d(TAG, "RETURN scanDevice()")
  }

  /**
   * Cancel the [Job] performing the BLE scan.
   */
  fun cancelScanDevice() {
    Log.v(TAG, "ENTRY cancelScanDevice()")
    currentJob?.cancel()
    Log.v(TAG, "RETURN cancelScanDevice()")
  }

  /**
   * Verifies if the BLE scan result [result] matches the [serviceUuidFilter].
   * @param result the scan result to filter
   * @return true if the [result] matches the [serviceUuidFilter].
   */
  private fun filterServiceUuid(result: BleScanResult): Boolean {
    Log.v(TAG, "ENTRY filterServiceUuid(), result = $result")
    val retVal = serviceUuidFilter.filter(result)
    Log.v(TAG, "RETURN filterServiceUuid(), filter $retVal")
    return retVal
  }

  /**
   * Returns the [WithServiceUuid] filter.
   */
  private fun getWithServiceUuidFilter(): WithServiceUuid {
    Log.v(TAG, "ENTRY getWithServiceUuidFilter()")
    return WithServiceUuid(uuid = ParcelUuid(UUID_NEXXTENDER_CHARGER_SERVICE_DATA_SERVICE))
  }

  companion object {
    /**
     * Delay (in milliseconds) between the disappearance of the last
     * subscriber and the stopping of the sharing coroutine.
     */
    private const val TIMEOUT_MILLIS = 5_000L

    /**
     * Determines if the [device] is advertising.
     * @param device
     * @param advertisingDeviceList
     * @return true if the [device] is also present in [advertisingDeviceList]
     */
    fun isAdvertising(device: Device, advertisingDeviceList: List<ServerDevice>): Boolean {
      advertisingDeviceList.forEach {
        if (it.address == device.mac) {
          return true
        }
      }

      return false
    }
  }
}

data class DeviceState(
  val device: Device = Device(),
  val bondState: BondState = BondState.NONE
)

/**
 * Ui State for HomeScreen
 */
data class HomeUiState(
  val deviceStateList: List<DeviceState> = listOf(),
//  val deviceList: List<Device> = listOf(),
  val advertisingDeviceList: List<ServerDevice> = listOf()
)
