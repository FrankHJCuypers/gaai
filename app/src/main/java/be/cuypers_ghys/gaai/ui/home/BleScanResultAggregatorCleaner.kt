/*
 * Copyright (c) 2023, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2026, Frank HJ Cuypers
 * Based on no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator.
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
package be.cuypers_ghys.gaai.ui.home

import android.os.SystemClock
import android.util.Log
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResultData
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner

// Tag for logging
private const val TAG = "BleScanResultAggregateCleaner"
const val CUTOFF_PERIOD_NANO = 1000000000L // 1000ms. Nexxtender Home advertises itself every 25ms.

/**
 * Class responsible for aggregating scan results with a single server device.
 * By default [BleScanner] emits one [BleScanResult] at time.
 * Grouping data is a responsibility of this class.
 * Because devices that were advertising, but then no longer do (out of range, other connection),
 * are not removed from the aggregated list, [clean] must be called periodically.
 */
class BleScanResultAggregatorCleaner {
  private val devices = mutableMapOf<ServerDevice, BleScanResultData?>()
  val results
    get() = devices.map { BleScanResult(it.key, it.value) }

  /**
   * Adds new scan item to [devices] and returns aggregated values.
   *
   * @param scanItem New scan item.
   * @return Aggregated values.
   */
  fun aggregate(scanItem: BleScanResult): List<BleScanResult> {
    val data = scanItem.data
    devices[scanItem.device] = data
    return results
  }

  /**
   * Adds new scan item to [devices] and returns all [ServerDevice] which advertised something.
   * Can be used in a scenario when scan record data are not important and one want only to
   * display list of devices.
   *
   * @param scanItem New scan item.
   * @return [List] of all devices which advertised something.
   */
  fun aggregateDevices(scanItem: BleScanResult): List<ServerDevice> {
    return aggregate(scanItem).map { it.device }
  }

  /**
   * Removes all devices whose last scan occurrence was longer ago then a threshold
   * from the [devices] map.
   * @return true if something was indeed cleaned
   */
  fun clean(): Boolean {
    Log.d(TAG, "ENTRY clean() #devices=${devices.size}")
    var cleaned = false

    val elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
    val cutoffTimeNano = elapsedRealtimeNanos - CUTOFF_PERIOD_NANO
//    Log.v(TAG,"elapsedRealtimeNanos =$elapsedRealtimeNanos , cutoffTimeNanos = $cutoffTimeNano")
    devices.forEach { (serverDevice, bleScanResultData) ->
//      Log.v(TAG, "Testing serverDevice=$serverDevice ")
//      Log.v(TAG, " bleScanResultData=$bleScanResultData ")
//      Log.v(TAG, " bleScanResultData=$bleScanResultData.last() ")
      val lastTimeStamp = bleScanResultData?.timestampNanos
      if (bleScanResultData == null) {
//        Log.d(TAG, "bleScanResultData null  Removing $serverDevice")
        devices.remove(serverDevice)
        cleaned = true
      } else if (lastTimeStamp != null) {
        if ((lastTimeStamp < cutoffTimeNano)) {
//          Log.v(TAG, "timestampNanos<cutoffTimeNano: Removing $serverDevice")
          devices.remove(serverDevice)
          cleaned = true
        } else {
//          Log.v(TAG, "Not Removing $serverDevice, lastTimeStamp=$lastTimeStamp, cutoffTimeNano=$cutoffTimeNano ")
        }
      } else {
//        Log.v(TAG, "lastTimeStamp not available ")
      }
    }
    Log.d(TAG, "EXIT clean()  #devices=${devices.size}")
    return cleaned
  }
}
