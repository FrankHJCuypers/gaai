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
package be.cuypers_ghys.gaai.data

import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


/**
 * @author Frank HJ Cuypers
 */
class ConfigDataParserComposerTest {

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun parse_VerifyResultsFromKnownTestVectors(
    configData: ByteArray,
    expectedMaxGrid: Int,
    expectedMaxDevice: Int,
    expectedMinDevice: Int,
    expectedMode: Mode,
    expectedSafe: Int,
    expectedNetworkType: NetWorkType,
    expectedTouWeekStart: Int,
    expectedTouWeekEnd: Int,
    expectedTouWeekendStart: Int,
    expectedTouWeekendEnd: Int,
    expectedICapacity: Int,
    expectedConfigurationVersion: ConfigVersion
  ) {

    mockkStatic(Log::class)
    every { Log.d(any(), any()) } returns 0

    val computedConfigGetData = when (expectedConfigurationVersion) {
      ConfigVersion.CONFIG_1_0 -> ConfigDataParserComposer.parseConfig_1_0(configData)
      ConfigVersion.CONFIG_1_1 -> ConfigDataParserComposer.parseConfig_1_1(configData)
      ConfigVersion.CONFIG_CBOR -> ConfigDataParserComposer.parseConfig_CBOR(configData)
    }
    assertNotNull(computedConfigGetData)
    assertEquals(expectedMaxGrid.toUByte(), computedConfigGetData!!.maxGrid)
    assertEquals(expectedMaxDevice.toUByte(), computedConfigGetData.maxDevice)
    assertEquals(expectedMinDevice.toUByte(), computedConfigGetData.minDevice)
    assertEquals(expectedMode, computedConfigGetData.mode)
    assertEquals(expectedSafe.toUByte(), computedConfigGetData.safe)
    assertEquals(expectedNetworkType, computedConfigGetData.networkType)
    assertEquals(expectedTouWeekStart.toShort(), computedConfigGetData.touWeekStart)
    assertEquals(expectedTouWeekEnd.toShort(), computedConfigGetData.touWeekEnd)
    assertEquals(expectedTouWeekendStart.toShort(), computedConfigGetData.touWeekendStart)
    assertEquals(expectedTouWeekendEnd.toShort(), computedConfigGetData.touWeekendEnd)
    assertEquals(expectedICapacity.toUByte(), computedConfigGetData.iCapacity)
    assertEquals(expectedConfigurationVersion, computedConfigGetData.configVersion)
  }

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun parseGeneral_VerifyResultsFromKnownTestVectors(
    configData: ByteArray,
    expectedMaxGrid: Int,
    expectedMaxDevice: Int,
    expectedMinDevice: Int,
    expectedMode: Mode,
    expectedSafe: Int,
    expectedNetworkType: NetWorkType,
    expectedTouWeekStart: Int,
    expectedTouWeekEnd: Int,
    expectedTouWeekendStart: Int,
    expectedTouWeekendEnd: Int,
    expectedICapacity: Int,
    expectedConfigurationVersion: ConfigVersion
  ) {
    mockkStatic(Log::class)
    every { Log.d(any(), any()) } returns 0

    val computedConfigGetData = ConfigDataParserComposer.parse(configData, expectedConfigurationVersion)

    assertNotNull(computedConfigGetData)
    assertEquals(expectedMaxGrid.toUByte(), computedConfigGetData!!.maxGrid)
    assertEquals(expectedMaxDevice.toUByte(), computedConfigGetData.maxDevice)
    assertEquals(expectedMinDevice.toUByte(), computedConfigGetData.minDevice)
    assertEquals(expectedMode, computedConfigGetData.mode)
    assertEquals(expectedSafe.toUByte(), computedConfigGetData.safe)
    assertEquals(expectedNetworkType, computedConfigGetData.networkType)
    assertEquals(expectedTouWeekStart.toShort(), computedConfigGetData.touWeekStart)
    assertEquals(expectedTouWeekEnd.toShort(), computedConfigGetData.touWeekEnd)
    assertEquals(expectedTouWeekendStart.toShort(), computedConfigGetData.touWeekendStart)
    assertEquals(expectedTouWeekendEnd.toShort(), computedConfigGetData.touWeekendEnd)
    assertEquals(expectedICapacity.toUByte(), computedConfigGetData.iCapacity)
    assertEquals(expectedConfigurationVersion, computedConfigGetData.configVersion)
  }

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun compose_VerifyResultsFromKnownTestVectors(
    expectedConfigData: ByteArray,
    maxGrid: Int,
    maxDevice: Int,
    minDevice: Int,
    mode: Mode,
    safe: Int,
    networkType: NetWorkType,
    touWeekStart: Int,
    touWeekEnd: Int,
    touWeekendStart: Int,
    touWeekendEnd: Int,
    iCapacity: Int,
    configurationVersion: ConfigVersion
  ) {

    val configData = ConfigData(
      maxGrid.toUByte(),
      maxDevice.toUByte(), mode,
      safe.toUByte(), networkType,
      touWeekStart.toShort(),
      touWeekEnd.toShort(),
      touWeekendStart.toShort(),
      touWeekendEnd.toShort(), minDevice.toUByte(), iCapacity.toUByte(), configurationVersion, false
    )
    val configDataArray = ConfigDataParserComposer.compose(configData)

    if (configurationVersion == ConfigVersion.CONFIG_CBOR) {
      /**
       * [ConfigDataParserComposer.compose] does not generate maps in which the pairs are sorted according to
       * an integer key value.
       * So literally comparing expectedConfigData and configDataArray is not possible
       */
      val recomputedConfigData = ConfigDataParserComposer.parseConfig_CBOR(configDataArray)
      assertEquals(configData, recomputedConfigData)
    } else {
      assertArrayEquals(expectedConfigData, configDataArray)
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parseConfig_1_0_LengthToShort() {
    assertNull(ConfigDataParserComposer.parseConfig_1_0("120578BCDEF0123456789A9E".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parseConfig_1_0_LengthToLong() {
    assertNull(ConfigDataParserComposer.parseConfig_1_0("120578BCDEF0123456789A9E1AFF".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parseConfig_1_0_IncorrectCRC16() {
    assertNull(ConfigDataParserComposer.parseConfig_1_0("120578BCDEF0123456789A6969".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parseConfig_1_1_LengthToShort() {
    assertNull(ConfigDataParserComposer.parseConfig_1_1("1234057800BCDEF0123456789ACC".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parseConfig_1_1_LengthToLong() {
    assertNull(ConfigDataParserComposer.parseConfig_1_1("1234057800BCDEF0123456789ACC2FFF".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parseConfig_1_1_IncorrectCRC16() {
    assertNull(ConfigDataParserComposer.parseConfig_1_1("1234057800BCDEF0123456789A6969".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parseConfig_CBOR_IncorrectCRC16() {
    assertNull(ConfigDataParserComposer.parseConfig_CBOR("A200A20101020101AB01000412051834060607186909000C19DEBC0D1912F00E1956340F199A781318696E71".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parseConfig_CBOR_FirstTypeNotMapAndIncorrect() {
    assertNull(ConfigDataParserComposer.parseConfig_CBOR("8200A20101020101AB01060418F00518F00618F007186909030C1977660D1999880E19BBAA0F19DDCC13189606AB".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parseConfig_CBOR_FirstTypeNotMapAndCorrect() {
    assertNull(ConfigDataParserComposer.parseConfig_CBOR("00BF40".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parseConfig_CBOR_FirstTypeMapAndSubmap0Absent() {
    assertNull(ConfigDataParserComposer.parseConfig_CBOR("A101AB01000412051834060607186909000C19DEBC0D1912F00E1956340F199A781318695599".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parseConfig_CBOR_FirstTypeMapAndSubmap1Absent() {
    assertNull(ConfigDataParserComposer.parseConfig_CBOR("A100A201010201C28F".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parseConfig_CBOR_FirstTypeMapAndSubItem2NotMap() {
    assertNull(ConfigDataParserComposer.parseConfig_CBOR("A200A2010102010101C464".hexToByteArray()))
  }

  companion object {
    /**
     * Returns the test vectors.
     *
     * See [Online CRC-8 CRC-16 CRC-32 Calculator](https://crccalc.com/) for CRC calculation.
     * See [CBOR playground](https://cbor.me/) for creating valid CBOR byte arrays.
     *
     * @return Stream of arguments to test
     */
    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    @Suppress("SpellCheckingInspection")
    fun usedCombinationsProvider(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(
          "1234057800BCDEF0123456789ACC2F".hexToByteArray(),
          0x12,
          0x34,
          0x00,
          Mode.MAX_OPEN,
          0x78,
          NetWorkType.MONO_TRIN,
          0xDEBC,
          0x12F0,
          0x5634,
          0x9A78,
          0x00,
          ConfigVersion.CONFIG_1_1
        ),
        Arguments.of(
          "112200440266778899AABBCCDD5AD6".hexToByteArray(),
          0x11,
          0x22,
          0x00,
          Mode.ECO_PRIVATE,
          0x44,
          NetWorkType.TRI,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x00,
          ConfigVersion.CONFIG_1_1
        ),
        Arguments.of(
          "112201440066778899AABBCCDD474A".hexToByteArray(),
          0x11,
          0x22,
          0x00,
          Mode.MAX_PRIVATE,
          0x44,
          NetWorkType.MONO_TRIN,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x00,
          ConfigVersion.CONFIG_1_1
        ),
        Arguments.of(
          "112204440066778899AABBCCDD5686".hexToByteArray(),
          0x11,
          0x22,
          0x00,
          Mode.ECO_OPEN,
          0x44,
          NetWorkType.MONO_TRIN,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x00,
          ConfigVersion.CONFIG_1_1
        ),
        Arguments.of(
          "112269446966778899AABBCCDD5179".hexToByteArray(),
          0x11,
          0x22,
          0x00,
          Mode.UNKNOWN,
          0x44,
          NetWorkType.UNKNOWN,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x00,
          ConfigVersion.CONFIG_1_1
        ),
        Arguments.of(
          "32200006003C00B10400003D00DD09".hexToByteArray(),
          0x32,
          0x20,
          0x00,
          Mode.ECO_PRIVATE,
          0x06,
          NetWorkType.MONO_TRIN,
          0x003C,
          0x04B1,
          0x0000,
          0x003D,
          0x00,
          ConfigVersion.CONFIG_1_1
        ),
        Arguments.of(
          "120578BCDEF0123456789A9E1A".hexToByteArray(),
          0x12,
          0x00,
          0x00,
          Mode.MAX_OPEN,
          0x78,
          NetWorkType.UNKNOWN,
          0xDEBC,
          0x12F0,
          0x5634,
          0x9A78,
          0x00,
          ConfigVersion.CONFIG_1_0
        ),
        Arguments.of(
          "11004466778899AABBCCDD12EE".hexToByteArray(),
          0x11,
          0x00,
          0x00,
          Mode.ECO_PRIVATE,
          0x44,
          NetWorkType.UNKNOWN,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x00,
          ConfigVersion.CONFIG_1_0
        ),
        Arguments.of(
          "11044466778899AABBCCDD533B".hexToByteArray(),
          0x11,
          0x00,
          0x00,
          Mode.ECO_OPEN,
          0x44,
          NetWorkType.UNKNOWN,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x00,
          ConfigVersion.CONFIG_1_0
        ),
        Arguments.of(
          "11014466778899AABBCCDD432B".hexToByteArray(),
          0x11,
          0x00,
          0x00,
          Mode.MAX_PRIVATE,
          0x44,
          NetWorkType.UNKNOWN,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x00,
          ConfigVersion.CONFIG_1_0
        ),
        Arguments.of(
          "11694466778899AABBCCDDC17F".hexToByteArray(),
          0x11,
          0x00,
          0x00,
          Mode.UNKNOWN,
          0x44,
          NetWorkType.UNKNOWN,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x00,
          ConfigVersion.CONFIG_1_0
        ),
        Arguments.of(
          "A200A20101020101AB01000412051834060607186909000C19DEBC0D1912F00E1956340F199A781318696E7D".hexToByteArray(),
          0x12,
          0x34,
          0x06,
          Mode.ECO_PRIVATE,
          0x69,
          NetWorkType.MONO_TRIN,
          0xDEBC,
          0x12F0,
          0x5634,
          0x9A78,
          0x69,
          ConfigVersion.CONFIG_CBOR
        ),
        Arguments.of(
          "A200A20101020101AB01050418F00518F00618F007186909020C1977660D1999880E19BBAA0F19DDCC13189602F5".hexToByteArray(),
          0xF0,
          0xF0,
          0xF0,
          Mode.MAX_OPEN,
          0x69,
          NetWorkType.TRI,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x96,
          ConfigVersion.CONFIG_CBOR
        ),
        Arguments.of(
          "A200A20101020101AB01010418F00518F00618F007186909000C1977660D1999880E19BBAA0F19DDCC1318967A63".hexToByteArray(),
          0xF0,
          0xF0,
          0xF0,
          Mode.MAX_PRIVATE,
          0x69,
          NetWorkType.MONO_TRIN,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x96,
          ConfigVersion.CONFIG_CBOR
        ),
        Arguments.of(
          "A200A20101020101AB01040418F00518F00618F007186909000C1977660D1999880E19BBAA0F19DDCC1318967AFA".hexToByteArray(),
          0xF0,
          0xF0,
          0xF0,
          Mode.ECO_OPEN,
          0x69,
          NetWorkType.MONO_TRIN,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x96,
          ConfigVersion.CONFIG_CBOR
        ),
        Arguments.of(
          "A200A20101020101AB01060418F00518F00618F007186909030C1977660D1999880E19BBAA0F19DDCC1318963F53".hexToByteArray(),
          0xF0,
          0xF0,
          0xF0,
          Mode.UNKNOWN,
          0x69,
          NetWorkType.UNKNOWN,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x96,
          ConfigVersion.CONFIG_CBOR
        ),
        Arguments.of( // added all unused and an unknown tag
          "A200A20101020101B40106020B030B0418F00518F00618F0071869080B09030A0B0B0B0C1977660D1999880E19BBAA0F19DDCC100B110B120B131896140B50B2".hexToByteArray(),
          0xF0,
          0xF0,
          0xF0,
          Mode.UNKNOWN,
          0x69,
          NetWorkType.UNKNOWN,
          0x7766,
          0x9988,
          0xBBAA,
          0xDDCC,
          0x96,
          ConfigVersion.CONFIG_CBOR
        ),
      )
    }
  }
}