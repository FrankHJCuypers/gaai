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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Calendar
import java.util.stream.Stream

/**
 * JUnit tests for the [TouTime] class.
 * @author Frank HJ Cuypers
 */
class TouTimeTest {

  @OptIn(ExperimentalMaterial3Api::class)
  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun verifyResultsFromKnownTestVectors(time: Int, expectedHours: Int, expectedMinutes: Int, expectedString: String) {
    val touTime = TouTime(time.toShort())
    val expectedCalendar = Calendar.getInstance()
    expectedCalendar.set(Calendar.HOUR_OF_DAY, expectedHours)
    expectedCalendar.set(Calendar.MINUTE, expectedMinutes)
    expectedCalendar.isLenient = false
    Assertions.assertEquals(expectedCalendar.isLenient, touTime.getCalendar().isLenient)
    Assertions.assertEquals(expectedCalendar.firstDayOfWeek, touTime.getCalendar().firstDayOfWeek)
    Assertions.assertEquals(expectedCalendar.minimalDaysInFirstWeek, touTime.getCalendar().minimalDaysInFirstWeek)
    Assertions.assertEquals(expectedCalendar.getTimeZone(), touTime.getCalendar().getTimeZone())
    Assertions.assertEquals(expectedCalendar.get(Calendar.HOUR_OF_DAY), touTime.getCalendar().get(Calendar.HOUR_OF_DAY))
    Assertions.assertEquals(expectedCalendar.get(Calendar.MINUTE), touTime.getCalendar().get(Calendar.MINUTE))
    Assertions.assertEquals(expectedString, touTime.toString())

    val touTime2 = TouTime(expectedHours, expectedMinutes)
    Assertions.assertEquals(touTime.time, touTime2.time)

    val timePickerState = TimePickerState(initialHour = expectedHours, initialMinute = expectedMinutes, is24Hour = true)
    val touTime3 = TouTime(timePickerState)
    Assertions.assertEquals(touTime.time, touTime3.time)
  }

  @Test
  fun timeOverflow() {
    val touTime = TouTime(2000)
    assertThrows(IllegalArgumentException::class.java) {
      touTime.getCalendar().get(Calendar.HOUR_OF_DAY)
    }
  }

  @Test
  fun timeNegative() {
    val touTime = TouTime(-1)
    assertThrows(IllegalArgumentException::class.java) {
      touTime.getCalendar().get(Calendar.HOUR_OF_DAY)
    }
  }

  companion object {
    /**
     * Returns the test vectors.
     *
     * @return Stream of arguments to test
     */
    @JvmStatic
    @Suppress("unused")
    fun usedCombinationsProvider(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(1234, 20, 34, "20:34"),
        Arguments.of(0, 0, 0, "00:00"),
        Arguments.of(720, 12, 0, "12:00"),
        Arguments.of(1439, 23, 59, "23:59"),
      )
    }
  }
}