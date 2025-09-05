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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import be.cuypers_ghys.gaai.ble.BleRepository
import be.cuypers_ghys.gaai.data.Badge
import be.cuypers_ghys.gaai.data.BadgeParser
import be.cuypers_ghys.gaai.data.ChargeType
import be.cuypers_ghys.gaai.data.Device
import be.cuypers_ghys.gaai.data.DevicesRepository
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_OPERATION_ADD_DEFAULT
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_OPERATION_ADD_MAX
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_OPERATION_DELETE
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_OPERATION_LIST_NEXT
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_OPERATION_LIST_START
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_STATUS_WAIT_ADD1
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_STATUS_WAIT_ADD2
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_STATUS_WAIT_ADDED
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_STATUS_WAIT_DELETE
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_STATUS_WAIT_EXISTS
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_STATUS_WAIT_FINISH
import be.cuypers_ghys.gaai.data.OperationAndStatusIDs.BADGE_STATUS_WAIT_NEXT
import be.cuypers_ghys.gaai.util.fromUint16LE
import be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray

// Tag for logging
private const val TAG = "BadgeListViewModel"

/**
 * A listener that listens for a new [List] of [Badge]s.
 */
interface IBadgeListListener {
  /**
   * Informs the [IBadgeListListener] of a new [List] of [Badge]s.
   */
  fun badgeListChanged(badgeList: List<Badge>)
}

/**
 * Badge list manager, responsible for (un)registering [IBadgeListListener]s that want to be informed of updates.
 */
interface IBadgeListManager {
  /**
   * Register the [listener].
   * @param listener
   */
  fun register(listener: IBadgeListListener)

  /**
   * Unregister the [listener].
   * @param listener
   */
  fun unregister(listener: IBadgeListListener)
}

/**
 * Badge list manager, responsible for (un)registering [IBadgeListListener]s that want to be informed of updates and
 * for emitting new badge lists to the registered listeners.
 */
class BadgeListManager : IBadgeListManager {

  /**
   * The registered listeners.
   */
  private val listeners: MutableList<IBadgeListListener> = mutableListOf()

  /**
   * Register a new [listener].
   * @param listener
   */
  // TODO: must be thread safe
  override fun register(listener: IBadgeListListener) {
    listeners.add(listener)
  }

  /**
   * Unregister a [listener].
   * @param listener
   */
  // TODO: must be thread safe
  override fun unregister(listener: IBadgeListListener) {
    listeners.remove(listener)
  }

  /**
   * Inform all registered listeners of the new [badgeList].
   * @param badgeList
   */
  fun emitNewBadgeList(badgeList: List<Badge>) {
    Log.d(TAG, "emitNewBadgeList: $badgeList")

    listeners.forEach { it.badgeListChanged(badgeList) }
  }
}

/**
 * ViewModel to manage all badges from the charger, to be used by [BadgeListScreen].
 *
 * @param savedStateHandle [SavedStateHandle] passed by
 *  [AppViewModelProvider][be.cuypers_ghys.gaai.ui.AppViewModelProvider]
 * @param devicesRepository The [DevicesRepository] to use.
 * @param bleRepository The [BleRepository] to use.
 * @constructor Called by [AppViewModelProvider][be.cuypers_ghys.gaai.ui.AppViewModelProvider].
 *
 * @author Frank HJ Cuypers
 */
