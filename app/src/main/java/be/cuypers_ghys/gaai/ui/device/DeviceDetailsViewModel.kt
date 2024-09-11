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
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.DevicesRepository
import be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices

// Tag for logging
private const val TAG = "DeviceDetailsViewModel"

/**
 * ViewModel to show the device details.
 */
class DeviceDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val devicesRepository: DevicesRepository,
    private val bleRepository: BleRepository
) : ViewModel() {

    private val deviceId: Int = checkNotNull(savedStateHandle[DeviceDetailsDestination.deviceIdArg])
    private val gaaiDevice = getDevice(deviceId)!!

    private val _device = MutableStateFlow<Device?>(null)
    val device = _device.asStateFlow()

    private val _state = MutableStateFlow(DeviceDetailsViewState())
    val state = _state.asStateFlow()

    private var client: ClientBleGatt? = null

    init {
        _device.value= gaaiDevice
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

    @SuppressLint("MissingPermission")
    private fun startGattClient(gaaiDevice: Device) = viewModelScope.launch{
        Log.d(TAG, "Starting Gatt Client for gaaiDevice: $gaaiDevice")

        //Connect a Bluetooth LE device.
        val client = bleRepository.getClientBleGattConnection( gaaiDevice.mac, viewModelScope).also {
            this@DeviceDetailsViewModel.client = it
        }

        if (!client.isConnected) {
            Log.d(TAG, "Gatt Client not connected.")
            return@launch
        }
        Log.d(TAG, "Gatt Client connected. Discovering services.")

        //Discover services on the Bluetooth LE Device.
        val services = client.discoverServices()
        configureGatt(services)
    }

    @SuppressLint("MissingPermission")
    private suspend fun configureGatt(services: ClientBleGattServices) {
        Log.d(TAG, "Found the following services: $services")

        //Remember needed service and characteristics which are used to communicate with the DK.
        val genericAccessService = services.findService(NexxtenderHomeSpecification.UUID_BLE_GENERIC_ACCESS_SERVICE)!!
        deviceNameCharacteristic = genericAccessService.findCharacteristic(NexxtenderHomeSpecification.UUID_BLE_DEVICE_NAME_CHARACTERISTIC)!!

        val deviceInformationService = services.findService(NexxtenderHomeSpecification.UUID_BLE_DEVICE_INFORMATION_SERVICE)!!
        modelNumberStringCharacteristic = deviceInformationService.findCharacteristic(NexxtenderHomeSpecification.UUID_BLE_MODEL_NUMBER_STRING_CHARACTERISTIC)!!
        serialNumberStringCharacteristic = deviceInformationService.findCharacteristic(NexxtenderHomeSpecification.UUID_BLE_SERIAL_NUMBER_STRING_CHARACTERISTIC)!!
        firmwareRevisionStringCharacteristic = deviceInformationService.findCharacteristic(NexxtenderHomeSpecification.UUID_BLE_FIRMWARE_REVISION_STRING_CHARACTERISTIC)!!
        hardwareRevisionStringCharacteristic = deviceInformationService.findCharacteristic(NexxtenderHomeSpecification.UUID_BLE_HARDWARE_REVISION_STRING_CHARACTERISTIC)!!

        val nexxtenderGenericService = services.findService(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_CDR_SERVICE)!!
        nexxtenderHomeChargingBasicDataCharacteristic = nexxtenderGenericService.findCharacteristic(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_BASIC_DATA_CHARACTERISTIC)!!
        nexxtenderHomeChargingGridDataCharacteristic = nexxtenderGenericService.findCharacteristic(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_GRID_DATA_CHARACTERISTIC)!!
        nexxtenderHomeChargingCarDataCharacteristic = nexxtenderGenericService.findCharacteristic(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_CAR_DATA_CHARACTERISTIC)!!
        nexxtenderHomeChargingAdvancedDataCharacteristic = nexxtenderGenericService.findCharacteristic(NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_ADVANCED_DATA_CHARACTERISTIC)!!



        val deviceName = deviceNameCharacteristic.read().value.toString(Charsets.UTF_8)
        val modelNumber = modelNumberStringCharacteristic.read().value.toString(Charsets.UTF_8)
        val serialNumber = serialNumberStringCharacteristic.read().value.toString(Charsets.UTF_8)
        val firmwareRevision = firmwareRevisionStringCharacteristic.read().value.toString(Charsets.UTF_8)
        val hardwareRevision = hardwareRevisionStringCharacteristic.read().value.toString(Charsets.UTF_8)
        val deviceInformation = DeviceInformation(modelNumber=modelNumber,serialNumber=serialNumber,
            firmwareRevision=firmwareRevision,hardwareRevision=hardwareRevision )

        val chargingBasicData =  ChargingBasicDataParser.parse(nexxtenderHomeChargingBasicDataCharacteristic.read().value)!!
        Log.d(TAG, "Found the following chargingBasicData: $chargingBasicData")

        val chargingGridData =  ChargingGridDataParser.parse(nexxtenderHomeChargingGridDataCharacteristic.read().value)!!
        Log.d(TAG, "Found the following chargingGridData: $chargingGridData")

        val chargingCarData =  ChargingCarDataParser.parse(nexxtenderHomeChargingCarDataCharacteristic.read().value)!!
        Log.d(TAG, "Found the following chargingCarData: $chargingCarData")

        val chargingAdvancedData =  ChargingAdvancedDataParser.parse(nexxtenderHomeChargingAdvancedDataCharacteristic.read().value)!!
        Log.d(TAG, "Found the following chargingAdvancedData: $chargingAdvancedData")

        _state.value=_state.value.copy(deviceName = deviceName, deviceInformation=deviceInformation,
            chargingBasicData=chargingBasicData, chargingGridData= chargingGridData,
            chargingCarData= chargingCarData, chargingAdvancedData = chargingAdvancedData)

    }

    fun navigateBack( navigateBack: () -> Unit) {
        viewModelScope.launch {
            client?.disconnect()
            navigateBack()
        }
    }
    fun getDevice(deviceId: Int) = runBlocking {
        Log.d(TAG, "Getting Device with id $deviceId")
        return@runBlocking devicesRepository.getDeviceStream(deviceId).first()
    }
}

/**
 * Represents DeviceInformation fields.
 */
data class DeviceInformation(
    val modelNumber : String = "",
    val serialNumber : String = "",
    val firmwareRevision : String = "",
    val hardwareRevision : String = ""
)

/**
 * Represents View State for a Device.
 */
data class DeviceDetailsViewState(
    val deviceName : String = "",
    val deviceInformation : DeviceInformation = DeviceInformation(),
    val chargingBasicData : ChargingBasicData = ChargingBasicData(),
    val chargingGridData : ChargingGridData = ChargingGridData(),
    val chargingCarData : ChargingCarData = ChargingCarData(),
    val chargingAdvancedData : ChargingAdvancedData = ChargingAdvancedData(),
)
