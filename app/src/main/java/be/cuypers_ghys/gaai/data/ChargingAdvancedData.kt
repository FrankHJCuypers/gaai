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

import android.content.Context
import be.cuypers_ghys.gaai.R

/**
 * Codes the bits in the authorization status.
 *
 * @author Frank HJ Cuypers
 */
// TODO: Make it a an enum like [Discriminator] and move the parsing logic to [ChargingAdvancedDataParser]
// like for the other Parsers
// TODO: create Junit tests for this class.
data class AuthorizationStatus ( val authStatus: Byte) {

    fun isSet(bit: Int ) : Boolean {
        return ((1 shl bit) and authStatus.toInt()) != 0
    }

        // TODO: move to a view. This is representation stuff.
    fun toString(context : Context): String {
       return when (authStatus) {
           AUTHORIZED_MAX -> context.getString(R.string.authorized_max)
           AUTHORIZED_ECO -> context.getString(R.string.authorized_eco)
           AUTHORIZED_DEFAULT -> context.getString(R.string.authorized_default)
           CHARGE_STOPPED_IN_APP-> context.getString(R.string.charge_stopped_in_app)
           CHARGE_PAUSED -> context.getString(R.string.charge_paused)
           UNAUTHORIZED -> context.getString(R.string.unauthorized)
           else -> context.getString(R.string.unknown)
       }
    }

    companion object {
        const val UNAUTHORIZED_BIT = 0
        const val AUTHORIZED_DEFAULT_BIT = 1
        const val CHARGE_STOPPED_IN_APP_BIT = 2
        const val RFU1_BIT = 3
        const val CHARGE_PAUSED_BIT = 4
        const val MAX_BIT = 5
        const val ECO_BIT = 6
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
 * Holds the result of the Charging Advanced Data BLE Characteristic.
 *
 * @author Frank HJ Cuypers
 */
data class ChargingAdvancedData(
    /** Measurement time in Unix Time. */
    val timestamp: UInt,
    /**
     * Available capacity in A.
     */
    val iAvailable: Short,
    /**
     * Total power consumption from the grid in W.
     */
    val gridPower: Int,
    /**
     * Total power consumption from the car in W.
     */
    val carPower: Int,
    /**
     * Authorization status.
     *
     */
    val authorizationStatus: AuthorizationStatus,
    /**
     * Error code returned by the Nexxtender Home. Values unknown.
     */
    val errorCode: Byte,
)
