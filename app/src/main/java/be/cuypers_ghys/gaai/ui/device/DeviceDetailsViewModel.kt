/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2024-2025, Frank HJ Cuypers
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
import be.cuypers_ghys.gaai.data.ChargerType
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
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_STATUS_POPPED_CBOR
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_STATUS_READY
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_STATUS_READY_CBOR
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_STATUS_SUCCESS
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CONFIG_STATUS_SUCCESS_CBOR
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_AUTO
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_DEFAULT
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_ECO
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_MAX
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_STOP_CHARGING
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_STATUS_UNLOCKED
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_STATUS_UNLOCKED_FORCE_ECO
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_STATUS_UNLOCKED_FORCE_MAX
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_OPERATION_GET
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_OPERATION_SET
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_STATUS_POPPED
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_STATUS_READY
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_STATUS_SUCCESS
import be.cuypers_ghys.gaai.data.TimeData
import be.cuypers_ghys.gaai.data.TimeDataParserComposer
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

/**
 * Creates a 2-byte DataByteArray with the value of *command* as a 2-byte Little Endian.
 * @param command Value to write in the DataByteArray.
 *
 * @author Frank HJ Cuypers
 */
// TODO: use [ByteArrayHelpers.toUint16LE]
fun DataByteArray.Companion.fromUShort(command: Int): DataByteArray {
  return from((command and 0xFF).toByte(), ((command shr 8) and 0xFF).toByte())
}

/**
 * ViewModel to manage the state with the details of the [Device] with id [deviceId], to be used by [DeviceDetails].
 *
 * @param savedStateHandle [SavedStateHandle] passed by
 *  [AppViewModelProvider][be.cuypers_ghys.gaai.ui.AppViewModelProvider]
 * @param devicesRepository The [DevicesRepository] to use.
 * @param bleRepository The [BleRepository] to use.
 * @constructor Called by [AppViewModelProvider][be.cuypers_ghys.gaai.ui.AppViewModelProvider].
 *
 * @author Frank HJ Cuypers
 */
