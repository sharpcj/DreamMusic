package com.sharpcj.dreammusic.core.network.dto

import com.sharpcj.dreammusic.core.network.DreamMusicJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class BaiduBillboardResponseSerializationTest {
    @Test
    fun decodeBillboardResponse_mapsBaiduFieldsToBillboardSong() {
        val json = """
            {
              "song_list": [
                {
                  "song_id": "1001",
                  "title": "晴天",
                  "author": "周杰伦",
                  "album_title": "叶惠美",
                  "rank": 3,
                  "unknown_field": "ignored"
                }
              ]
            }
        """.trimIndent()

        val response = DreamMusicJson.decodeFromString<BaiduBillboardResponse>(json)
        val song = response.songList.firstOrNull()?.toBillboardSong(index = 0)

        assertNotNull(song)
        assertEquals("1001", song?.id)
        assertEquals("晴天", song?.title)
        assertEquals("周杰伦", song?.artist)
        assertEquals("叶惠美", song?.album)
        assertEquals(3, song?.rank)
    }

    @Test
    fun decodeBillboardResponse_usesFallbacksForBlankOptionalFields() {
        val json = """
            {
              "song_list": [
                {
                  "song_id": "",
                  "title": "  歌曲名  ",
                  "author": "",
                  "album_title": ""
                }
              ]
            }
        """.trimIndent()

        val response = DreamMusicJson.decodeFromString<BaiduBillboardResponse>(json)
        val song = response.songList.firstOrNull()?.toBillboardSong(index = 4)

        assertNotNull(song)
        assertEquals("remote-5-歌曲名", song?.id)
        assertEquals("歌曲名", song?.title)
        assertEquals("未知歌手", song?.artist)
        assertEquals("未知专辑", song?.album)
        assertEquals(5, song?.rank)
    }
}
