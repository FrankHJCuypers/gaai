/*
 * Project Gaai: one app to control the Nexxtender chargers.
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
 * Possible charge type values.
 *
 * @author Frank HJ Cuypers
 */
enum class ChargeType {
  DEFAULT,
  MAX, UNKNOWN
}

/**
 * Holds the information regarding a Badge.
 *
 * @author Frank HJ Cuypers
 */
@Suppress("ArrayInDataClass", "ArrayInDataClass")
data class Badge(
  /** The UUID without the leading length byte send by the charger. */
  val uuid: ByteArray,

  /**
   * The charge type.
   */
  val chargeType: ChargeType = ChargeType.UNKNOWN

)