class DeviceDetailsViewModel(
  savedStateHandle: SavedStateHandle,
  private val devicesRepository: DevicesRepository,
  private val bleRepository: BleRepository
) : ViewModel() {

  /**
   * The id of the [Device] for which to build a state.
   */
  val deviceId: Int = checkNotNull(savedStateHandle[DeviceDetailsDestination.DEVICE_ID_ARG])

  /**
   * The [Device] corresponding with [deviceId].
   */
  private val gaaiDevice = getDevice(deviceId)!!

  private val _device = MutableStateFlow<Device?>(null)
  val device = _device.asStateFlow()

  private val _state = MutableStateFlow(DeviceDetailsViewState())
  val state = _state.asStateFlow()

  private var client: ClientBleGatt? = null

  init {
    Log.v(TAG, "ENTRY init()")
    _device.value = gaaiDevice
    startGattClient(gaaiDevice)
    Log.v(TAG, "RETURN init()")
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

  /** New configuration value to be written to GENERIC_DATA. */
  private lateinit var newConfigData: ConfigData

  /** New time value to be written to GENERIC_DATA. */
  private lateinit var newTimeData: TimeData

  /**
   * Starts a [ClientBleGatt] to communicate with the [gaaiDevice].
   * @param gaaiDevice [Device] for which to start a [ClientBleGatt].
   */
  @SuppressLint("MissingPermission")
  private fun startGattClient(gaaiDevice: Device) = viewModelScope.launch {
    Log.d(TAG, "ENTRY startGattClient(gaaiDevice: $gaaiDevice)")

    //Connect a Bluetooth LE device.
    updateDeviceState(DeviceState(ConnectionState.CONNECTING))
    val client = bleRepository.getClientBleGattConnection(gaaiDevice.mac, viewModelScope).also {
      this@DeviceDetailsViewModel.client = it
    }

    if (!client.isConnected) {
      Log.d(TAG, "Gatt Client not connected.")
      updateDeviceState(DeviceState(ConnectionState.NOT_CONNECTED))
      return@launch
    }

    Log.v(TAG, "Gatt Client connected. Discovering services.")
    updateDeviceState(DeviceState(ConnectionState.DISCOVERING))

    /*
     * Bluetooth caches the BLE GATT table.
     * That can be a problem when analyzing the protocol with WireShark,
     * because from the log, it can not map handles to UUIDs.
     * If mapping is required for debugging purposes, uncomment the next line.
     */
    // client.clearServicesCache()

    //Discover services on the Bluetooth LE Device.
    val services = client.discoverServices()
    configureGatt(gaaiDevice, services)
    Log.v(TAG, "RETURN startGattClient()")
  }

  /**
   * Sets up the GATT services and characteristics required for the Nexxtender charger.
   * @param gaaiDevice [Device] for which to configure GATT.
   * @param services Entry point for finding services and characteristics.
   */
  @SuppressLint("MissingPermission")
  private suspend fun configureGatt(gaaiDevice: Device, services: ClientBleGattServices) {
    Log.d(TAG, "ENTRY configureGatt($services)")

    // Remember needed service and characteristics which are used to communicate with the DK.
    val genericAccessService = services.findService(NexxtenderHomeSpecification.UUID_BLE_GENERIC_ACCESS_SERVICE)!!
    deviceNameCharacteristic =
      genericAccessService.findCharacteristic(NexxtenderHomeSpecification.UUID_BLE_DEVICE_NAME_CHARACTERISTIC)!!

    val deviceInformationService =
      services.findService(NexxtenderHomeSpecification.UUID_BLE_DEVICE_INFORMATION_SERVICE)!!
    modelNumberStringCharacteristic =
      deviceInformationService.findCharacteristic(
        NexxtenderHomeSpecification.UUID_BLE_MODEL_NUMBER_STRING_CHARACTERISTIC
      )!!
    serialNumberStringCharacteristic =
      deviceInformationService.findCharacteristic(
        NexxtenderHomeSpecification.UUID_BLE_SERIAL_NUMBER_STRING_CHARACTERISTIC
      )!!
    firmwareRevisionStringCharacteristic =
      deviceInformationService.findCharacteristic(
        NexxtenderHomeSpecification.UUID_BLE_FIRMWARE_REVISION_STRING_CHARACTERISTIC
      )!!
    hardwareRevisionStringCharacteristic =
      deviceInformationService.findCharacteristic(
        NexxtenderHomeSpecification.UUID_BLE_HARDWARE_REVISION_STRING_CHARACTERISTIC
      )!!

    if (gaaiDevice.type == ChargerType.MOBILE) {
      val nexxtenderChargingService =
        services.findService(NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CHARGING_SERVICE)!!
      nexxtenderHomeChargingBasicDataCharacteristic =
        nexxtenderChargingService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CHARGING_BASIC_DATA_CHARACTERISTIC
        )!!
    } else {
      val nexxtenderGenericService =
        services.findService(NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_GENERIC_CDR_SERVICE)!!
      nexxtenderHomeChargingBasicDataCharacteristic =
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CHARGING_BASIC_DATA_CHARACTERISTIC
        )!!
      nexxtenderHomeChargingGridDataCharacteristic =
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_GRID_DATA_CHARACTERISTIC
        )!!
      nexxtenderHomeChargingCarDataCharacteristic =
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_CAR_DATA_CHARACTERISTIC
        )!!
      nexxtenderHomeChargingAdvancedDataCharacteristic =
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_ADVANCED_DATA_CHARACTERISTIC
        )!!
      nexxtenderHomeGenericCommandCharacteristic =
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC
        )!!
      nexxtenderHomeGenericStatusCharacteristic =
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_STATUS_CHARACTERISTIC
        )!!
      nexxtenderHomeGenericDataCharacteristic =
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC
        )!!
    }
    updateDeviceState(DeviceState(ConnectionState.CONNECTED))

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

    if (gaaiDevice.type != ChargerType.MOBILE) {
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
          CONFIG_STATUS_POPPED, CONFIG_STATUS_POPPED_CBOR -> {
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

          TIME_STATUS_POPPED -> {
            val timeData = TimeDataParserComposer.parse(nexxtenderHomeGenericDataCharacteristic.read().value)!!
            _state.value = _state.value.copy(timeData = timeData)
          }

          TIME_STATUS_READY -> {
            writeNewTimeData()
          }

          LOADER_STATUS_UNLOCKED, LOADER_STATUS_UNLOCKED_FORCE_MAX, LOADER_STATUS_UNLOCKED_FORCE_ECO -> {
            // nop
          }

          // NOTE: Nexxtender Home seems to never send a TIME_STATUS_SUCCESS
          TIME_STATUS_SUCCESS -> {
            // Read configuration to sync with changes
            sendConfigOperationGet()
          }


          else -> {
            Log.d(TAG, "Unknown GENERIC_STATUS value: $status")
          }
        }
      }.launchIn(viewModelScope)
    }
    configVersion = getConfigVersion(firmwareRevision)
    sendConfigOperationGet()
    // Do not call sendTimeOperationGet() here! The sendConfigOperationGet() message sequence is still running
    // and would interfere with sendTimeOperationGet()
    Log.v(TAG, "RETURN configureGatt()")
  }

  /**
   * Writes [newConfigData] to the [Generic Data]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun writeNewConfigData() {
    Log.v(TAG, "ENTRY writeNewConfigData()")
    writeNewConfigDataToGenericData(newConfigData)
    Log.v(TAG, "RETURN writeNewConfigData()")
  }

  /**
   * Writes [newTimeData] to the [Generic Data]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun writeNewTimeData() {
    Log.v(TAG, "ENTRY writeNewTimeData()")
    val time = (System.currentTimeMillis() / 1000).toUInt()
    newTimeData = _state.value.timeData.copy(
      time = time
    )
    writeNewTimeDataToGenericData(newTimeData)
    Log.v(TAG, "RETURN writeNewTimeData()")
  }

  /**
   * Writes [newConfigData] to the [Generic Data]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
   * characteristic.
   * @param newConfigData The new [ConfigData] to write.
   */
  @SuppressLint("MissingPermission")
  private suspend fun writeNewConfigDataToGenericData(newConfigData: ConfigData) {
    Log.d(TAG, "ENTRY writeNewConfigDataToGenericData(newConfigData=$newConfigData)")
    writeNewDataToGenericData(DataByteArray(ConfigDataParserComposer.compose(newConfigData)))
    Log.v(TAG, "RETURN writeNewConfigDataToGenericData()")
  }

  /**
   * Writes [newTimeData] to the [Generic Data]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
   * characteristic.
   * @param newTimeData The new [TimeData] to write.
   */
  @SuppressLint("MissingPermission")
  private suspend fun writeNewTimeDataToGenericData(newTimeData: TimeData) {
    Log.d(TAG, "ENTRY writeNewTimeDataToGenericData(newTimeData=$newTimeData)")
    writeNewDataToGenericData(DataByteArray(TimeDataParserComposer.compose(newTimeData)))
    Log.v(TAG, "RETURN writeNewTimeDataToGenericData()")
  }

  /**
   * Writes [newData] to the [Generic Data]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
   * characteristic.
   * @param newData The new data to write.
   */
  @SuppressLint("MissingPermission")
  private suspend fun writeNewDataToGenericData(newData: DataByteArray) {
    Log.d(TAG, "ENTRY writeNewDataToGenericData(newData=$newData)")
    nexxtenderHomeGenericDataCharacteristic.write(newData)
    Log.v(TAG, "RETURN writeNewDataToGenericData()")
  }

  /**
   * Writes [CONFIG_OPERATION_GET] or [CONFIG_OPERATION_CBOR_GET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendConfigOperationGet() {
    Log.v(TAG, "ENTRY sendConfigOperationGet()")
    val command =
      if (configVersion == ConfigVersion.CONFIG_CBOR) CONFIG_OPERATION_CBOR_GET else CONFIG_OPERATION_GET
    writeGenericCommand(command)
    Log.v(TAG, "RETURN sendConfigOperationGet()")
  }

  /**
   * Writes [TIME_OPERATION_GET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendTimeOperationGet() {
    Log.v(TAG, "ENTRY sendTimeOperationGet()")
    val command = TIME_OPERATION_GET
    writeGenericCommand(command)
    Log.v(TAG, "RETURN sendTimeOperationGet()")
  }

  /**
   * Writes [TIME_OPERATION_SET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendTimeOperationSet() {
    Log.v(TAG, "ENTRY sendTimeOperationSet()")
    val command = TIME_OPERATION_SET
    writeGenericCommand(command)
    Log.v(TAG, "RETURN sendTimeOperationSet()")
  }

  /**
   * Writes [command] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   * @param command The [OperationId][be.cuypers_ghys.gaai.data.OperationAndStatusIDs] to write.
   */
  @SuppressLint("MissingPermission")
  private suspend fun writeGenericCommand(command: Int) {
    Log.d(TAG, "ENTRY writeGenericCommand(command=$command)")
    nexxtenderHomeGenericCommandCharacteristic.write(DataByteArray.fromUShort(command))
    Log.v(TAG, "RETURN writeGenericCommand()")
  }

  /**
   * Disconnects the [client] and executes [navigateUp].
   * @param navigateUp Function called when this view model wants to navigate up to the previous view.
   */
  fun navigateUp(navigateUp: () -> Unit) {
    viewModelScope.launch {
      Log.v(TAG, "ENTRY navigateUp()")
      client?.disconnect()
      navigateUp()
      Log.v(TAG, "RETURN navigateUp()")
    }
  }

  /**
   * Writes [CONFIG_OPERATION_SET] or [CONFIG_OPERATION_CBOR_SET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic, changing the [touPeriodWeek].
   * @param touPeriodWeek New values for [ConfigData.touWeekStart] and [ConfigData.touWeekEnd].
   */
  fun sendConfigOperationSetTouWeek(touPeriodWeek: TouPeriod) {
    viewModelScope.launch {
      Log.d(TAG, "ENTRY sendConfigOperationSetTouWeek(touPeriodWeek=$touPeriodWeek)")
      newConfigData = _state.value.configData.copy(
        touWeekStart = touPeriodWeek.startTime.time,
        touWeekEnd = touPeriodWeek.endTime.time
      )
      sendConfigOperationSet()
      Log.v(TAG, "RETURN sendConfigOperationSetTouWeek()")
    }
  }

  /**
   * Writes [CONFIG_OPERATION_SET] or [CONFIG_OPERATION_CBOR_SET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic, changing the [touPeriodWeekend].
   * @param touPeriodWeekend New values for [ConfigData.touWeekendStart] and [ConfigData.touWeekendEnd].
   */
  fun sendConfigOperationSetTouWeekend(touPeriodWeekend: TouPeriod) {
    viewModelScope.launch {
      Log.d(TAG, "ENTRY sendConfigOperationSetTouWeekend(touPeriodWeekend=$touPeriodWeekend)")
      newConfigData = _state.value.configData.copy(
        touWeekendStart = touPeriodWeekend.startTime.time,
        touWeekendEnd = touPeriodWeekend.endTime.time
      )
      sendConfigOperationSet()
      Log.v(TAG, "RETURN sendConfigOperationSetTouWeekend()")
    }
  }

  /**
   * Writes [CONFIG_OPERATION_SET] or [CONFIG_OPERATION_CBOR_SET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic, changing the [ConfigData.maxGrid].
   * @param maxGrid New values for [ConfigData.maxGrid].
   */
  fun sendConfigOperationSetMaxGrid(maxGrid: UByte) {
    viewModelScope.launch {
      Log.d(TAG, "ENTRY sendConfigOperationSetMaxGrid(maxGrid=$maxGrid)")
      newConfigData = _state.value.configData.copy(
        maxGrid = maxGrid
      )
      sendConfigOperationSet()
      Log.v(TAG, "RETURN sendConfigOperationSetMaxGrid()")
    }
  }

  /**
   * Writes [CONFIG_OPERATION_SET] or [CONFIG_OPERATION_CBOR_SET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic, changing the [ConfigData.maxDevice].
   * @param maxDevice New values for [ConfigData.maxDevice].
   */
  fun sendConfigOperationSetMaxDevice(maxDevice: UByte) {
    viewModelScope.launch {
      Log.d(TAG, "ENTRY sendConfigOperationSetMaxDevice(maxDevice=$maxDevice)")
      newConfigData = _state.value.configData.copy(
        maxDevice = maxDevice
      )
      sendConfigOperationSet()
      Log.v(TAG, "RETURN sendConfigOperationSetMaxDevice()")
    }
  }

  /**
   * Writes [CONFIG_OPERATION_SET] or [CONFIG_OPERATION_CBOR_SET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic, changing the [ConfigData.safe].
   * @param safe New values for [ConfigData.safe].
   */
  fun sendConfigOperationSetSafe(safe: UByte) {
    viewModelScope.launch {
      Log.d(TAG, "ENTRY sendConfigOperationSetSafe(safe=$safe)")
      newConfigData = _state.value.configData.copy(
        safe = safe
      )
      sendConfigOperationSet()
      Log.v(TAG, "RETURN sendConfigOperationSetSafe()")
    }
  }

  /**
   * Writes [CONFIG_OPERATION_SET] or [CONFIG_OPERATION_CBOR_SET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic, changing the [ConfigData.mode].
   * @param mode New values for [ConfigData.mode].
   */
  fun sendConfigOperationSetMode(mode: Mode) {
    viewModelScope.launch {
      Log.d(TAG, "ENTRY sendConfigOperationSetMode(mode=$mode)")
      newConfigData = _state.value.configData.copy(
        mode = mode
      )
      sendConfigOperationSet()
      Log.v(TAG, "RETURN sendConfigOperationSetMode()")
    }
  }

  /**
   * Writes [CONFIG_OPERATION_SET] or [CONFIG_OPERATION_CBOR_SET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic, changing the [ConfigData.iCapacity].
   * @param iCapacity New values for [ConfigData.iCapacity].
   */
  fun sendConfigOperationSetICapacity(iCapacity: UByte) {
    viewModelScope.launch {
      Log.d(TAG, "ENTRY sendConfigOperationSetICapacity(iCapacity=$iCapacity)")
      newConfigData = _state.value.configData.copy(
        iCapacity = iCapacity
      )
      sendConfigOperationSet()
      Log.v(TAG, "RETURN sendConfigOperationSetICapacity()")
    }
  }

  /**
   * Writes [TIME_OPERATION_SET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic, changing the time to the current time on the mobile phone.
   */
  fun sendTimeOperationSyncTime() {
    viewModelScope.launch {
      Log.v(TAG, "ENTRY sendTimeOperationSyncTime()")
      sendTimeOperationSet()
      Log.v(TAG, "RETURN sendTimeOperationSyncTime()")
    }
  }

  /**
   * Writes [TIME_OPERATION_GET]  to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic].
   */
  fun sendTimeOperationGetTime() {
    viewModelScope.launch {
      Log.v(TAG, "ENTRY sendTimeOperationGetTime()")
      sendTimeOperationGet()
      Log.v(TAG, "RETURN sendTimeOperationGetTime()")
    }
  }

  /**
   * Writes [CONFIG_OPERATION_SET] or [CONFIG_OPERATION_CBOR_SET] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendConfigOperationSet() {
    Log.v(TAG, "ENTRY sendConfigOperationSet()")
    if (newConfigData.default) {
      Log.d(TAG, "New Config Data is still default: $newConfigData")
      return
    }

    val command =
      if (configVersion == ConfigVersion.CONFIG_CBOR) CONFIG_OPERATION_CBOR_SET else CONFIG_OPERATION_SET
    writeGenericCommand(command)
    Log.v(TAG, "RETURN sendConfigOperationSet()")
  }

  /**
   * Loads the [Device] corresponding with [deviceId] from the database.
   * @param deviceId The id of the [device] to load.
   */
  private fun getDevice(deviceId: Int) = runBlocking {
    Log.d(TAG, "ENTRY getDevice(deviceId=$deviceId)")
    return@runBlocking devicesRepository.getDeviceStream(deviceId).first()
  }

  /**
   * Writes [loaderOperation] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   * @param loaderOperation Any of [LOADER_OPERATION_START_CHARGING_DEFAULT], [LOADER_OPERATION_START_CHARGING_MAX],
   *  [LOADER_OPERATION_START_CHARGING_AUTO], [LOADER_OPERATION_START_CHARGING_ECO],
   *  [LOADER_OPERATION_STOP_CHARGING]
   */
  fun sendLoaderOperation(loaderOperation: Int) {
    viewModelScope.launch {
      Log.d(TAG, "ENTRY sendLoaderOperation(loaderOperation=$loaderOperation)")

      when (loaderOperation) {
        LOADER_OPERATION_START_CHARGING_DEFAULT, LOADER_OPERATION_START_CHARGING_MAX,
        LOADER_OPERATION_START_CHARGING_AUTO, LOADER_OPERATION_START_CHARGING_ECO, LOADER_OPERATION_STOP_CHARGING ->
          writeGenericCommand(loaderOperation)

        else -> {
          Log.d(TAG, "sendLoaderOperation() trying to send incorrect loaderOperation: $loaderOperation")
        }
      }
      Log.v(TAG, "RETURN sendLoaderOperation()")
    }
  }

  /**
   * Updates the [deviceState] with the value provided in the argument.
   * @param deviceState The DeviceState.
   */
  private fun updateDeviceState(deviceState: DeviceState) {
    Log.d(TAG, "ENTRY updateDeviceState(deviceState=$deviceState)")
    _state.value = _state.value.copy(deviceState = deviceState)
    Log.v(TAG, "RETURN updateDeviceState()")
  }
}