class BadgeListViewModel(
  savedStateHandle: SavedStateHandle,
  private val devicesRepository: DevicesRepository,
  private val bleRepository: BleRepository
) : ViewModel(

) {

  /**
   * The badge that must be deleted on the charger using BLE.
   */
  private lateinit var badgeToDelete: Badge

  /**
   * The id of the [Device] for which to build a state.
   */
  private val deviceId: Int = checkNotNull(savedStateHandle[BadgeListDestination.DEVICE_ID_ARG])

  /**
   * The [Device] corresponding with [deviceId].
   */
  private val gaaiDevice = getDevice(deviceId)!!

  /**
   * Holds current device ui state.
   */
  @Suppress("unused")
  var badgeDeviceUiState by mutableStateOf(BadgeDeviceUiState())
//    private set

  /**
   * Initializes the [badgeDeviceUiState] with the value provided in the argument.
   * @param device The initial device details from which to compute the state.
   */
  private fun initBadgeDeviceUiState(device: Device) {
    badgeDeviceUiState =
      BadgeDeviceUiState(
        device = device
      )
  }

  /**
   * Updates the [badgeDeviceUiState] with the value provided in the argument.
   * @param statusId The initial status Id.
   */
  private fun updateBadgeDeviceUiState(statusId: Int) {
    badgeDeviceUiState = badgeDeviceUiState.copy(statusId = statusId)
  }

  private var client: ClientBleGatt? = null

  /**
   * Temporary badge lists. Used for preparing the list that will be send to [BadgeListManager.emitNewBadgeList()]
   */
  private val badgeList: MutableList<Badge> = mutableListOf()

  private val badgeListManager: BadgeListManager = BadgeListManager()

  /**
   * Generates a flow of List<Badge> using asynchronous events.
   * In order to obtain a list of badges, Gaai must interrogate the Charger over BLE and get all registered badges.
   * That involves severa BLE messages that are handled asynchronously.
   * Once the complete list is obtained, Gaai will execute a [BadgeListManager.emitNewBadgeList()] to generate a new
   * entry in the flow.
   *
   * See [callBackFlow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/callback-flow.html).
   * See [CallbackFlow in kotlin](https://medium.com/@appdevinsights/callbackflow-in-kotlin-b830a1498946).
   * @param badgeListManager
   * @return a [Flow] of [List] of [Badge]s.
   */
  private fun badgeListFlow(badgeListManager: IBadgeListManager): Flow<List<Badge>> = callbackFlow {
    val listener = object : IBadgeListListener {
      override fun badgeListChanged(badgeList: List<Badge>) {
        trySend(badgeList)
      }
    }

    badgeListManager.register(listener)
    awaitClose { badgeListManager.unregister(listener) }
  }

  init {
    initBadgeDeviceUiState(gaaiDevice)
    startGattClient(gaaiDevice)
  }

  private lateinit var nexxtenderHomeGenericCommandCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeGenericStatusCharacteristic: ClientBleGattCharacteristic
  private lateinit var nexxtenderHomeGenericDataCharacteristic: ClientBleGattCharacteristic

  /**
   * Starts a [ClientBleGatt] to communicate with the [gaaiDevice].
   * @param gaaiDevice [Device] for which to start a [ClientBleGatt].
   */
  @SuppressLint("MissingPermission")
  private fun startGattClient(gaaiDevice: Device) = viewModelScope.launch {
    Log.d(TAG, "Starting Gatt Client for gaaiDevice: $gaaiDevice")

    //Connect a Bluetooth LE device.
    val client = bleRepository.getClientBleGattConnection(gaaiDevice.mac, viewModelScope).also {
      this@BadgeListViewModel.client = it
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

  /**
   * Sets up the GATT services and characteristics required for the Nexxtender charger.
   * @param services Entry point for finding services and characteristics.
   */
  @SuppressLint("MissingPermission")
  private suspend fun configureGatt(services: ClientBleGattServices) {
    Log.d(TAG, "Found the following services: $services")

    val nexxtenderGenericService =
      services.findService(NexxtenderHomeSpecification.UUID_NEXXTENDER_CHARGER_GENERIC_CDR_SERVICE)!!
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

    // Launch notifications for dynamic data

    // Read Configuration Data
    nexxtenderHomeGenericStatusCharacteristic.getNotifications().onEach {
      Log.d(TAG, "Found Generic Status: $it")
      val status = it.value.fromUint16LE(0).toInt()
      Log.d(TAG, "Converted status: $status")
      updateBadgeDeviceUiState(status)
      when (status) {
        BADGE_STATUS_WAIT_ADD1, BADGE_STATUS_WAIT_ADD2 -> {
          // TODO: set state such that UI shows that badge must be presented.
        }

        BADGE_STATUS_WAIT_DELETE -> {
          Log.d(TAG, "Deleting badge: $badgeToDelete")
          nexxtenderHomeGenericDataCharacteristic.write(BadgeParser.getLengthUUID(badgeToDelete))
          startGettingNewBadgeList()
        }

        BADGE_STATUS_WAIT_NEXT -> {
          val badge = BadgeParser.parse(nexxtenderHomeGenericDataCharacteristic.read().value)!!
          Log.d(TAG, "Retrieved badge: $badge")

          badgeList.add(badge)
          sendBadgeListNext()
        }

        BADGE_STATUS_WAIT_FINISH -> {
          val newBadgeList = badgeList.toList()
          badgeListManager.emitNewBadgeList(newBadgeList)
        }

        BADGE_STATUS_WAIT_ADDED -> {
          BadgeParser.parse(nexxtenderHomeGenericDataCharacteristic.read().value)!!
          startGettingNewBadgeList()

        }

        BADGE_STATUS_WAIT_EXISTS -> {
          startGettingNewBadgeList()
        }

        else -> {
          Log.d(TAG, "Unknown GENERIC_STATUS value: $status")
        }
      }
    }.launchIn(viewModelScope)
    startGettingNewBadgeList()
  }

  private suspend fun startGettingNewBadgeList() {
    Log.d(TAG, "startGettingNewBadgeList")
    badgeList.clear()
    sendBadgeListStart()
  }

  /**
   * Writes [BADGE_OPERATION_LIST_START] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendBadgeListStart() {
    val command = BADGE_OPERATION_LIST_START
    writeGenericCommand(command)
  }

  /**
   * Writes [BADGE_OPERATION_LIST_NEXT] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendBadgeListNext() {
    val command = BADGE_OPERATION_LIST_NEXT
    writeGenericCommand(command)
  }

  /**
   * Writes [BADGE_OPERATION_ADD_MAX] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendBadgeAddMax() {
    val command = BADGE_OPERATION_ADD_MAX
    writeGenericCommand(command)
  }

  /**
   * Writes [BADGE_OPERATION_ADD_DEFAULT] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendBadgeAddDefault() {
    val command = BADGE_OPERATION_ADD_DEFAULT
    writeGenericCommand(command)
  }

  /**
   * Writes [BADGE_OPERATION_DELETE] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   */
  private suspend fun sendBadgeDelete() {
    val command = BADGE_OPERATION_DELETE
    writeGenericCommand(command)
  }


  /**
   * Writes [command] to the [Generic Command]
   * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC]
   * characteristic.
   * @param command The [OperationId][be.cuypers_ghys.gaai.data.OperationAndStatusIDs] to write.
   */
  @OptIn(ExperimentalStdlibApi::class)
  @SuppressLint("MissingPermission")
  private suspend fun writeGenericCommand(command: Int) {
    Log.d(TAG, "Writing Generic Command: $command (0x${command.toHexString()})")
    nexxtenderHomeGenericCommandCharacteristic.write(DataByteArray.fromUShort(command))
  }

  /**
   * Delete the [badge] from the charger.
   * @param badge The [Badge] to delete.
   */
  // TODO: We already emit a newBadgeList before actually deleting the badge from the charger.
  //  The original idea was that this was not necessary;
  //  rereading the badge list from the charger after the delete should have the same effect.
  //  But it did not work correctly, so I added the explicit emit.
  fun deleteBadge(badge: Badge) {
    viewModelScope.launch(Dispatchers.IO) {
      Log.d(TAG, "deleteBadge: $badge")
      // emit an updated badge list for the UI
      val newBadgeList = badgeList.toMutableList()
      newBadgeList.remove(badge)
      badgeListManager.emitNewBadgeList(newBadgeList)
      // And now delete the badge from the charger
      badgeToDelete = badge
      sendBadgeDelete()
    }
  }

  /**
   * Add a new [Badge] in the specified [be.cuypers_ghys.gaai.data.ChargeType].
   * @param chargeType The [be.cuypers_ghys.gaai.data.ChargeType] for the badge to add.
   */
  fun addBadge(chargeType: ChargeType) {
    viewModelScope.launch {
      when (chargeType) {
        ChargeType.DEFAULT -> sendBadgeAddDefault()
        ChargeType.MAX -> sendBadgeAddMax()
        ChargeType.UNKNOWN -> Unit // do nothing
      }
    }
  }

  /**
   * Holds badge list ui state. The list of badges are retrieved from the charger and mapped to
   * [BadgeListUiState]
   */
  val badgeListUiState: StateFlow<BadgeListUiState> = badgeListFlow(badgeListManager).map { BadgeListUiState(it) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
      initialValue = BadgeListUiState()
    )

  companion object {
    /**
     * Delay (in milliseconds) between the disappearance of the last
     * subscriber and the stopping of the sharing coroutine.
     */
    private const val TIMEOUT_MILLIS = 5_000L
  }

  /**
   * Loads the [Device] corresponding with [deviceId] from the database.
   * @param deviceId The id of the [Device] to load.
   */
  private fun getDevice(deviceId: Int) = runBlocking {
    Log.d(TAG, "Getting Device with id $deviceId")
    return@runBlocking devicesRepository.getDeviceStream(deviceId).first()
  }
}

/**
 * Ui State for BadgeListScreen
 */
data class BadgeListUiState(val badgeList: List<Badge> = listOf())

/**
 * Represents the UI State for the [Device].
 */
data class BadgeDeviceUiState(
  val device: Device = Device(),
  val statusId: Int = -1
)


