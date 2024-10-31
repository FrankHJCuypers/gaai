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

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Handles a Time Of Use (tou) time.
 *
 * @property time Time in minutes since midnight.
 * @constructor Creates an instance with the specified time.
 *
 * @author Frank HJ Cuypers
 */
data class TouTime (
    val time: Short
){
    /**
     * Creates an instance with the specified [hours] and [minutes].
     * @param hours Hours part ot the time.
     * @param minutes minutes part of the time.
     */
    constructor(hours: Int, minutes: Int) :
        this((hours * 60 + minutes).toShort())

    /**
     * Creates an instance with the specified [timePickerState].
     * @param timePickerState Hours part ot the time.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    constructor(timePickerState: TimePickerState) :
            this(timePickerState.hour, timePickerState.minute)

    /**
     * Converts the time into a readable string.
     * @return Time on HH:mm format
     */
    override fun toString(): String {
        return format(getCalendar().time)
    }

    fun getCalendar(): Calendar{
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, getHours())
        cal.set(Calendar.MINUTE, getMinutes())
        cal.isLenient = false
        return cal
    }

    fun getHours() : Int {
        return time / 60
    }

    fun getMinutes() : Int {
        return time % 60
    }

    companion object {
        @SuppressLint("ConstantLocale")
        val hourMinutesFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        /**
         * Format the time.
         */
        fun format( time: Date): String {
            return hourMinutesFormatter.format(time)
        }
    }
}