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
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.cuypers_ghys.gaai.ble.BleRepository
import be.cuypers_ghys.gaai.data.ChargingAdvancedData
import be.cuypers_ghys.gaai.data.ChargingAdvancedDataParser
import be.cuypers_ghys.gaai.data.ChargingBasicData
import be.cuypers_ghys.gaai.data.ChargingBasicDataParser
import be.cuypers_ghys.gaai.data.ChargingCarData
import be.cuypers_ghys.gaai.data.ChargingCarDataParser
import be.cuypers_ghys.gaai.data.ChargingGridData
import be.cuypers_ghys.gaai.data.ChargingGridDataParser
import be.cuypers_ghys.gaai.data.ConfigData
import be.cuypers_ghys.gaai.data.ConfigDataParserComposer
import be.cuypers_ghys.gaai.data.ConfigDataParserComposer.getConfigVersion
import be.cuypers_ghys.gaai.data.ConfigVersion
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.DevicesRepository
import be.cuypers_ghys.gaai.data.Mode
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_OPERATION_CBOR_GET
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_OPERATION_CBOR_SET
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_OPERATION_GET
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_OPERATION_SET
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_STATUS_POPPED
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_STATUS_READY
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_STATUS_READY_CBOR
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_STATUS_SUCCESS
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_STATUS_SUCCESS_CBOR
import be.cuypers_ghys.gaai.util.TouPeriod
import be.cuypers_ghys.gaai.util.fromUint16LE
import be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

// Tag for logging
private const val TAG = "DeviceDetailsViewModel"

fun DataByteArray.Companion.fromUShort(command: Int): DataByteArray {
  return from((command and 0xFF).toByte(), ((command shr 8) and 0xFF).toByte())
}

/**
 * ViewModel to show the device details.
 */
