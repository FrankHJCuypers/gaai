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

import no.nordicsemi.android.kotlin.ble.profile.common.CRC16

/**
 * Implementation of CRC-16-MODBUS over the given range of bytes.
 *
 * x<sup>16</sup>+x<sup>15</sup>+x<sup>2</sup>+x+1 or 0x8005 in normal presentation,
 * initial value 0xFFFF and no XORing at the end.
 *
 * See also
 * + [MODBUS](https://en.wikipedia.org/wiki/Modbus)
 * + [Cyclic_redundancy_check](https://en.wikipedia.org/wiki/Cyclic_redundancy_check)
 * + [Online CRC-8 CRC-16 CRC-32 Calculator](https://crccalc.com/)
 * + [CRC-16/MODBUS calculator](https://crccalc.com/?crc=263b130bff13b97472d0143635984d0bb76c7bb6f45dfcc381&method=CRC-16/MODBUS&datatype=hex&outtype=hex)
 * + [no.nordicsemi.android.kotlin.ble.profile.common.CRC16.CRC]
 *
 * @param data   The input data block for computation.
 * @param offset Offset from where the range starts.
 * @param length Length of the range in bytes.
 * @return the CRC-16-MODBUS.
 * @author Frank HJ Cuypers
 *
 */
fun CRC16.modBus(data: ByteArray, offset: Int, length: Int): Int {
  return CRC(0x8005, 0xFFFF, data, offset, length, refin = true, refout = true, xorout = 0x0000)
}