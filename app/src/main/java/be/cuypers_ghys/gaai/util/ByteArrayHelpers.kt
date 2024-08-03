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
 * Writes the *value* as a 2-byte Little Endian value ot position *offset* in the ByteArray.
 * @param offset Offset to start writing in the ByteArray.
 * @param value Value to write in the ByteArray.
 */
fun ByteArray.toUint16LE(offset: Int, value: UInt) {
    this[offset] = (value and 0xFFu).toByte()
    this[offset+1] = (value shr 8).toByte()
}

/**
 * Writes the *value* as a 2-byte Big Endian value ot position *offset* in the ByteArray.
 * @param offset Offset to start writing in the ByteArray.
 * @param value Value to write in the ByteArray.
 */
fun ByteArray.toUint16BE(offset: Int, value: UInt) {
    this[offset] = (value shr 8).toByte()
    this[offset+1] = (value and 0xFFu).toByte()
}

