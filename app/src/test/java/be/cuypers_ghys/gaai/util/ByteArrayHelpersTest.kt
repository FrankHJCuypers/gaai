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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ByteArrayHelpersTest {

    @Test
    fun toUint32_LE_correct() {
        val ba = ByteArray(6)
        ba.toUint32LE(1, 0x12345678u)
        Assertions.assertEquals(0x0.toByte(), ba[0])
        Assertions.assertEquals(0x78.toByte(), ba[1])
        Assertions.assertEquals(0x56.toByte(), ba[2])
        Assertions.assertEquals(0x34.toByte(), ba[3])
        Assertions.assertEquals(0x12.toByte(), ba[4])
        Assertions.assertEquals(0x0.toByte(), ba[5])
    }

    @Test
    fun toUint32_LE_MSBitSetInAllBytes() {
        val ba = ByteArray(6)
        ba.toUint32LE(1, 0xC2D4E6F8u)
        Assertions.assertEquals(0x0.toByte(), ba[0])
        Assertions.assertEquals(0xF8.toByte(), ba[1])
        Assertions.assertEquals(0xE6.toByte(), ba[2])
        Assertions.assertEquals(0xD4.toByte(), ba[3])
        Assertions.assertEquals(0xC2.toByte(), ba[4])
        Assertions.assertEquals(0x0.toByte(), ba[5])
    }

    @Test
    fun toUint32_BE_correct() {
        val ba = ByteArray(6)
        ba.toUint32BE(1, 0x12345678u)
        Assertions.assertEquals(0x0.toByte(), ba[0])
        Assertions.assertEquals(0x12.toByte(), ba[1])
        Assertions.assertEquals(0x34.toByte(), ba[2])
        Assertions.assertEquals(0x56.toByte(), ba[3])
        Assertions.assertEquals(0x78.toByte(), ba[4])
        Assertions.assertEquals(0x0.toByte(), ba[5])
    }

    @Test
    fun toUint32_BE_MSBitSetInAllBytes() {
        val ba = ByteArray(6)
        ba.toUint32BE(1, 0xC2D4E6F8u)
        Assertions.assertEquals(0x0.toByte(), ba[0])
        Assertions.assertEquals(0xC2.toByte(), ba[1])
        Assertions.assertEquals(0xD4.toByte(), ba[2])
        Assertions.assertEquals(0xE6.toByte(), ba[3])
        Assertions.assertEquals(0xF8.toByte(), ba[4])
        Assertions.assertEquals(0x0.toByte(), ba[5])
    }

    @Test
    fun toUint16_LE_correct() {
        val ba = ByteArray(4)
        ba.toUint16LE(1, 0x1234u)
        Assertions.assertEquals(0x0.toByte(), ba[0])
        Assertions.assertEquals(0x34.toByte(), ba[1])
        Assertions.assertEquals(0x12.toByte(), ba[2])
        Assertions.assertEquals(0x0.toByte(), ba[3])
    }

    @Test
    fun toUint16_LE_MSBitSetInBothBytes() {
        val ba = ByteArray(4)
        ba.toUint16LE(1, 0x82F4u)
        Assertions.assertEquals(0x0.toByte(), ba[0] )
        Assertions.assertEquals(0xF4.toByte(), ba[1])
        Assertions.assertEquals(0x82.toByte(), ba[2])
        Assertions.assertEquals(0x0.toByte(), ba[3])
    }

    @Test
    fun toUint16_BE_correct() {
        val ba = ByteArray(4)
        ba.toUint16BE(1, 0x1234u)
        Assertions.assertEquals(0x0.toByte(), ba[0])
        Assertions.assertEquals(0x12.toByte(), ba[1])
        Assertions.assertEquals(0x34.toByte(), ba[2])
        Assertions.assertEquals(0x0.toByte(), ba[3])
    }

    @Test
    fun toUint16_BE_MSBitSetInBothBytes() {
        val ba = ByteArray(4)
        ba.toUint16BE(1, 0x82F4u)
        Assertions.assertEquals(0x0.toByte(), ba[0] )
        Assertions.assertEquals(/* expected = */ 0x82.toByte(), /* actual = */ ba[1])
        Assertions.assertEquals(0xF4.toByte(), ba[2])
        Assertions.assertEquals(0x0.toByte(), ba[3])
    }

    @Test
    fun fromUint16_LE_correct() {
        val ba = byteArrayOf(0x00, 0x12.toByte(), 0x34.toByte(), 0x00)
        Assertions.assertEquals(0x3412u.toUShort(), ba.fromUint16LE(1))
    }

    @Test
    fun fromUint16_LE_MSBitSetInBothBytes() {
        val ba = byteArrayOf(0x00, 0x82.toByte(), 0xF4.toByte(), 0x00)
        Assertions.assertEquals(0xF482u.toUShort(), ba.fromUint16LE(1))
    }

    @Test
    fun fromUint32_LE_correct() {
        val ba = byteArrayOf(0x00, 0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte(), 0x00)
        Assertions.assertEquals(0x78563412u, ba.fromUint32LE(1))
    }

    @Test
    fun fromUint32_LE_MSBitSetInBothBytes() {
        val ba = byteArrayOf(0x00, 0x82.toByte(), 0xF4.toByte(), 0xA6.toByte(), 0x98.toByte(), 0x00)
        Assertions.assertEquals(0x98A6F482u, ba.fromUint32LE(1))
    }

    @Test
    fun fromInt16_LE_correct() {
        val ba = byteArrayOf(0x00, 0x12.toByte(), 0x34.toByte(), 0x00)
        Assertions.assertEquals(0x3412.toShort(), ba.fromInt16LE(1))
    }

    @Test
    fun fromInt16_LE_MSBitSetInBothBytes() {
        val ba = byteArrayOf(0x00, 0x82.toByte(), 0xF4.toByte(), 0x00)
        Assertions.assertEquals(0xF482u.toShort(), ba.fromInt16LE(1))
    }

    @Test
    fun fromUint32_BE_correct() {
        val ba = byteArrayOf(0x00, 0x12.toByte(), 0x34.toByte(), 0x56.toByte(), 0x78.toByte(), 0x00)
        Assertions.assertEquals(0x12345678u, ba.fromUint32BE(1))
    }

    @Test
    fun fromUint32_BE_MSBitSetInBothBytes() {
        val ba = byteArrayOf(0x00, 0x82.toByte(), 0xF4.toByte(), 0xA6.toByte(), 0x98.toByte(), 0x00)
        Assertions.assertEquals(0x82F4A698u, ba.fromUint32BE(1))
    }

}