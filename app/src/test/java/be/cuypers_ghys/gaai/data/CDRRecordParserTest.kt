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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.String
import java.text.SimpleDateFormat
import java.util.Date
import java.util.stream.Stream

/**
 * @author Frank HJ Cuypers
 */
class CDRRecordParserTest {

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun parse_VerifyResultsFromKnownTestVectors(
    cdrRecord: ByteArray,
    expectedSessionStartTime: Long,
    expectedSessionStartEnergy: Long,
    expectedSessionStopTime: Long,
    expectedSessionStopEnergy: Long,
  ) {
    val cdrRecord = CDRRecordParser.parse(cdrRecord)
    assertNotNull(cdrRecord)
    assertEquals(expectedSessionStartTime.toUInt(), cdrRecord!!.sessionStartTime)
    assertEquals(expectedSessionStartEnergy.toUInt(), cdrRecord!!.sessionStartEnergy)
    assertEquals(expectedSessionStopTime.toUInt(), cdrRecord!!.sessionStopTime)
    assertEquals(expectedSessionStopEnergy.toUInt(), cdrRecord!!.sessionStopEnergy)

    val expectedSessionStartTimeDate = Date(expectedSessionStartTime * 1000)
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SS")
    val timess = sdf.format(expectedSessionStartTimeDate)

    val expectedSessionStopTimeDate = Date(expectedSessionStopTime * 1000)
    val timese = sdf.format(expectedSessionStopTimeDate)

    val formatted = String.format(
      "0x%08d %s 0x%08x 0x%08x 0x%08x %s 0x%08x 0x%04x",
      cdrRecord.unknown1,
      timess,
      cdrRecord.sessionStartEnergy.toLong(),
      cdrRecord.unknown2.toLong(),
      cdrRecord.unknown3.toLong(),
      timese,
      cdrRecord.sessionStopEnergy.toLong(),
      cdrRecord.unknown4,
    )
    println(formatted)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parse_CDRRecordLengthToShort() {
    assertNull(CDRRecordParser.parse("0000000019c38f67c8f82d000000000000000000689790671c8e2e0060005e".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parse_CDRRecordLengthToLong() {
    assertNull(CDRRecordParser.parse("0000000019c38f67c8f82d000000000000000000689790671c8e2e0060005ec3EF".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parse_CDRRecordIncorrectCRC16() {
    assertNull(CDRRecordParser.parse("0000000019c38f67c8f82d000000000000000000689790671c8e2e0060006969".hexToByteArray()))
  }

  companion object {
    /**
     * Returns the test vectors.
     *
     * @return Stream of arguments to test
     */
    @OptIn(ExperimentalStdlibApi::class)
    @JvmStatic
    @Suppress("SpellCheckingInspection")
    fun usedCombinationsProvider(): Stream<Arguments> {
      return Stream.of(
        Arguments.of(
          "0000000019c38f67c8f82d000000000000000000689790671c8e2e0060005ec3".hexToByteArray(),
          0x678fc319,
          0x002df8c8,
          0x67909768,
          0x002e8e1c
        ),

        // From HCI-BLE log between NExxtmove and NExxtender Home of 14-3-2025
        Arguments.of(
          "000000003b029a67958a2f00000000000000000014169a6744a42f0060009a58".hexToByteArray(),
          0x679a023b,
          0x002f8a95,
          0x679a1614,
          0x002fa444
        ),
        Arguments.of(
          "00000000bcefbc6744a42f0000000000000000004b80bd67f72230006000d3bd".hexToByteArray(),
          0x67bcefbc,
          0x002fa444,
          0x67bd804b,
          0x003022f7
        ),
        Arguments.of(
          "0000000012d4bd67f7223000000000000000000030edbd67425630006000f771".hexToByteArray(),
          0x67bdd412,
          0x003022f7,
          0x67bded30,
          0x00305642
        ),
        Arguments.of(
          "0000000089f4bd6742563000000000000000000096b2be674d8030006000e102".hexToByteArray(),
          0x67bdf489,
          0x00305642,
          0x67beb296,
          0x0030804d
        ),
        Arguments.of(
          "000000001ad1c1674d803000000000000000000030b8c26746db300060006c20".hexToByteArray(),
          0x67c1d11a,
          0x0030804d,
          0x67c2b830,
          0x0030db46
        ),
        Arguments.of(
          "00000000f341c46746db3000000000000000000098bbc567e49431006000ddf1".hexToByteArray(),
          0x67c441f3,
          0x0030db46,
          0x67c5bb98,
          0x003194e4
        ),
        Arguments.of(
          "000000000313c867e494310000000000000000000114c867e494310040009383".hexToByteArray(),
          0x67c81303,
          0x003194e4,
          0x67c81401,
          0x003194e4
        ),
        Arguments.of(
          "000000000d14c867e49431000000000000000000342dc867ceaa31006000fc63".hexToByteArray(),
          0x67c8140d,
          0x003194e4,
          0x67c82d34,
          0x0031aace
        ),
        Arguments.of(
          "000000003cbfca67ceaa310000000000000000001df0ca67c8d231006000b42a".hexToByteArray(),
          0x67cabf3c,
          0x0031aace,
          0x67caf01d,
          0x0031d2c8
        ),
        Arguments.of(
          "000000007a48d167c8d231000000000000000000c379d16719ed31005a00bb7e".hexToByteArray(),
          0x67d1487a,
          0x0031d2c8,
          0x67d179c3,
          0x0031ed19
        ),
        Arguments.of(
          "00000000bb9bd26719ed31000000000000000000c1b6d26799fb31005c0068c5".hexToByteArray(),
          0x67d29bbb,
          0x0031ed19,
          0x67d2b6c1,
          0x0031fb99
        ),
      )
    }
  }
}