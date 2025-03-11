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
class CCDTRecordParserTest {

  @ParameterizedTest
  @MethodSource("usedCombinationsProvider")
  fun parse_VerifyResultsFromKnownTestVectors(
    ccdtRecordArray: ByteArray,
    expectedTimeStamp: Long
  ) {
    val ccdtRecord = CCDTRecordParser.parse(ccdtRecordArray)
    assertNotNull(ccdtRecord)
    assertEquals(expectedTimeStamp.toUInt(), ccdtRecord!!.timestamp)
    val expectedTimeStampDate = Date(expectedTimeStamp * 1000)
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss:SS")
    val times = sdf.format(expectedTimeStampDate)
    val formatted = String.format(
      "%s 0x%08d 0x%04x 0x%02x 0x%02x 0x%02x 0x%02x",
      times,
      ccdtRecord.eventEnergy.toLong(),
      ccdtRecord.quarterEnergy.toInt(),
//      ccdtRecord.eventType.name,
      ccdtRecordArray[10],
      ccdtRecord.L1.toInt(),
      ccdtRecord.L2.toInt(),
      ccdtRecord.L3.toInt()
    )
    println(formatted)
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parse_CCDTRecordLengthToShort() {
    assertNull(CCDTRecordParser.parse("1234567890ABCDEF1234567890ABCD".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parse_CCDTRecordLengthToLong() {
    assertNull(CCDTRecordParser.parse("1234567890ABCDEF1234567890ABCDEF18".hexToByteArray()))
  }

  @OptIn(ExperimentalStdlibApi::class)
  @Suppress("SpellCheckingInspection")
  @Test
  fun parse_CCDTRecordIncorrectCRC16() {
    assertNull(CCDTRecordParser.parse("1234567890ABCDEF1234567890AB6969".hexToByteArray()))
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
        Arguments.of("a8e78f670f242e006d1d4a2000006e98".hexToByteArray(), 0x678fe7a8),
        Arguments.of("2ceb8f67592b2e005f1d4a2000005d6c".hexToByteArray(), 0x678feb2c),
        Arguments.of("b0ee8f67a6322e00781d4a2000005941".hexToByteArray(), 0x678feeb0),
        Arguments.of("34f28f67f4392e00801d4a200000369a".hexToByteArray(), 0x678ff234),
        Arguments.of("b8f58f673f412e00631d4a200000d019".hexToByteArray(), 0x678ff5b8),
        Arguments.of("3cf98f6787482e006f1d4a200000a930".hexToByteArray(), 0x678ff93c),
        Arguments.of("c0fc8f67d24f2e00661d4a2000006c31".hexToByteArray(), 0x678ffcc0),
        Arguments.of("4400906718572e00581d4a200000e7a1".hexToByteArray(), 0x67900044),
        Arguments.of("c8039067685e2e00871d4a200000143a".hexToByteArray(), 0x679003c8),
        Arguments.of("4c079067b2652e006a1d4a2000008aff".hexToByteArray(), 0x6790074c),
        Arguments.of("d00a9067006d2e00761d4a200000c66f".hexToByteArray(), 0x67900ad0),
        Arguments.of("d9119067a27b2e00801d4a200000bec3".hexToByteArray(), 0x679011d9),
        Arguments.of("5c159067f2822e00801d4a2000005917".hexToByteArray(), 0x6790155c),
        Arguments.of("e0189067458a2e00791d4a2000003242".hexToByteArray(), 0x679018e0),
        Arguments.of("b91a90671c8e2e00741d492000008cae".hexToByteArray(), 0x67901ab9),
        Arguments.of("641c90671c8e2e00000041000000bda9".hexToByteArray(), 0x67901c64),
        Arguments.of("e81f90671c8e2e000000410000003526".hexToByteArray(), 0x67901fe8),
        Arguments.of("6c2390671c8e2e00000041000000e19e".hexToByteArray(), 0x6790236c),
        Arguments.of("f02690671c8e2e000000410000007107".hexToByteArray(), 0x679026f0),
        Arguments.of("742a90671c8e2e00000041000000e44f".hexToByteArray(), 0x67902a74),
        Arguments.of("f82d90671c8e2e000000410000006244".hexToByteArray(), 0x67902df8),
        Arguments.of("7c3190671c8e2e00000041000000c99c".hexToByteArray(), 0x6790317c),
        Arguments.of("003590671c8e2e00000041000000baa4".hexToByteArray(), 0x67903500),
        Arguments.of("843890671c8e2e000000410000002d6d".hexToByteArray(), 0x67903884),
        Arguments.of("083c90671c8e2e00000041000000aea5".hexToByteArray(), 0x67903c08),
        Arguments.of("8c3f90671c8e2e000000410000002fe2".hexToByteArray(), 0x67903f8c),
        Arguments.of("104390671c8e2e000000410000001d82".hexToByteArray(), 0x67904310),
        Arguments.of("944690671c8e2e000000410000009503".hexToByteArray(), 0x67904694),
        Arguments.of("184a90671c8e2e000000410000000983".hexToByteArray(), 0x67904a18),
        Arguments.of("9c4d90671c8e2e000000410000008640".hexToByteArray(), 0x67904d9c),
        Arguments.of("205190671c8e2e000000410000001460".hexToByteArray(), 0x67905120),
        Arguments.of("a55490671c8e2e000000410000009c20".hexToByteArray(), 0x679054a5),
        Arguments.of("285890671c8e2e000000410000000061".hexToByteArray(), 0x67905828),
        Arguments.of("ad5b90671c8e2e0000004100000081e7".hexToByteArray(), 0x67905bad),
        Arguments.of("305f90671c8e2e00000041000000133e".hexToByteArray(), 0x67905f30),
        Arguments.of("b46290671c8e2e00000041000000c507".hexToByteArray(), 0x679062b4),
        Arguments.of("386690671c8e2e0000004100000046cf".hexToByteArray(), 0x67906638),
        Arguments.of("bc6990671c8e2e00000041000000d644".hexToByteArray(), 0x679069bc),
        Arguments.of("406d90671c8e2e00000041000000243c".hexToByteArray(), 0x67906d40),
        Arguments.of("c47090671c8e2e000000410000008d65".hexToByteArray(), 0x679070c4),
        Arguments.of("487490671c8e2e000000410000000ead".hexToByteArray(), 0x67907448),
        Arguments.of("cc7790671c8e2e000000410000008fea".hexToByteArray(), 0x679077cc),
        Arguments.of("507b90671c8e2e0000004100000002ba".hexToByteArray(), 0x67907b50),
        Arguments.of("d47e90671c8e2e000000410000008a3b".hexToByteArray(), 0x67907ed4),
        Arguments.of("588290671c8e2e00000041000000564a".hexToByteArray(), 0x67908258),
        Arguments.of("dc8590671c8e2e00000041000000d989".hexToByteArray(), 0x679085dc),
        Arguments.of("608990671c8e2e000000410000007539".hexToByteArray(), 0x67908960),
        Arguments.of("e48c90671c8e2e00000041000000fdb8".hexToByteArray(), 0x67908ce4),
        Arguments.of("689090671c8e2e000000410000005fa8".hexToByteArray(), 0x67909068),
        Arguments.of("ec9390671c8e2e00000041000000deef".hexToByteArray(), 0x679093ec),
        Arguments.of("689790671c8e2e000000600000005ed3".hexToByteArray(), 0x67909768),
        // From HCI-BLE log between NExxtmove and NExxtender Home of 14-3-2025
        Arguments.of("d43ac567714c31008e054a0600001c15".hexToByteArray(), 0x67c53ad4),
        Arguments.of("583ec567cf4d310088054a060000f4c9".hexToByteArray(), 0x67c53e58),
        Arguments.of("dc41c5672d4f310084054a0600006742".hexToByteArray(), 0x67c541dc),
        Arguments.of("6045c5678b50310080054a060000bf93".hexToByteArray(), 0x67c54560),
        Arguments.of("e448c567e85131008a054a060000d6d1".hexToByteArray(), 0x67c548e4),
        Arguments.of("684cc567465331008b054a0600002adf".hexToByteArray(), 0x67c54c68),
        Arguments.of("ec4fc567a55431008a054a0600007820".hexToByteArray(), 0x67c54fec),
        Arguments.of("7053c5670456310087054a06000084da".hexToByteArray(), 0x67c55370),
        Arguments.of("f456c5676357310087054a060000b3af".hexToByteArray(), 0x67c556f4),
        Arguments.of("785ac567c258310086054a0600003849".hexToByteArray(), 0x67c55a78),
        Arguments.of("fd5dc567255a310086064a0700000e67".hexToByteArray(), 0x67c55dfd),
        Arguments.of("8061c567ce5b310046084a0900004207".hexToByteArray(), 0x67c56180),
        Arguments.of("0465c567f75d31002c0a4a0b0000e27d".hexToByteArray(), 0x67c56504),
        Arguments.of("8868c567785f3100290a4a0b00005291".hexToByteArray(), 0x67c56888),
        Arguments.of("0c6cc5671e623100750c4a0d0000533c".hexToByteArray(), 0x67c56c0c),
        Arguments.of("906fc56796643100d70e4a1000009745".hexToByteArray(), 0x67c56f90),
        Arguments.of("1473c56791673100dc0d4a0f0000ed0a".hexToByteArray(), 0x67c57314),
        Arguments.of("9876c56799693100910d4a0e0000ffd4".hexToByteArray(), 0x67c57698),
        Arguments.of("1c7ac567746c3100e30e4a100000eeb4".hexToByteArray(), 0x67c57a1c),
        Arguments.of("a17dc5675c6e3100ec0e4a100000c151".hexToByteArray(), 0x67c57da1),
        Arguments.of("a17dc5675c6e3100ec0e4a100000c151".hexToByteArray(), 0x67c57da1),
        Arguments.of("a884c5679d733100bf0e4a10000058aa".hexToByteArray(), 0x67c584a8),
        Arguments.of("2c88c567c4763100ad0e4a10000022ae".hexToByteArray(), 0x67c5882c),
        Arguments.of("b08bc567577a3100ce0e4a1000001110".hexToByteArray(), 0x67c58bb0),
        Arguments.of("348fc567067e3100d40e4a100000f96e".hexToByteArray(), 0x67c58f34),
        Arguments.of("b892c56711813100e10e4a100000a9e1".hexToByteArray(), 0x67c592b8),
        Arguments.of("3c96c5679d843100cd0e4a100000dcca".hexToByteArray(), 0x67c5963c),
        Arguments.of("c099c567bd863100ee0e4a1000002b60".hexToByteArray(), 0x67c599c0),
        Arguments.of("449dc567648a3100dc0e4a10000024f4".hexToByteArray(), 0x67c59d44),
        Arguments.of("c8a0c567f38d3100e40e4a1000006e2a".hexToByteArray(), 0x67c5a0c8),
        Arguments.of("4ca4c567a5903100d20e4a10000097b2".hexToByteArray(), 0x67c5a44c),
        Arguments.of("d0a7c5678f933100040d4a0e00000ca7".hexToByteArray(), 0x67c5a7d0),
        Arguments.of("bda9c567e4943100eb0c490e000009f4".hexToByteArray(), 0x67c5a9bd),
        Arguments.of("54abc567e49431000000410000008226".hexToByteArray(), 0x67c5ab54),
        Arguments.of("d8aec567e4943100000041000000036f".hexToByteArray(), 0x67c5aed8),
        Arguments.of("5cb2c567e4943100000041000000a8b7".hexToByteArray(), 0x67c5b25c),
        Arguments.of("e0b5c567e49431000000410000001e8c".hexToByteArray(), 0x67c5b5e0),
        Arguments.of("65b9c567e49431000000410000008b05".hexToByteArray(), 0x67c5b965),
        Arguments.of("98bbc567e49431000000600000007a46".hexToByteArray(), 0x67c5bb98),
        Arguments.of("0313c867e4943100000021000000861b".hexToByteArray(), 0x67c81303),
        Arguments.of("0114c867e49431000000600000009aa2".hexToByteArray(), 0x67c81401),
        Arguments.of("0d14c867e49431000000210000008292".hexToByteArray(), 0x67c8140d),
        Arguments.of("2014c867e4943100000042000000b1fb".hexToByteArray(), 0x67c81420),
        Arguments.of("1416c867b39631008a1d4a200000d874".hexToByteArray(), 0x67c81614),
        Arguments.of("9819c867189a3100f90e4a100000ed9f".hexToByteArray(), 0x67c81998),
        Arguments.of("1c1dc8678d9c31000c0f4a1000007337".hexToByteArray(), 0x67c81d1c),
        Arguments.of("a020c867639f3100c2114a130000a589".hexToByteArray(), 0x67c820a0),
        Arguments.of("2424c86726a3310017134a150000b648".hexToByteArray(), 0x67c82424),
        Arguments.of("a827c867e2a63100d6114a130000cc6e".hexToByteArray(), 0x67c827a8),
        Arguments.of("2c2bc86795aa3100c0114a130000bd21".hexToByteArray(), 0x67c82b2c),
        Arguments.of("6a2bc867ceaa3100ee0d410f00009c6d".hexToByteArray(), 0x67c82b6a),
        Arguments.of("342dc867ceaa3100000060000000ca85".hexToByteArray(), 0x67c82d34),
        Arguments.of("3cbfca67ceaa3100000021000000e8a5".hexToByteArray(), 0x67cabf3c),
        Arguments.of("45bfca67ceaa31000000420000008e58".hexToByteArray(), 0x67cabf45),
        Arguments.of("1cc0ca67d2ab3100b11d4a200000444d".hexToByteArray(), 0x67cac01c),
        Arguments.of("a0c3ca6741af3100d70f4a110000d84a".hexToByteArray(), 0x67cac3a0),
        Arguments.of("24c7ca6723b2310018104a110000e153".hexToByteArray(), 0x67cac724),
        Arguments.of("a8caca67a5b43100b5104a1200005ab8".hexToByteArray(), 0x67cacaa8),
        Arguments.of("2cceca67beb73100c7114a130000d2ea".hexToByteArray(), 0x67cace2c),
        Arguments.of("b0d1ca672eba3100a0124a140000c7d2".hexToByteArray(), 0x67cad1b0),
        Arguments.of("34d5ca673dbc3100070f4a100000233c".hexToByteArray(), 0x67cad534),
        Arguments.of("b8d8ca67a7bd3100f0084a090000e7b0".hexToByteArray(), 0x67cad8b8),
        Arguments.of("3cdcca67a6c0310061114a13000021a3".hexToByteArray(), 0x67cadc3c),
        Arguments.of("c0dfca6759c43100e1114a130000c06f".hexToByteArray(), 0x67cadfc0),
        Arguments.of("45e3ca670bc83100d40e4a10000083fa".hexToByteArray(), 0x67cae345),
        Arguments.of("c8e6ca675bcb3100cc0e4a100000161b".hexToByteArray(), 0x67cae6c8),
        Arguments.of("4ceaca675cce3100fa0e4a100000084f".hexToByteArray(), 0x67caea4c),
        Arguments.of("d0edca679dd131000c0e4a0f0000620d".hexToByteArray(), 0x67caedd0),
        Arguments.of("2aefca67c8d23100160e490f00009a6a".hexToByteArray(), 0x67caef2a),
        Arguments.of("1df0ca67c8d23100000060000000d56a".hexToByteArray(), 0x67caf01d),
        Arguments.of("7a48d167c8d2310000002100000022ac".hexToByteArray(), 0x67d1487a),
        Arguments.of("9e48d167c8d23100000042000000d80c".hexToByteArray(), 0x67d1489e),
        Arguments.of("8c49d16733d33100af174a1a0000fed2".hexToByteArray(), 0x67d1498c),
        Arguments.of("104dd16773d63100fe0e4a100000610f".hexToByteArray(), 0x67d14d10),
        Arguments.of("9550d167c5d93100dd0f4a110000053c".hexToByteArray(), 0x67d15095),
        Arguments.of("1854d1672fdd310091114a130000974d".hexToByteArray(), 0x67d15418),
        Arguments.of("9c57d16715df310068114a1300003bdc".hexToByteArray(), 0x67d1579c),
        Arguments.of("205bd167ebe0310074124a1400001136".hexToByteArray(), 0x67d15b20),
        Arguments.of("a45ed16748e2310082054a060000b6ca".hexToByteArray(), 0x67d15ea4),
        Arguments.of("2862d167e0e33100b90e4a100000a1cc".hexToByteArray(), 0x67d16228),
        Arguments.of("ac65d1673fe531008b054a0600007121".hexToByteArray(), 0x67d165ac),
        Arguments.of("3069d1679fe631008c054a060000efb4".hexToByteArray(), 0x67d16930),
        Arguments.of("b46cd167ffe7310089054a060000694e".hexToByteArray(), 0x67d16cb4),
        Arguments.of("3870d1675fe931008b054a060000805e".hexToByteArray(), 0x67d17038),
        Arguments.of("bc73d167bfea31008b054a06000011bf".hexToByteArray(), 0x67d173bc),
        Arguments.of("4077d1671fec31008a054a060000cf34".hexToByteArray(), 0x67d17740),
        Arguments.of("be79d16719ed3100890549060000cb7c".hexToByteArray(), 0x67d179be),
        Arguments.of("c379d16719ed31000000600000008d05".hexToByteArray(), 0x67d179c3),
        Arguments.of("bb9bd26719ed31000000210000009c66".hexToByteArray(), 0x67d29bbb),
        Arguments.of("c29bd26719ed3100000042000000fa9b".hexToByteArray(), 0x67d29bc2),
        Arguments.of("909ed26777ef3100451a4a1c00004ad6".hexToByteArray(), 0x67d29e90),
        Arguments.of("15a2d2673bf23100f90e4a10000023e8".hexToByteArray(), 0x67d2a215),
        Arguments.of("98a5d26770f431008c124a14000066d5".hexToByteArray(), 0x67d2a598),
        Arguments.of("1ca9d267ecf531000d0f4a1000008b9d".hexToByteArray(), 0x67d2a91c),
        Arguments.of("a0acd26757f73100d00e4a1000007a3b".hexToByteArray(), 0x67d2aca0),
        Arguments.of("25b0d2672df931009b124a14000020f1".hexToByteArray(), 0x67d2b025),
        Arguments.of("6cb2d26799fb31001412491300009e22".hexToByteArray(), 0x67d2b26c),
        Arguments.of("a8b3d26799fb31000000410000001115".hexToByteArray(), 0x67d2b3a8),
        Arguments.of("c1b6d26799fb31000000600000007e45".hexToByteArray(), 0x67d2b6c1),
      )
    }
  }
}