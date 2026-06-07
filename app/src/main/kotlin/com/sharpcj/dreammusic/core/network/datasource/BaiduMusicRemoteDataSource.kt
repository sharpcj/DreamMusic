package com.sharpcj.dreammusic.core.network.datasource

import com.sharpcj.dreammusic.core.network.NetworkResult
import com.sharpcj.dreammusic.core.network.dto.Billboard
import com.sharpcj.dreammusic.core.network.dto.BillboardSong
import com.sharpcj.dreammusic.core.network.dto.BillboardType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Singleton
class BaiduMusicRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient,
) : RemoteMusicDataSource {
    override suspend fun getBillboard(
        type: BillboardType,
        pageSize: Int,
        offset: Int,
    ): NetworkResult<Billboard> = try {
        val body = httpClient.get(BAIDU_TING_ENDPOINT) {
            parameter("method", "baidu.ting.billboard.billList")
            parameter("format", "json")
            parameter("callback", "")
            parameter("type", type.code)
            parameter("size", pageSize)
            parameter("offset", offset)
        }.body<String>()

        val songs = parseBillboardSongs(body)
        NetworkResult.Success(Billboard(type = type, songs = songs))
    } catch (throwable: Throwable) {
        NetworkResult.Failure(
            message = throwable.message?.takeIf { it.isNotBlank() } ?: "榜单接口请求失败",
            cause = throwable,
        )
    }

    private fun parseBillboardSongs(body: String): List<BillboardSong> {
        val root = Json.parseToJsonElement(body).jsonObject
        val songList = root["song_list"] as? JsonArray ?: return emptyList()
        return songList.mapIndexedNotNull { index, item ->
            val song = item as? JsonObject ?: return@mapIndexedNotNull null
            val title = song.stringValue("title").ifBlank { return@mapIndexedNotNull null }
            BillboardSong(
                id = song.stringValue("song_id").ifBlank { "remote-${index + 1}-$title" },
                title = title,
                artist = song.stringValue("author").ifBlank { "未知歌手" },
                album = song.stringValue("album_title").ifBlank { "未知专辑" },
                rank = song.intValue("rank") ?: index + 1,
            )
        }
    }

    private fun JsonObject.stringValue(key: String): String =
        this[key]?.jsonPrimitive?.contentOrNull.orEmpty()

    private fun JsonObject.intValue(key: String): Int? =
        this[key]?.jsonPrimitive?.intOrNull

    private companion object {
        // 旧项目使用的百度 Ting API 只提供 HTTP；Manifest 已为该遗留接口打开 cleartext。
        const val BAIDU_TING_ENDPOINT = "http://tingapi.ting.baidu.com/v1/restserver/ting"
    }
}
