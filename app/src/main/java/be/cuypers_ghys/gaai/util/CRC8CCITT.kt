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
package be.cuypers_ghys.gaai.util

/**
 * Implementation of CRC-8-CCITT.
 *
 * x^8^+x^2^+x+1 or 0x7 in normal presentation, no initial value and no XORing at the end.
 *
 * Non-optimized version.
 *
 * See also [CRC_8_CCITT](https://www.3dbrew.org/wiki/CRC-8-CCITT),
 * [Cyclic_redundancy_check](https://en.wikipedia.org/wiki/Cyclic_redundancy_check)
 * and [Online CRC-8 CRC-16 CRC-32 Calculator](https://crccalc.com/).
 *
 * @author Frank HJ Cuypers
 */
class CRC8CCITT {
  /** The current value of theCRC.  */
  private var crc = INITIAL_VALUE

  /** Constructor  */
  init {
    reset()
  }

  /** Resets the CRC to the initial state.  */
  fun reset() {
    crc = INITIAL_VALUE
  }

  /**
   * Add byte to the CRC.
   * @param data The byte to add.
   * @return The current value of the CRC, unsigned.
   */
  private fun update(data: Byte): Int {
    var ch = data.toInt() and 0xFF
    ch = (ch xor this.crc) and 0xFF
    for (i in 0..7) {
      ch = if ((ch and 0x80) != 0) {
        ch shl 1 xor POLYNOMIAL
      } else {
        ch shl 1
      }
      ch = ch and 0xFF
    }
    this.crc = ch
    return this.crc
  }

  /**
   * Add bytes to the CRC.
   * @param data The bytes to add.
   * @param offset Start offset in *data*.
   * @param length Length of the bytes from *data*.
   * @return The current value of the CRC, unsigned.
   */
  //@JvmOverloads
  fun update(data: ByteArray, offset: Int = 0, length: Int = data.size): Int {
    var dataOffset = offset
    var remaining = length
    while (remaining > 0) {
      update(data[dataOffset])
      remaining--
      dataOffset++
    }
    return this.crc
  }

  /**
   * Add integer to the CRC.
   * @param data The integer to add.
   * @return The current value of the CRC, unsigned.
   */
  @Suppress("unused")
  fun update(data: Int): Int {
    // Add in Little Endian
    update(data.toByte())
    update((data shr 8).toByte())
    update((data shr 16).toByte())
    return update((data shr 24).toByte())
  }

  companion object {
    /** The initial value  */
    const val INITIAL_VALUE: Int = 0x0

    /** Normal Polynomial.  */
    const val POLYNOMIAL: Int = 0x07

    /**
     * Returns the size in bytes of the CRC value.
     * @return The size in bytes.
     */
    @Suppress("unused", "SameReturnValue")
    fun size(): Int {
      return 1
    }
  }
}