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

import be.cuypers_ghys.gaai.util.toHex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.stream.Stream

/**
 * JUnit tests for [EventRecordParser].
 * @author Frank HJ Cuypers
 */
class EventRecordParserTest {

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun parse_VerifyResultsFromKnownTestVectors(
    eventRecordByteArray: ByteArray,
    expectedTimestamp: Long
  ) {
    val eventRecord = EventRecordParser.parse(eventRecordByteArray)
    assertNotNull(eventRecord)
    assertEquals(expectedTimestamp.toUInt(), eventRecord!!.timestamp)

    val expectedTimestampTimeDate = Date(expectedTimestamp * 1000)
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SS")
    val times = sdf.format(expectedTimestampTimeDate)

    val formatted = String.format(
      "%s 0x%02x 0x%02x 0x%02x %s",
      times,
      eventRecord.unknown1,
      eventRecord.unknown2,
      eventRecord.unknown3,
      "0x" + eventRecord.unknown4.toHex()
    )
    println(formatted)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_EventRecordLengthToShort() {
    assertNull(EventRecordParser.parse("c0518667060108010003000000000000000010".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_EventRecordLengthToLong() {
    assertNull(EventRecordParser.parse("c05186670601080100030000000000000000102569".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Test
  fun parse_EventRecordIncorrectCRC16() {
    assertNull(EventRecordParser.parse("c051866706010801000300000000000000006969".hexToByteArray()))
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
          "c051866706010801000300000000000000001025".hexToByteArray(),
          0x678651c0
        ),
        // From HCI-BLE log between NExxtmove and NExxtender Home of 14-3-2025
        Arguments.of("c22798670201020100000000000000000000f763".hexToByteArray(), 0x679827c2),
        Arguments.of("b476986702010100000000000000000000006048".hexToByteArray(), 0x679876b4),
        Arguments.of("07bc98670201000100000000000000000000394a".hexToByteArray(), 0x6798bc07),
        Arguments.of("0fbc9867010102070497db82126880000000311b".hexToByteArray(), 0x6798bc0f),
        Arguments.of("10bc986702010102000000000000000000007e7d".hexToByteArray(), 0x6798bc10),
        Arguments.of("4af098670201020100000000000000000000b321".hexToByteArray(), 0x6798f04a),
        Arguments.of("cdfe986702010100000000000000000000004f27".hexToByteArray(), 0x6798fecd),
        Arguments.of("7be19967020100010000000000000000000089d0".hexToByteArray(), 0x6799e17b),
        Arguments.of("85e1996701010207048b90821268800000008f78".hexToByteArray(), 0x6799e185),
        Arguments.of("90e1996702010102000000000000000000003e87".hexToByteArray(), 0x6799e190),
        Arguments.of("c2e1996701014000000000000000000000007a69".hexToByteArray(), 0x6799e1c2),
        Arguments.of("b9fb99670201020100000000000000000000c6f4".hexToByteArray(), 0x6799fbb9),
        Arguments.of("c3fb99670201010000000000000000000000ff0d".hexToByteArray(), 0x6799fbc3),
        Arguments.of("3b029a67020100010000000000000000000065f3".hexToByteArray(), 0x679a023b),
        Arguments.of("41029a6701010207048b90821268800000000b82".hexToByteArray(), 0x679a0241),
        Arguments.of("42029a6702010102000000000000000000005e31".hexToByteArray(), 0x679a0242),
        Arguments.of("01139a670201020100000000000000000000ecb4".hexToByteArray(), 0x679a1301),
        Arguments.of("14169a670201010000000000000000000000eb15".hexToByteArray(), 0x679a1614),
        Arguments.of("224f9b6706010809000200000000000000000164".hexToByteArray(), 0x679b4f22),
        Arguments.of("a08bb56706010809000100000000000000001285".hexToByteArray(), 0x67b58ba0),
        Arguments.of("8896b5670601080100030000000000000000317b".hexToByteArray(), 0x67b59688),
        Arguments.of("9a65ba6706010801000500000000000000004e91".hexToByteArray(), 0x67ba659a),
        Arguments.of("9a65ba6706010809000400000000000000006961".hexToByteArray(), 0x67ba659a),
        Arguments.of("bcefbc670201000100000000000000000000ba71".hexToByteArray(), 0x67bcefbc),
        Arguments.of("c6efbc6701010207048b9082126880000000d400".hexToByteArray(), 0x67bcefc6),
        Arguments.of("c7efbc670201010200000000000000000000200b".hexToByteArray(), 0x67bcefc7),
        Arguments.of("110ebd6702010201000000000000000000002acd".hexToByteArray(), 0x67bd0e11),
        Arguments.of("3212bd67020101020000000000000000000012c5".hexToByteArray(), 0x67bd1232),
        Arguments.of("2e32bd670201020100000000000000000000c7d9".hexToByteArray(), 0x67bd322e),
        Arguments.of("4b80bd6702010100000000000000000000001d75".hexToByteArray(), 0x67bd804b),
        Arguments.of("12d4bd6702010001000000000000000000005815".hexToByteArray(), 0x67bdd412),
        Arguments.of("17d4bd6701010207048b90821268800000001fa8".hexToByteArray(), 0x67bdd417),
        Arguments.of("18d4bd6702010102000000000000000000008948".hexToByteArray(), 0x67bdd418),
        Arguments.of("2aedbd670201020100000000000000000000eee2".hexToByteArray(), 0x67bded2a),
        Arguments.of("30edbd670201010000000000000000000000c905".hexToByteArray(), 0x67bded30),
        Arguments.of("89f4bd6702010001000000000000000000001ed4".hexToByteArray(), 0x67bdf489),
        Arguments.of("91f4bd6701010207048b9082126880000000cf03".hexToByteArray(), 0x67bdf491),
        Arguments.of("92f4bd6702010102000000000000000000009ab0".hexToByteArray(), 0x67bdf492),
        Arguments.of("0509be6702010201000000000000000000009df9".hexToByteArray(), 0x67be0905),
        Arguments.of("96b2be67020101000000000000000000000042bd".hexToByteArray(), 0x67beb296),
        Arguments.of("1ad1c1670201000100000000000000000000e9c7".hexToByteArray(), 0x67c1d11a),
        Arguments.of("25d1c16701010207048b908212688000000082af".hexToByteArray(), 0x67c1d125),
        Arguments.of("26d1c1670201010200000000000000000000d71c".hexToByteArray(), 0x67c1d126),
        Arguments.of("52fdc16702010201000000000000000000005b7f".hexToByteArray(), 0x67c1fd52),
        Arguments.of("30b8c26702010100000000000000000000008655".hexToByteArray(), 0x67c2b830),
        Arguments.of("f341c4670201000100000000000000000000a2d1".hexToByteArray(), 0x67c441f3),
        Arguments.of("fc41c46701010207048b9082126880000000c6b6".hexToByteArray(), 0x67c441fc),
        Arguments.of("fd41c467020101020000000000000000000032bd".hexToByteArray(), 0x67c441fd),
        Arguments.of("4242c467010140000000000000000000000023fa".hexToByteArray(), 0x67c44242),
        Arguments.of("bda9c5670201020100000000000000000000238d".hexToByteArray(), 0x67c5a9bd),
        Arguments.of("98bbc567020101000000000000000000000094d7".hexToByteArray(), 0x67c5bb98),
        Arguments.of("0413c8670201000100000000000000000000b8e3".hexToByteArray(), 0x67c81304),
        Arguments.of("ae13c8670101400000000000000000000000bfdb".hexToByteArray(), 0x67c813ae),
        Arguments.of("d613c86701010207048b908212688000000076d2".hexToByteArray(), 0x67c813d6),
        Arguments.of("0114c8670201010000000000000000000000e320".hexToByteArray(), 0x67c81401),
        Arguments.of("0d14c8670201000100000000000000000000d94c".hexToByteArray(), 0x67c8140d),
        Arguments.of("1514c86701010207048b9082126880000000089b".hexToByteArray(), 0x67c81415),
        Arguments.of("2114c8670201010200000000000000000000e352".hexToByteArray(), 0x67c81421),
        Arguments.of("7114c86701014000000000000000000000000604".hexToByteArray(), 0x67c81471),
        Arguments.of("6a2bc8670201020100000000000000000000ae1c".hexToByteArray(), 0x67c82b6a),
        Arguments.of("342dc86702010100000000000000000000003ebf".hexToByteArray(), 0x67c82d34),
        Arguments.of("97b1ca670601080900010000000000000000fe42".hexToByteArray(), 0x67cab197),
        Arguments.of("3cbfca67020100010000000000000000000009ce".hexToByteArray(), 0x67cabf3c),
        Arguments.of("44bfca6701010207048b9082126880000000c607".hexToByteArray(), 0x67cabf44),
        Arguments.of("46bfca670201010200000000000000000000c248".hexToByteArray(), 0x67cabf46),
        Arguments.of("90bfca670101400000000000000000000000ee7f".hexToByteArray(), 0x67cabf90),
        Arguments.of("2aefca6702010201000000000000000000002854".hexToByteArray(), 0x67caef2a),
        Arguments.of("1df0ca670201010000000000000000000000aee0".hexToByteArray(), 0x67caf01d),
        Arguments.of("ec76cb6706010801000300000000000000008622".hexToByteArray(), 0x67cb76ec),
        Arguments.of("7a48d1670201000100000000000000000000406a".hexToByteArray(), 0x67d1487a),
        Arguments.of("9d48d16701010207048b90821268800000009199".hexToByteArray(), 0x67d1489d),
        Arguments.of("9f48d167020101020000000000000000000095d6".hexToByteArray(), 0x67d1489f),
        Arguments.of("b548d16701014000000000000000000000004981".hexToByteArray(), 0x67d148b5),
        Arguments.of("be79d1670201020100000000000000000000813f".hexToByteArray(), 0x67d179be),
        Arguments.of("c379d167020101000000000000000000000009b3".hexToByteArray(), 0x67d179c3),
        Arguments.of("a8b9d167040103060000000a0000000000007efe".hexToByteArray(), 0x67d1b9a8),
        Arguments.of("b1b9d1670401030a000000060000000000005af5".hexToByteArray(), 0x67d1b9b1),
        Arguments.of("bb9bd2670201000100000000000000000000c052".hexToByteArray(), 0x67d29bbb),
        Arguments.of("c19bd267010102070497db821268800000007360".hexToByteArray(), 0x67d29bc1),
        Arguments.of("c29bd2670201010200000000000000000000fb90".hexToByteArray(), 0x67d29bc2),
      )
    }
  }
}