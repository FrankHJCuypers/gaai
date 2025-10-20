/*
 * Project Gaai: one app to control the Nexxtender chargers.
 * Copyright © 2024-2025, Frank HJ Cuypers
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
package be.cuypers_ghys.gaai.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

/**
 * JUnit tests for [GetConfigVersion].
 * @author Frank HJ Cuypers
 */
class GetConfigVersionTest {

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun parse_VerifyResultsFromKnownTestVectors(
    version: String,
    expectedConfigVersion: ConfigVersion
  ) {
    assertEquals(expectedConfigVersion, ConfigDataParserComposer.getConfigVersion(version))
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
        Arguments.of("0.0", ConfigVersion.CONFIG_1_0),
        Arguments.of("0.0.0", ConfigVersion.CONFIG_1_0),
        Arguments.of("0.5.4", ConfigVersion.CONFIG_1_0),
        Arguments.of("1.0.0", ConfigVersion.CONFIG_1_0),
        Arguments.of("1.0.999", ConfigVersion.CONFIG_1_0),
        Arguments.of("1.1", ConfigVersion.CONFIG_1_1),
        Arguments.of("1.1.0", ConfigVersion.CONFIG_1_1),
        Arguments.of("1.1.1", ConfigVersion.CONFIG_1_1),
        Arguments.of("3.49.1", ConfigVersion.CONFIG_1_1),
        Arguments.of("3.50", ConfigVersion.CONFIG_CBOR),
        Arguments.of("3.50.0", ConfigVersion.CONFIG_CBOR),
        Arguments.of("3.50.1", ConfigVersion.CONFIG_CBOR),
        Arguments.of("4.5.6", ConfigVersion.CONFIG_CBOR),
      )
    }
  }
}