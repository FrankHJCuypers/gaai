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
 * Writes the *value* as a 2-byte Little Endian value at position *offset* in the ByteArray.
 * @param offset Offset to start writing in the ByteArray.
 * @param value Value to write in the ByteArray.
 */
fun ByteArray.toUint16LE(offset: Int, value: UInt) {
    this[offset] = (value and 0xFFu).toByte()
    this[offset+1] = (value shr 8).toByte()
}

/**
 * Writes the *value* as a 2-byte Big Endian value at position *offset* in the ByteArray.
 * @param offset Offset to start writing in the ByteArray.
 * @param value Value to write in the ByteArray.
 */
fun ByteArray.toUint16BE(offset: Int, value: UInt) {
    this[offset] = (value shr 8).toByte()
    this[offset+1] = (value and 0xFFu).toByte()
}

/**
 * Writes the *value* as a 4-byte Little Endian value at position *offset* in the ByteArray.
 * @param offset Offset to start writing in the ByteArray.
 * @param value Value to write in the ByteArray.
 */
fun ByteArray.toUint32LE(offset: Int, value: UInt) {
    this[offset] = (value).toByte()
    this[offset+1] = (value shr 8).toByte()
    this[offset+2] = (value shr 16).toByte()
    this[offset+3] = (value shr 24).toByte()
}

/**
 * Reads the 2-byte Unsigned Little Endian value at position *offset* of the ByteArray.
 * @param offset Offset to start reading in the ByteArray.
 * @return Value read from the ByteArray.
 */
fun ByteArray.fromUint16LE(offset: Int) :UShort {
    return (( this[offset].toUInt() and 0xFFu) or (this[offset+1].toUInt() shl 8)).toUShort()
}

/**
 * Reads the 4-byte Unsigned Little Endian value at position *offset* of the ByteArray.
 * @param offset Offset to start reading in the ByteArray.
 * @return Value read from the ByteArray.
 */
fun ByteArray.fromUint32LE(offset: Int) :UInt {
    return (( this[offset].toUInt() and 0xFFu)
            or ((this[offset+1].toUInt() and 0xFFu) shl 8)
            or ((this[offset+2].toUInt() and 0xFFu) shl 16)
            or ((this[offset+3].toUInt() and 0xFFu) shl 24))
}

/**
 * Reads the 2-byte signed Little Endian value ot position *offset* of the ByteArray.
 * @param offset Offset to start reading in the ByteArray.
 * @return Value read from the ByteArray.
 */
fun ByteArray.fromInt16LE(offset: Int) :Short {
    return (( this[offset].toInt() and 0xFF) or (this[offset+1].toInt() shl 8)).toShort()
}

/**
 * Reads the 4-byte Signed Little Endian value at position *offset* of the ByteArray.
 * @param offset Offset to start reading in the ByteArray.
 * @return Value read from the ByteArray.
 */
fun ByteArray.fromInt32LE(offset: Int) :Int {
    return (( this[offset].toInt() and 0xFF)
            or ((this[offset+1].toInt() and 0xFF) shl 8)
            or ((this[offset+2].toInt() and 0xFF) shl 16)
            or ((this[offset+3].toInt() and 0xFF) shl 24))
}
