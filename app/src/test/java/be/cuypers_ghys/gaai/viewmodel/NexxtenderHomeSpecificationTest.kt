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

package be.cuypers_ghys.gaai.viewmodel

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID
import java.util.stream.Stream

class NexxtenderHomeSpecificationTest {

    @ParameterizedTest
    @MethodSource("usedCombinationsProvider")
    fun verifyResultsFromKnownCorrectTestVectors(expectedUUIDString: String, uuid: UUID) {
        Assertions.assertEquals(expectedUUIDString, uuid.toString())
    }

    companion object {
        /**
         * Returns the test vectors.
         *
         * @return Stream of arguments to test
         */
        @JvmStatic
        fun usedCombinationsProvider(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("00001800-0000-1000-8000-00805f9b34fb", NexxtenderHomeSpecification.UUID_BLE_GENERIC_ACCESS_SERVICE),
                Arguments.of("00002a00-0000-1000-8000-00805f9b34fb", NexxtenderHomeSpecification.UUID_BLE_DEVICE_NAME_CHARACTERISTIC),
                Arguments.of("00002a01-0000-1000-8000-00805f9b34fb", NexxtenderHomeSpecification.UUID_BLE_APPEARANCE_CHARACTERISTIC),
                Arguments.of("00002a04-0000-1000-8000-00805f9b34fb", NexxtenderHomeSpecification.UUID_BLE_PERIPHERAL_PREFERRED_CONNECTION_CHARACTERISTIC),
                Arguments.of("00001801-0000-1000-8000-00805f9b34fb", NexxtenderHomeSpecification.UUID_BLE_GENERIC_ATTRIBUTE_SERVICE),
                Arguments.of("00002a05-0000-1000-8000-00805f9b34fb", NexxtenderHomeSpecification.UUID_BLE_SERVICE_CHANGED_CHARACTERISTIC),
                Arguments.of("0000180a-0000-1000-8000-00805f9b34fb", NexxtenderHomeSpecification.UUID_BLE_DEVICE_INFORMATION_SERVICE),
                Arguments.of("00002a24-0000-1000-8000-00805f9b34fb", NexxtenderHomeSpecification.UUID_BLE_MODEL_NUMBER_STRING_CHARACTERISTIC),
                Arguments.of("00002a25-0000-1000-8000-00805f9b34fb", NexxtenderHomeSpecification.UUID_BLE_SERIAL_NUMBER_STRING_CHARACTERISTIC),
                Arguments.of("00002a26-0000-1000-8000-00805f9b34fb", NexxtenderHomeSpecification.UUID_BLE_FIRMWARE_REVISION_STRING_CHARACTERISTIC),
                Arguments.of("00002a27-0000-1000-8000-00805f9b34fb", NexxtenderHomeSpecification.UUID_BLE_HARDWARE_REVISION_STRING_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75c0", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_SERVICE_DATA_SERVICE),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75c1", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_CDR_SERVICE),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75dd", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_COMMAND_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75de", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_STATUS_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75df", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_GENERIC_DATA_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75c5", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CCDT_SERVICE),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75c6", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CCDT_COMMAND_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75c7", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CCDT_STATUS_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75c8", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CCDT_RECORD_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75c9", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_FIRMWARE_SERVICE),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75ca", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_FIRMWARE_COMMAND_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75cb", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_FIRMWARE_STATUS_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75cc", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_FIRMWARE_WANTED_CHUNK_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75cd", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_FIRMWARE_DATA_CHUNK_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75ce", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_SERVICE),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75cf", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_BASIC_DATA_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75d0", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_GRID_DATA_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75da", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_CAR_DATA_CHARACTERISTIC),
                Arguments.of("fd47416a-95fb-4206-88b5-b4a8045f75db", NexxtenderHomeSpecification.UUID_NEXXTENDER_HOME_CHARGING_ADVANCED_DATA_CHARACTERISTIC))
        }
    }
}