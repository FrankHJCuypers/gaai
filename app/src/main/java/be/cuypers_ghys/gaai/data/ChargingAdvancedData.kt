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

package be.cuypers_ghys.gaai.data

/**
 * Codes the bits in the authorization status.
 *
 * @author Frank HJ Cuypers
 */
// TODO: Make it a an enum like [Discriminator] and move the parsing logic to [ChargingAdvancedDataParser]
// like for the other Parsers
// TODO: create Junit tests for this class.
data class AuthorizationStatus(val authStatus: Byte) {

  @Suppress("MemberVisibilityCanBePrivate")
  companion object {
    const val UNAUTHORIZED_BIT = 0
    const val AUTHORIZED_DEFAULT_BIT = 1
    const val CHARGE_STOPPED_IN_APP_BIT = 2

    @Suppress("unused")
    const val RFU1_BIT = 3
    const val CHARGE_PAUSED_BIT = 4
    const val MAX_BIT = 5
    const val ECO_BIT = 6

    @Suppress("unused")
    const val RFU2_BIT = 7
    const val UNAUTHORIZED = (1 shl UNAUTHORIZED_BIT).toByte()
    const val AUTHORIZED = 1 shl AUTHORIZED_DEFAULT_BIT
    const val CHARGE_STOPPED_IN_APP = (1 shl CHARGE_STOPPED_IN_APP_BIT).toByte()
    const val CHARGE_PAUSED = (1 shl CHARGE_PAUSED_BIT).toByte()
    const val MAX = 1 shl MAX_BIT
    const val ECO = 1 shl ECO_BIT
    const val AUTHORIZED_DEFAULT = (AUTHORIZED).toByte()
    const val AUTHORIZED_MAX = (AUTHORIZED or MAX).toByte()
    const val AUTHORIZED_ECO = (AUTHORIZED or ECO).toByte()
  }
}


/**
 * Holds the result of the [Charging Advanced Data BLE Characteristic]
 * [be.cuypers_ghys.gaai.viewmodel.NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_ADVANCED_DATA_CHARACTERISTIC].
 *
 * @author Frank HJ Cuypers
 */
data class ChargingAdvancedData(
  /** Measurement time in [Unix Time](https://en.wikipedia.org/wiki/Unix_time). */
  val timestamp: UInt = 0u,

  /**
   * Available capacity in A.
   */
  val iAvailable: Short = 0,

  /**
   * Total power consumption from the grid in W.
   */
  val gridPower: Int = 0,

  /**
   * Total power consumption from the car in W.
   */
  val carPower: Int = 0,

  /**
   * Authorization status.
   *
   */
  val authorizationStatus: AuthorizationStatus = AuthorizationStatus(0),

  /**
   * Error code returned by the Nexxtender Home. Values unknown.
   */
  val errorCode: Byte = 0
)
