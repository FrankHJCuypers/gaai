/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2026, Frank HJ Cuypers
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

/*
 * Based on  https://stackoverflow.com/questions/54827455/how-to-implement-timer-with-kotlin-coroutines
 */

package be.cuypers_ghys.gaai.ui.home

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration

// Tag for logging
private const val TAG = "TickerFlow"

/**
 * Starts a ticker flow.
 * @param period Time period in between two ticks.
 * @param initialDelay Time period before the first tick.
 */
fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
  Log.v(TAG, "START tickerFlow()")
  delay(initialDelay)
  while (true) {
    emit(Unit)
    delay(period)
  }
}