/**
 * Represents Device Information fields.
 */
data class DeviceInformation(
  val modelNumber: String = "",
  val serialNumber: String = "",
  val firmwareRevision: String = "",
  val hardwareRevision: String = ""
)

enum class ConnectionState {
  UNKNOWN, CONNECTING, DISCOVERING, CONNECTED, NOT_CONNECTED
}

/**
 * Represents Device State.
 */
data class DeviceState(
  val connectionState: ConnectionState = ConnectionState.UNKNOWN
)


/**
 * Represents View State for a Device.
 */
data class DeviceDetailsViewState(
  val deviceName: String = "",
  val deviceState: DeviceState = DeviceState(),
  val deviceInformation: DeviceInformation = DeviceInformation(),
  val chargingBasicData: ChargingBasicData = ChargingBasicData(),
  val chargingGridData: ChargingGridData = ChargingGridData(),
  val chargingCarData: ChargingCarData = ChargingCarData(),
  val chargingAdvancedData: ChargingAdvancedData = ChargingAdvancedData(),
  val configData: ConfigData = ConfigData(),
  /**
   * Time in [Unix Time](https://en.wikipedia.org/wiki/Unix_time) as reported by the TIME SET and TIME GET
   * operations.
   */
  val timeData: TimeData = TimeData()
)