class DeviceDetailsViewModel(
  savedStateHandle: SavedStateHandle,
  private val devicesRepository: DevicesRepository,
  private val bleRepository: BleRepository
) : ViewModel() {

  private val deviceId: Int = checkNotNull(savedStateHandle[DeviceDetailsDestination.DEVICE_ID_ARG])
  private val gaaiDevice = getDevice(deviceId)!!

  private val _device = MutableStateFlow<Device?>(null)
  val device = _device.asStateFlow()

  private val _state = MutableStateFlow(DeviceDetailsViewState())
  val state = _state.asStateFlow()

  private var client: ClientBleGatt? = null

  init {
    _device.value = gaaiDevice
    startGattClient(gaaiDevice)
  }

  private lateinit var deviceNameCharacteristic: ClientBleGattCharacteristic
  private lateinit var modelNumberStringCharacteristic: ClientBleGattCharacteristic
  private lateinit var serialNumberStringCharacteristic: ClientBleGattCharacteristic
  private lateinit var firmwareRevisionStringCharacteristic: ClientBleGattCharacteristic
  private lateinit var hardwareRevisionStringCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeChargingBasicDataCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeChargingGridDataCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeChargingCarDataCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeChargingAdvancedDataCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeGenericCommandCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeGenericStatusCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeGenericDataCharacteristic: ClientBleGattCharacteristic
  private lateinit var configVersion: ConfigVersion

  /** New configuration value to be written to GENERIC_DATA*/
  private lateinit var newConfigData: ConfigData

  @SuppressLint("MissingPermission")
  private fun startGattClient(gaaiDevice: Device) = viewModelScope.launch {
    Log.d(TAG, "Starting Gatt Client for gaaiDevice: $gaaiDevice")

    //Connect a Bluetooth LE device.
    val client = bleRepository.getClientBleGattConnection(gaaiDevice.mac, viewModelScope).also {
      this@DeviceDetailsViewModel.client = it
    }

    if (!client.isConnected) {
      Log.d(TAG, "Gatt Client not connected.")
      return@launch
    }
    Log.d(TAG, "Gatt Client connected. Discovering services.")

    /*
     * Bluetooth caches the BLE GATT table.
     * That can be a problem when analyzing the protocol with WireShark,
     * because from the log, it can not map handles to UUIDs.
     * If mapping is required for debugging purposes, uncomment the next line.
     */
    // client.clearServicesCache()

    //Discover services on the Bluetooth LE Device.
    val services = client.discoverServices()
    configureGatt(services)
  }

  @SuppressLint("MissingPermission")
  private suspend fun configureGatt(services: ClientBleGattServices) {
    Log.d(TAG, "Found the following services: $services")

    // Remember needed service and characteristics which are used to communicate with the DK.
    val genericAccessService = services.findService(NexxtenderHomeSpecification.UUID_BLE_GENERIC_ACCESS_SERVICE)!!
    deviceNameCharacteristic =
      genericAccessService.findCharacteristic(NexxtenderHomeSpecification.UUID_BLE_DEVICE_NAME_CHARACTERISTIC)!!

    val deviceInformationService =
      services.findService(NexxtenderHomeSpecification.UUID_BLE_DEVICE_INFORMATION_SERVICE)!!
    modelNumberStringCharacteristic =
      deviceInformationService.findCharacteristic(NexxtenderHomeSpecification.UUID_BLE_MODEL_NUMBER_STRING_CHARACTERISTIC)!!
    serialNumberStringCharacteristic =
      deviceInformationService.findCharacteristic(NexxtenderHomeSpecification.UUID_BLE_SERIAL_NUMBER_STRING_CHARACTERISTIC)!!
    firmwareRevisionStringCharacteristic =
      deviceInformationService.findCharacteristic(NexxtenderHomeSpecification.UUID_BLE_FIRMWARE_REVISION_STRING_CHARACTERISTIC)!!
    hardwareRevisionStringCharacteristic =
      deviceInformationService.findCharacteristic(NexxtenderHomeSpecification.UUID_BLE_HARDWARE_REVISION_STRING_CHARACTERISTIC)!!

    val nexxtenderGenericService =
      services.findService(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_CDR_SERVICE)!!
    nexxtenderHomeChargingBasicDataCharacteristic =
      nexxtenderGenericService.findCharacteristic(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_BASIC_DATA_CHARACTERISTIC)!!
    nexxtenderHomeChargingGridDataCharacteristic =
      nexxtenderGenericService.findCharacteristic(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_GRID_DATA_CHARACTERISTIC)!!
    nexxtenderHomeChargingCarDataCharacteristic =
      nexxtenderGenericService.findCharacteristic(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_CAR_DATA_CHARACTERISTIC)!!
    nexxtenderHomeChargingAdvancedDataCharacteristic =
      nexxtenderGenericService.findCharacteristic(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_ADVANCED_DATA_CHARACTERISTIC)!!
    nexxtenderHomeGenericCommandCharacteristic =
      nexxtenderGenericService.findCharacteristic(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC)!!
    nexxtenderHomeGenericStatusCharacteristic =
      nexxtenderGenericService.findCharacteristic(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_STATUS_CHARACTERISTIC)!!
    nexxtenderHomeGenericDataCharacteristic =
      nexxtenderGenericService.findCharacteristic(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC)!!

    // Read static information
    val deviceName = deviceNameCharacteristic.read().value.toString(Charsets.UTF_8)
    val modelNumber = modelNumberStringCharacteristic.read().value.toString(Charsets.UTF_8)
    val serialNumber = serialNumberStringCharacteristic.read().value.toString(Charsets.UTF_8)
    val firmwareRevision = firmwareRevisionStringCharacteristic.read().value.toString(Charsets.UTF_8)
    val hardwareRevision = hardwareRevisionStringCharacteristic.read().value.toString(Charsets.UTF_8)
    val deviceInformation = DeviceInformation(
      modelNumber = modelNumber, serialNumber = serialNumber,
      firmwareRevision = firmwareRevision, hardwareRevision = hardwareRevision
    )
    _state.value = _state.value.copy(deviceName = deviceName, deviceInformation = deviceInformation)

    // Launch notifications for dynamic data
    nexxtenderHomeChargingBasicDataCharacteristic.getNotifications().onEach {
      val newChargingBasicData = ChargingBasicDataParser.parse(it.value)!!
      //_state is a MutableStateFlow which propagates data to UI.
      _state.value = _state.value.copy(chargingBasicData = newChargingBasicData)
      Log.d(TAG, "Found the following notification of changed chargingBasicData: $newChargingBasicData")
    }.launchIn(viewModelScope)

    nexxtenderHomeChargingGridDataCharacteristic.getNotifications().onEach {
      val newChargingGridData = ChargingGridDataParser.parse(it.value)!!
      //_state is a MutableStateFlow which propagates data to UI.
      _state.value = _state.value.copy(chargingGridData = newChargingGridData)
      Log.d(TAG, "Found the following notification of changed chargingGridData: $newChargingGridData")
    }.launchIn(viewModelScope)

    nexxtenderHomeChargingCarDataCharacteristic.getNotifications().onEach {
      val newChargingCarData = ChargingCarDataParser.parse(it.value)!!
      //_state is a MutableStateFlow which propagates data to UI.
      _state.value = _state.value.copy(chargingCarData = newChargingCarData)
      Log.d(TAG, "Found the following notification of changed chargingCarData: $newChargingCarData")
    }.launchIn(viewModelScope)

    nexxtenderHomeChargingAdvancedDataCharacteristic.getNotifications().onEach {
      val newChargingAdvancedData = ChargingAdvancedDataParser.parse(it.value)!!
      //_state is a MutableStateFlow which propagates data to UI.
      _state.value = _state.value.copy(chargingAdvancedData = newChargingAdvancedData)
      Log.d(TAG, "Found the following notification of changed chargingAdvancedData: $newChargingAdvancedData")
    }.launchIn(viewModelScope)

    // Read Configuration Data
    nexxtenderHomeGenericStatusCharacteristic.getNotifications().onEach {
      Log.d(TAG, "Found Generic Status: $it")
      val status = it.value.fromUint16LE(0).toInt()
      Log.d(TAG, "Converted status: $status")
      when (status) {
        CONFIG_STATUS_POPPED -> {
          val configData = ConfigDataParserComposer.parse(
            nexxtenderHomeGenericDataCharacteristic.read().value,
            configVersion
          )!!
          _state.value = _state.value.copy(configData = configData)
        }

        CONFIG_STATUS_READY, CONFIG_STATUS_READY_CBOR -> {
          writeNewConfigData()
        }

        CONFIG_STATUS_SUCCESS, CONFIG_STATUS_SUCCESS_CBOR -> {
          // Read configuration to sync with changes
          sendConfigOperationGet()
        }

        else -> {
          Log.d(TAG, "Unknown GENERIC_STATUS value: $status")
        }
      }
    }.launchIn(viewModelScope)

    configVersion = getConfigVersion(firmwareRevision)
    sendConfigOperationGet()
  }

  private suspend fun writeNewConfigData() {
    writeGenericData(newConfigData)
  }

  @SuppressLint("MissingPermission")
  private suspend fun writeGenericData(newConfigData: ConfigData) {
    Log.d(TAG, "Writing Generic Data: $newConfigData")
    nexxtenderHomeGenericDataCharacteristic.write(DataByteArray(ConfigDataParserComposer.compose(newConfigData)))
  }

  private suspend fun sendConfigOperationGet() {
    val command =
      if (configVersion == ConfigVersion.CONFIG_CBOR) CONFIG_OPERATION_CBOR_GET else CONFIG_OPERATION_GET
    writeGenericCommand(command)
  }

  @SuppressLint("MissingPermission")
  private suspend fun writeGenericCommand(command: Int) {
    Log.d(TAG, "Writing Generic Command: $command")
    nexxtenderHomeGenericCommandCharacteristic.write(DataByteArray.fromUShort(command))
  }

  fun navigateBack(navigateBack: () -> Unit) {
    viewModelScope.launch {
      client?.disconnect()
      navigateBack()
    }
  }

  fun sendConfigOperationSetTouWeek(touPeriodWeek: TouPeriod) {
    viewModelScope.launch {
      newConfigData = _state.value.configData.copy(
        touWeekStart = touPeriodWeek.startTime.time,
        touWeekEnd = touPeriodWeek.endTime.time
      )
      sendConfigOperationSet()
    }
  }

  fun sendConfigOperationSetTouWeekend(touPeriodWeek: TouPeriod) {
    viewModelScope.launch {
      newConfigData = _state.value.configData.copy(
        touWeekendStart = touPeriodWeek.startTime.time,
        touWeekendEnd = touPeriodWeek.endTime.time
      )
      sendConfigOperationSet()
    }
  }

  fun sendConfigOperationSetMaxGrid(maxGrid: UByte) {
    viewModelScope.launch {
      newConfigData = _state.value.configData.copy(
        maxGrid = maxGrid
      )
      sendConfigOperationSet()
    }
  }

  fun sendConfigOperationSetMaxDevice(maxDevice: UByte) {
    viewModelScope.launch {
      newConfigData = _state.value.configData.copy(
        maxDevice = maxDevice
      )
      sendConfigOperationSet()
    }
  }

  fun sendConfigOperationSetMode(mode: Mode) {
    viewModelScope.launch {
      newConfigData = _state.value.configData.copy(
        mode = mode
      )
      sendConfigOperationSet()
    }
  }

  private suspend fun sendConfigOperationSet() {
    if (newConfigData.default) {
      Log.d(TAG, "New Config Data is still default: $newConfigData")
      return
    }

    val command =
      if (configVersion == ConfigVersion.CONFIG_CBOR) CONFIG_OPERATION_CBOR_SET else CONFIG_OPERATION_SET
    writeGenericCommand(command)
  }

  private fun getDevice(deviceId: Int) = runBlocking {
    Log.d(TAG, "Getting Device with id $deviceId")
    return@runBlocking devicesRepository.getDeviceStream(deviceId).first()
  }
}


/**
 * Represents DeviceInformation fields.
 */
data class DeviceInformation(
  val modelNumber: String = "",
  val serialNumber: String = "",
  val firmwareRevision: String = "",
  val hardwareRevision: String = ""
)

/**
 * Represents View State for a Device.
 */
data class DeviceDetailsViewState(
  val deviceName: String = "",
  val deviceInformation: DeviceInformation = DeviceInformation(),
  val chargingBasicData: ChargingBasicData = ChargingBasicData(),
  val chargingGridData: ChargingGridData = ChargingGridData(),
  val chargingCarData: ChargingCarData = ChargingCarData(),
  val chargingAdvancedData: ChargingAdvancedData = ChargingAdvancedData(),
  val configData: ConfigData = ConfigData()
)
