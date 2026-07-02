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

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.documentfile.provider.DocumentFile
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
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CCDT_COMMAND_NEXT
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.CDR_COMMAND_NEXT
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
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.EVENT
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.EVENT_OPERATION_NEXT
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.EVENT_OPERATION_UPDATE_STATUS
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_AUTO
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_DEFAULT
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_ECO
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_START_CHARGING_MAX
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_OPERATION_STOP_CHARGING
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_STATUS_UNLOCKED
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_STATUS_UNLOCKED_FORCE_ECO
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.LOADER_STATUS_UNLOCKED_FORCE_MAX
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.METRIC
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.METRIC_OPERATION_NEXT
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.METRIC_OPERATION_UPDATE_STATUS
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_OPERATION_GET
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_OPERATION_SET
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_STATUS_POPPED
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_STATUS_READY
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.TIME_STATUS_SUCCESS
import be.cuypers_ghys.gaai.data.TimeData
import be.cuypers_ghys.gaai.data.TimeDataParserComposer
import be.cuypers_ghys.gaai.util.Timestamp
import be.cuypers_ghys.gaai.util.TouPeriod
import be.cuypers_ghys.gaai.util.fromInt32LE
import be.cuypers_ghys.gaai.util.fromUint16LE
import be.cuypers_ghys.gaai.util.fromUint32LE
import be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification
import io.github.g00fy2.versioncompare.Version
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattService
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BondState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionStateWithStatus
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import java.io.PrintWriter
import java.util.LinkedList
import java.util.Queue
import kotlin.coroutines.cancellation.CancellationException

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

  private var onlyGetMumberofRecords: Boolean = true

  private val VERSION_1_3_8 = Version("1.3.8")

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
  private lateinit var manufacturerNameStringCharacteristic: ClientBleGattCharacteristic
  private lateinit var dateUtcCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeChargingBasicDataCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeChargingGridDataCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeChargingCarDataCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeChargingAdvancedDataCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderChargingService: ClientBleGattService
  private lateinit var nexxtenderHomeGenericCommandCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeGenericStatusCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeGenericDataCharacteristic: ClientBleGattCharacteristic

  private lateinit var nexxtenderCDRService: ClientBleGattService
  private lateinit var nexxtenderHomeCDRCommandCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeCDRStatusCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeCDRRecordCharacteristic: ClientBleGattCharacteristic

  private lateinit var nexxtenderCCDTService: ClientBleGattService
  private lateinit var nexxtenderHomeCCDTCommandCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeCCDTStatusCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeCCDTRecordCharacteristic: ClientBleGattCharacteristic


  private lateinit var configVersion: ConfigVersion

  /** New configuration value to be written to GENERIC_DATA. */
  private lateinit var newConfigData: ConfigData

  /** New time value to be written to GENERIC_DATA. */
  private lateinit var newTimeData: TimeData

  /** PrintWriter for writing records.*/
  private lateinit var printWriter: PrintWriter

  /** Remaining CDR Records to read */
  private var remainingCDRRecords: Int = 0

  /** Remaining CCDT Records to read */
  private var remainingCCDTRecords: Int = 0

  /** Remaining Event Records to read */
  private var remainingEventRecords: Int = 0

  /** Remaining Metric Records to read */
  private var remainingMetricRecords: Int = 0

  /**
   * URI of the directory selected by the user for writing log records (CDR, CCDT, Event, Metrics)
   */
  private var logRecordOutDirectoryUri : Uri? = null

  private var cdrFileName : String = ""
  private var ccdtFileName : String = ""
  private var eventFileName : String = ""
  private var metricFileName : String = ""

  /**
   * The Generic Command that is currently handled. -1 means none.
   */
  private var currentGenericCommand : Int = -1 ;

  /**
   * Queue of Generic Command to process.
   * Generic commands require the exchange of multiple BLE messages.
   * A new Generic Command should not start as long as the previous one is not completed.
   */
  private var genericCommandQueue: Queue<Int> = LinkedList();

  /**
   * Starts a [ClientBleGatt] to communicate with the [gaaiDevice].
   * @param gaaiDevice [Device] for which to start a [ClientBleGatt].
   */
  @SuppressLint("MissingPermission")
  private fun startGattClient(gaaiDevice: Device) = viewModelScope.launch {
    Log.d(TAG, "ENTRY startGattClient(gaaiDevice: $gaaiDevice)")

    //Connect a Bluetooth LE device.
    val client = bleRepository.getClientBleGattConnection(gaaiDevice.mac, viewModelScope).also {
      this@DeviceDetailsViewModel.client = it
    }

    client.connectionStateWithStatus
      .filterNotNull()
      .onEach { updateGattConnectionStateWithStatus(it) }
      .launchIn(viewModelScope)

    updateBondState(GaaiBondState.getBondState(bleRepository.context, gaaiDevice))

    client.bondState
      .filterNotNull()
      .onEach {
        Log.v(TAG, "bondState: $it")
        updateBondState(it)
      }
      .launchIn(viewModelScope)

    if (!client.isConnected) {
      Log.d(TAG, "Gatt Client not connected.")
      return@launch
    }

    Log.v(TAG, "Gatt Client connected. Discovering services.")

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

    if (gaaiDevice.type != ChargerType.HOME) {
      manufacturerNameStringCharacteristic =
        deviceInformationService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_BLE_MANUFACTURER_NAME_STRING_CHARACTERISTIC
        )!!

      nexxtenderChargingService =
        services.findService(NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CHARGING_SERVICE)!!

      nexxtenderHomeChargingBasicDataCharacteristic =
        nexxtenderChargingService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CHARGING_BASIC_DATA_CHARACTERISTIC
        )!!

      nexxtenderCDRService =
        services.findService(NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_GENERIC_CDR_SERVICE)!!
      nexxtenderHomeCDRCommandCharacteristic =
        nexxtenderCDRService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CDR_COMMAND_CHARACTERISTIC
        )!!
      nexxtenderHomeCDRStatusCharacteristic =
        nexxtenderCDRService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CDR_STATUS_CHARACTERISTIC
        )!!
      nexxtenderHomeCDRRecordCharacteristic=
        nexxtenderCDRService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CDR_RECORD_CHARACTERISTIC
        )!!

      nexxtenderCCDTService =
        services.findService(NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CCDT_SERVICE)!!
      nexxtenderHomeCCDTCommandCharacteristic =
        nexxtenderCCDTService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CCDT_COMMAND_CHARACTERISTIC
        )!!
      nexxtenderHomeCCDTStatusCharacteristic =
        nexxtenderCCDTService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CCDT_STATUS_CHARACTERISTIC
        )!!
      nexxtenderHomeCCDTRecordCharacteristic=
        nexxtenderCCDTService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CCDT_RECORD_CHARACTERISTIC
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

      nexxtenderCDRService =
        services.findService(NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_GENERIC_CDR_SERVICE)!!
      nexxtenderHomeCDRCommandCharacteristic =
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CDR_COMMAND_CHARACTERISTIC
        )!!
      nexxtenderHomeCDRStatusCharacteristic =
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CDR_STATUS_CHARACTERISTIC
        )!!
      nexxtenderHomeCDRRecordCharacteristic=
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CDR_RECORD_CHARACTERISTIC
        )!!

      nexxtenderCCDTService =
        services.findService(NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_GENERIC_CDR_SERVICE)!!
      nexxtenderHomeCCDTCommandCharacteristic =
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CCDT_COMMAND_CHARACTERISTIC
        )!!
      nexxtenderHomeCCDTStatusCharacteristic =
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CCDT_STATUS_CHARACTERISTIC
        )!!
      nexxtenderHomeCCDTRecordCharacteristic=
        nexxtenderGenericService.findCharacteristic(
          NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_CCDT_RECORD_CHARACTERISTIC
        )!!
    }

    // Read static information
    val deviceName = deviceNameCharacteristic.read().value.toString(Charsets.UTF_8)
    val modelNumber = modelNumberStringCharacteristic.read().value.toString(Charsets.UTF_8)
    val serialNumber = serialNumberStringCharacteristic.read().value.toString(Charsets.UTF_8)
    val firmwareRevision = firmwareRevisionStringCharacteristic.read().value.toString(Charsets.UTF_8)
    val hardwareRevision = hardwareRevisionStringCharacteristic.read().value.toString(Charsets.UTF_8)
    val manufacturerName = if (gaaiDevice.type != ChargerType.HOME) {
      manufacturerNameStringCharacteristic.read().value.toString(Charsets.UTF_8)
    } else {
      "?"
    }
    val deviceInformation = DeviceInformation(
      modelNumber = modelNumber, serialNumber = serialNumber,
      firmwareRevision = firmwareRevision, hardwareRevision = hardwareRevision, manufacturerName=manufacturerName
    )
    _state.value = _state.value.copy( deviceInformation = deviceInformation)

    nexxtenderHomeChargingBasicDataCharacteristic.getNotifications().onEach {
      // Log.i(TAG, "Found the following notification of changed chargingBasicData DataByteArray: $it")
      val newChargingBasicData = ChargingBasicDataParser.parse(it.value)!!
      //_state is a MutableStateFlow which propagates data to UI.
      _state.value = _state.value.copy(chargingBasicData = newChargingBasicData)
      Log.d(TAG, "Found the following notification of changed chargingBasicData: $newChargingBasicData")
    }.launchIn(viewModelScope)

    if (gaaiDevice.type != ChargerType.HOME) {
      val version = Version(firmwareRevision)
      val supportsDateUtc = version >= VERSION_1_3_8
      _state.value = _state.value.copy(supportsDateUtc = supportsDateUtc)

      if (supportsDateUtc) {
        dateUtcCharacteristic =
          nexxtenderChargingService.findCharacteristic(
            NexxtenderHomeSpecification.UUID_BLE_DATE_UTC_CHARACTERISTIC
          )!!
      }
    }

    configVersion = getConfigVersion(firmwareRevision)

    if (gaaiDevice.type == ChargerType.HOME) {
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
            startNextQueuedGenericCommand()
          }

          CONFIG_STATUS_READY, CONFIG_STATUS_READY_CBOR -> {
            writeNewConfigData()
          }

          CONFIG_STATUS_SUCCESS, CONFIG_STATUS_SUCCESS_CBOR -> {
            // Read configuration to sync with changes
            sendConfigOperationGet()
            startNextQueuedGenericCommand()
          }

          TIME_STATUS_POPPED -> {
            val timeData = TimeDataParserComposer.parse(nexxtenderHomeGenericDataCharacteristic.read().value)!!
            _state.value = _state.value.copy(timeData = timeData)
            startNextQueuedGenericCommand()
          }

          TIME_STATUS_READY -> {
            writeNewTimeData()
            startNextQueuedGenericCommand()
          }

          LOADER_STATUS_UNLOCKED, LOADER_STATUS_UNLOCKED_FORCE_MAX, LOADER_STATUS_UNLOCKED_FORCE_ECO -> {
            startNextQueuedGenericCommand()
          }

          // NOTE: Nexxtender Home seems to never send a TIME_STATUS_SUCCESS
          TIME_STATUS_SUCCESS -> {
            // Read time to sync with changes
            sendTimeOperationGet()
          }
          else -> {
            if ( (status and 0xF000) == EVENT) {
              remainingEventRecords = status and 0x0FFF
              updateEventState(remainingEventRecords)
              if ( onlyGetMumberofRecords ) {
                startNextQueuedGenericCommand()
              } else {
                if (remainingEventRecords > 0) {
                  readEventRecordAndAskNext()
                } else {
                  printWriter.close()
                  currentGenericCommand = - 1
                  startmetricSync()
                }
              }
            } else if ( (status and 0xF000) == METRIC) {
              remainingMetricRecords = status and 0x0FFF
              updateMetricState(remainingMetricRecords)

              if ( onlyGetMumberofRecords ) {
                startNextQueuedGenericCommand()
              } else {
                if ( remainingMetricRecords > 0) {
                  readMetricRecordAndAskNext()
                } else {
                  printWriter.close()
                  startNextQueuedGenericCommand()
                }
              }
            } else {
              startNextQueuedGenericCommand()
              Log.d(TAG, "Unknown GENERIC_STATUS value: $status")
            }
          }
        }
      }.launchIn(viewModelScope)

      readRemainingCDRRecordsAndUpdateState()
      readRemainingCCDTRecordsAndUpdateState()
      readRemainingEventRecordsAndUpdateState()
      readRemainingMetricRecordsAndUpdateState()
      sendTimeOperationGet()
      sendConfigOperationGet()
    }
    Log.v(TAG, "RETURN configureGatt()")
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  private suspend fun readRemainingCDRRecordsAndUpdateState() {
    remainingCDRRecords = nexxtenderHomeCDRStatusCharacteristic.read().value.fromInt32LE(0)
    updateCDRState(remainingCDRRecords)
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  private suspend fun readRemainingCCDTRecordsAndUpdateState() {
    remainingCCDTRecords = nexxtenderHomeCCDTStatusCharacteristic.read().value.fromInt32LE(0)
    updateCCDTState(remainingCCDTRecords)
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  private suspend fun readRemainingEventRecordsAndUpdateState() {
    onlyGetMumberofRecords = true
    sendEventOperationUpdateStatus()
  }
  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  private suspend fun readRemainingMetricRecordsAndUpdateState() {
    onlyGetMumberofRecords = true
    sendMetricOperationUpdateStatus()
  }

  private fun updateCDRState(remainingCDRRecords: Int) {
    Log.d(TAG, "ENTRY updateCDRState(remainingCdrRecords=$remainingCDRRecords)")
    val dataRecordsInformation = _state.value.dataRecordsInformation.copy(remainingCDRRecords=remainingCDRRecords)
    _state.value = _state.value.copy(dataRecordsInformation = dataRecordsInformation)
    Log.v(TAG, "RETURN updateCDRState()")
  }

  private fun updateCCDTState(remainingCCDTRecords: Int) {
    Log.d(TAG, "ENTRY updateCCDTState(remainingCCDTRecords=$remainingCCDTRecords)")
    val dataRecordsInformation = _state.value.dataRecordsInformation.copy(remainingCCDTRecords=remainingCCDTRecords)
    _state.value = _state.value.copy(dataRecordsInformation = dataRecordsInformation)
    Log.v(TAG, "RETURN updateCCDTState()")
  }

  private fun updateEventState(remainingEventRecords: Int) {
    Log.d(TAG, "ENTRY updateEventState(remainingEventRecords=$remainingEventRecords)")
    val dataRecordsInformation = _state.value.dataRecordsInformation.copy(remainingEventRecords=remainingEventRecords)
    _state.value = _state.value.copy(dataRecordsInformation = dataRecordsInformation)
    Log.v(TAG, "RETURN updateEventState()")
  }

  private fun updateMetricState(remainingMetricRecords: Int) {
    Log.d(TAG, "ENTRY updateMetricState(remainingMetricRecords=$remainingMetricRecords)")
    val dataRecordsInformation = _state.value.dataRecordsInformation.copy(remainingMetricRecords=remainingMetricRecords)
    _state.value = _state.value.copy(dataRecordsInformation = dataRecordsInformation)
    Log.v(TAG, "RETURN updateMetricState()")
  }

  /**
   * Writes [newConfigData] to the [Generic Data]
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun writeNewConfigData() {
    Log.v(TAG, "ENTRY writeNewConfigData()")
    writeNewConfigDataToGenericData(newConfigData)
    Log.v(TAG, "RETURN writeNewConfigData()")
  }

  /**
   * Writes [newTimeData] to the [Generic Data]
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC]
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendConfigOperationGet() {
    Log.v(TAG, "ENTRY sendConfigOperationGet()")
    val command =
      if (configVersion == ConfigVersion.CONFIG_CBOR) CONFIG_OPERATION_CBOR_GET else CONFIG_OPERATION_GET
    queueGenericCommand(command)
    Log.v(TAG, "RETURN sendConfigOperationGet()")
  }

  /**
   * Writes [TIME_OPERATION_GET] to the [Generic Command]
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendTimeOperationGet() {
    Log.v(TAG, "ENTRY sendTimeOperationGet()")
    val command = TIME_OPERATION_GET
    queueGenericCommand(command)
    Log.v(TAG, "RETURN sendTimeOperationGet()")
  }

  /**
   * Writes [TIME_OPERATION_SET] to the [Generic Command]
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendTimeOperationSet() {
    Log.v(TAG, "ENTRY sendTimeOperationSet()")
    val command = TIME_OPERATION_SET
    queueGenericCommand(command)
    Log.v(TAG, "RETURN sendTimeOperationSet()")
  }

  private suspend fun sendEventOperationUpdateStatus() {
    Log.v(TAG, "ENTRY sendEventOperationUpdateStatus()")
    queueGenericCommand(EVENT_OPERATION_UPDATE_STATUS)
    Log.v(TAG, "RETURN sendEventOperationUpdateStatus()")
  }

  private suspend fun sendMetricOperationUpdateStatus() {
    Log.v(TAG, "ENTRY sendMetricOperationUpdateStatus()")
    queueGenericCommand(METRIC_OPERATION_UPDATE_STATUS)
    Log.v(TAG, "RETURN sendMetricOperationUpdateStatus()")
  }

  private suspend fun sendEventOperationNext() {
    Log.v(TAG, "ENTRY sendEventOperationNext()")
    writeGenericCommand(EVENT_OPERATION_NEXT)
    Log.v(TAG, "RETURN sendEventOperationNext()")
  }

  private suspend fun sendMetricOperationNext() {
    Log.v(TAG, "ENTRY sendMetricOperationNext()")
    writeGenericCommand(METRIC_OPERATION_NEXT)
    Log.v(TAG, "RETURN sendMetricOperationNext()")
  }

  /**
   * Writes [command] to the [Generic Command]
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
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

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  fun syncTime() {
    Log.v(TAG, "ENTRY syncTime()")
    if (gaaiDevice.type == ChargerType.HOME) {
      sendTimeOperationSyncTime()
    }
    else if (_state.value.supportsDateUtc) {
      writeDateUTC()
    }
    Log.v(TAG, "RETURN syncTime()")
  }
  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  fun writeDateUTC() {
    viewModelScope.launch {
      Log.v(TAG, "ENTRY writeDateUTC()")
      val time = (System.currentTimeMillis() / 1000).toUInt()
      newTimeData = _state.value.timeData.copy(
        time = time
      )
      dateUtcCharacteristic.write(DataByteArray(TimeDataParserComposer.compose(newTimeData)))
     // _state.value = _state.value.copy(timeData = newTimeData)
      Log.v(TAG, "RETURN writeDateUTC() " +Timestamp.toString(time) )
    }
  }

  /**
   * Writes [TIME_OPERATION_SET] to the [Generic Command]
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic, changing the time to the current time on the mobile phone.
   */
  fun sendTimeOperationSyncTime() {
    viewModelScope.launch {
      Log.v(TAG, "ENTRY sendTimeOperationSyncTime()")
      sendTimeOperationSet()
      Log.v(TAG, "RETURN sendTimeOperationSyncTime()")
    }
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  fun getTime() {
    Log.v(TAG, "ENTRY getTime()")
    if (gaaiDevice.type == ChargerType.HOME) {
      sendTimeOperationGetTime()
    }
    else if (_state.value.supportsDateUtc) {
      readDateUTC()
    }
    Log.v(TAG, "RETURN getTime()")
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  fun readDateUTC() {
    viewModelScope.launch {
      Log.v(TAG, "ENTRY readDateUTC()")
      val timeData = TimeDataParserComposer.parse(dateUtcCharacteristic.read().value)!!
      _state.value = _state.value.copy(timeData = timeData)
      Log.v(TAG, "RETURN readDateUTC() " + Timestamp.toString(timeData.time))
    }
  }

  /**
   * Writes [TIME_OPERATION_GET]  to the [Generic Command]
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
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
    queueGenericCommand(command)
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
   * [NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
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
          queueGenericCommand(loaderOperation)

        else -> {
          Log.d(TAG, "sendLoaderOperation() trying to send incorrect loaderOperation: $loaderOperation")
        }
      }
      Log.v(TAG, "RETURN sendLoaderOperation()")
    }
  }

  /**
   * Updates the [gattConnectionStateWithStatus] with the value provided in the argument.
   * @param gattConnectionStateWithStatus The GattConnectionStateWithStatus.
   */
  private fun updateGattConnectionStateWithStatus(gattConnectionStateWithStatus: GattConnectionStateWithStatus) {
    Log.d(
      TAG,
      "ENTRY updateGattConnectionStateWithStatus(gattConnectionStateWithStatus=$gattConnectionStateWithStatus)"
    )
    _state.value = _state.value.copy(gattConnectionStateWithStatus = gattConnectionStateWithStatus)
    Log.v(TAG, "RETURN updateGattConnectionStateWithStatus()")
  }

  /**
   * Updates the [bondState] with the value provided in the argument.
   * @param bondState The BondState.
   */
  private fun updateBondState(bondState: BondState) {
    Log.d(TAG, "ENTRY updateBondState(bondState=$bondState)")
    _state.value = _state.value.copy(bondState = bondState)
    Log.v(TAG, "RETURN updateBondState()")
  }

  /**
   * Starts syncing the records.
   * characteristic.
   */
  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  fun startRecordsSync(outputDirectoryUri: Uri) {
    viewModelScope.launch {
      onlyGetMumberofRecords = false
      // Create outputfile names
      val time = (System.currentTimeMillis() / 1000).toUInt()
      val timeString = Timestamp.toString(time).replace("[^\\w]".toRegex(),"")
      logRecordOutDirectoryUri = outputDirectoryUri
      cdrFileName = gaaiDevice.sn + "_CDR_" + timeString
      ccdtFileName = gaaiDevice.sn + "_CCDT_" + timeString
      eventFileName = gaaiDevice.sn + "_EVENT_" + timeString
      metricFileName = gaaiDevice.sn + "_METRIC_" + timeString

      startCDRSync()
    }
  }

    fun createRecordsOutputWriter(fileName: String): PrintWriter {
      // Create outputfile
      Log.i(TAG, "createRecordsOutputWriter() fileName = $fileName")
      val context = bleRepository.context
      val logRecOutDir = DocumentFile.fromTreeUri(context, logRecordOutDirectoryUri!!)
      val file = logRecOutDir?.createFile("text/plain", fileName)
      val contentResolver = context.contentResolver
      val outputStream = contentResolver.openOutputStream(file!!.uri, "w")
      printWriter = PrintWriter(outputStream)
      return printWriter
  }

  /**
   * Starts syncing the [CDR].
   * Once complete, it calls [startCCDTSync()]
   * characteristic.
   */
  @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
  fun startCDRSync() {
    viewModelScope.launch {
      Log.v(TAG, "ENTRY startCDRSync()")
      printWriter = createRecordsOutputWriter(cdrFileName)

      // Start reading CDR record
      if (remainingCDRRecords > 0 ) {
        nexxtenderHomeCDRStatusCharacteristic.getNotifications().onEach() {
          remainingCDRRecords = it.value.fromUint32LE(0).toInt()
          Log.d(TAG, "Found the following notification of changed remainingCdrRecords: $remainingCDRRecords")
          updateCDRState(remainingCDRRecords)
          if (remainingCDRRecords > 0 ) {
            // Read one more
            readCDRRecordAndAskNext()
          } else {
            // We have read all CDR records
            printWriter.close()
            // stop notification
            val cancellationException = CancellationException()
            throw cancellationException
          }
        }.onCompletion{startCCDTSync()}.launchIn(viewModelScope)
        readCDRRecordAndAskNext()  // read first record
      } else {
        // There were no CDR records
        printWriter.close()
        startCCDTSync()
      }
      Log.v(TAG, "RETURN startCDRSync()")
    }
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  private suspend fun readCDRRecordAndAskNext() {
    val record = nexxtenderHomeCDRRecordCharacteristic.read().value
    printRecord(record, 4)
    nexxtenderHomeCDRCommandCharacteristic.write(DataByteArray.from(CDR_COMMAND_NEXT))
  }

  private suspend fun printRecord(record: ByteArray, timeOffset:Int) {
    val timestamp = record.fromUint32LE(timeOffset)
    printWriter.print("< ")
    printWriter.print(Timestamp.toString(timestamp))
    printWriter.print(" ")
    printWriter.print(record.toHexString())
    printWriter.println()
    printWriter.flush()
  }

  /**
   * Starts syncing the [CCDT] records.
   * Once complete, it calls [startEventSync()]
   * characteristic.
   */
  @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
  private suspend fun startCCDTSync() {
    viewModelScope.launch {
      Log.v(TAG, "ENTRY startCCDTSync()")

      printWriter = createRecordsOutputWriter(ccdtFileName)

      // Start reading CCDT record
      if (remainingCCDTRecords > 0 ) {
        nexxtenderHomeCCDTStatusCharacteristic.getNotifications().onEach() {
          remainingCCDTRecords = it.value.fromUint32LE(0).toInt()
          Log.d(TAG, "Found the following notification of changed remainingCCDTRecords: $remainingCCDTRecords")
          updateCCDTState(remainingCCDTRecords)
          if (remainingCCDTRecords > 0 ) {
            // Read one more
            readCCDTRecordAndAskNext()
          } else {
            // We have read all CDR records
            printWriter.close()
            // stop notification
            val cancellationException = CancellationException()
            throw cancellationException
          }
        }.onCompletion{startEventSync()}.launchIn(viewModelScope)
        readCCDTRecordAndAskNext()
      } else {
        // There were no CCDT records
        printWriter.close()
        startEventSync()
      }
      Log.v(TAG, "RETURN startCCDTSync()")
    }
  }

  /**
   * Starts syncing the [event] records.
   * Once complete, it calls [startMetricSync()]
   */
  @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
  private suspend fun startEventSync() {
    viewModelScope.launch {
      Log.v(TAG, "ENTRY startEventSync()")

      printWriter = createRecordsOutputWriter(eventFileName)
      onlyGetMumberofRecords = false
      sendEventOperationUpdateStatus()
      Log.v(TAG, "RETURN startEventSync()")
    }
  }

  /**
   * Starts syncing the [metric] records.
   */
  @RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
  private suspend fun startmetricSync() {
    viewModelScope.launch {
      Log.v(TAG, "ENTRY startmetricSync()")

      printWriter = createRecordsOutputWriter(metricFileName)
      onlyGetMumberofRecords = false
      sendMetricOperationUpdateStatus()
      Log.v(TAG, "RETURN startmetricSync()")
    }
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  private suspend fun readCCDTRecordAndAskNext() {
    Log.v(TAG, "ENTRY readCCDTRecordAndAskNext()")
    val record = nexxtenderHomeCCDTRecordCharacteristic.read().value
    printRecord(record, 0)
    nexxtenderHomeCCDTCommandCharacteristic.write(DataByteArray.from(CCDT_COMMAND_NEXT))
    Log.v(TAG, "RETURN readCCDTRecordAndAskNext()")
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  private suspend fun readEventRecordAndAskNext() {
    Log.v(TAG, "ENTRY readEventRecordAndAskNext()")
    val record = nexxtenderHomeGenericDataCharacteristic.read().value
    printRecord(record, 0)
    sendEventOperationNext()
    Log.v(TAG, "RETURN readEventRecordAndAskNext()")
  }

  @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
  private suspend fun readMetricRecordAndAskNext() {
    Log.v(TAG, "ENTRY readMetricRecordAndAskNext()")
    val record = nexxtenderHomeGenericDataCharacteristic.read().value
    printRecord(record, 0)
    sendMetricOperationNext()
    Log.v(TAG, "RETURN readMetricRecordAndAskNext()")
  }

  /**
   * Puts the [genericCommand] on the queue and starts the first one on the queue if none was busy
   * @param genericCommand
   */
  private suspend fun queueGenericCommand(genericCommand: Int) {
    Log.v(TAG, "ENTRY queueGenericCommand() genericCommand:genericCommand")
    genericCommandQueue.add(genericCommand)
    startNextQueuedGenericCommandIfNotBusy()
    Log.v(TAG, "RETURN queueGenericCommand()")
  }

  /**
   * Starts the first command on the []genericCommandQueue] on the queue if none was busy.
   */
   private suspend fun startNextQueuedGenericCommandIfNotBusy() {
    Log.v(TAG, "ENTRY startNextQueuedGenericCommandIfNotBusy()")
    if (currentGenericCommand == -1) {
      if ( ! genericCommandQueue.isEmpty()) {
        currentGenericCommand = genericCommandQueue.remove()
        writeGenericCommand(currentGenericCommand)
      } else {
        Log.i(TAG, "startNextQueuedGenericCommandIfNotBusy(): no generic commands to queue")
      }
    }  else {
      Log.i(TAG, "startNextQueuedGenericCommandIfNotBusy(): previous command still busy")
    }
    Log.v(TAG, "RETURN startNextQueuedGenericCommandIfNotBusy()")
  }

  /**
   * Starts the first command on the [genericCommandQueue] on the queue if none was busy.
   */
  private suspend fun startNextQueuedGenericCommand() {
    Log.v(TAG, "ENTRY startNextQueuedGenericCommand()")
    currentGenericCommand = - 1
    startNextQueuedGenericCommandIfNotBusy()
    Log.v(TAG, "RETURN startNextQueuedGenericCommand()")
  }
}

/**
 * Represents Device Information fields.
 */
data class DeviceInformation(
  val modelNumber: String = "",
  val serialNumber: String = "",
  val firmwareRevision: String = "",
  val hardwareRevision: String = "",
  // Only on Mobile
  val manufacturerName: String = ""
)

/**
 * Represents information about CDR record.
 */
data class DataRecordsInformation(
  /**
   * The number of CDR records still available on the charger over BLE.
   */
  val remainingCDRRecords: Int = -1,

  /**
   * The number of CCDT records still available on the charger over BLE.
   */
  val remainingCCDTRecords: Int = -1,

  /**
   * The number of EVENT records still available on the charger over BLE.
   */
  val remainingEventRecords: Int = -1,

  /**
   * The number of METRIC records still available on the charger over BLE.
   */
  val remainingMetricRecords: Int = -1,
)

/**
 * Represents View State for a Device.
 */
data class DeviceDetailsViewState(
  val deviceName: String = "",
  val gattConnectionStateWithStatus: GattConnectionStateWithStatus = GattConnectionStateWithStatus(
    GattConnectionState.STATE_DISCONNECTED,
    BleGattConnectionStatus.UNKNOWN
  ),
  val bondState: BondState = BondState.NONE,
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
  val timeData: TimeData = TimeData(),
  val supportsDateUtc: Boolean = false,
  val dataRecordsInformation : DataRecordsInformation = DataRecordsInformation()
)
