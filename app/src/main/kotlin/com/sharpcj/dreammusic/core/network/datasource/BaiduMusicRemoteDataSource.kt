package com.sharpcj.dreammusic.core.network.datasource

import com.sharpcj.dreammusic.core.network.NetworkResult
import com.sharpcj.dreammusic.core.network.dto.BaiduBillboardResponse
import com.sharpcj.dreammusic.core.network.dto.Billboard
import com.sharpcj.dreammusic.core.network.dto.BillboardType
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Singleton
class BaiduMusicRemoteDataSource @Inject constructor(
    private val httpClient: HttpClient,
    private val json: Json,
) : RemoteMusicDataSource {
    override suspend fun getBillboard(
        type: BillboardType,
        pageSize: Int,
        offset: Int,
    ): NetworkResult<Billboard> = try {
        val responseText = httpClient.get(BAIDU_TING_ENDPOINT) {
            parameter("method", "baidu.ting.billboard.billList")
            parameter("format", "json")
            parameter("type", type.code)
            parameter("size", pageSize)
            parameter("offset", offset)
        }.bodyAsText()

        val response = json.decodeFromString<BaiduBillboardResponse>(responseText)
        val songs = response.songList.mapIndexedNotNull { index, song ->
            song.toBillboardSong(index)
        }

        NetworkResult.Success(Billboard(type = type, songs = songs))
    } catch (exception: ResponseException) {
        NetworkResult.Failure(
            message = "HTTP ${exception.response.status.value} ${exception.response.status.description}",
            cause = exception,
        )
    } catch (exception: SerializationException) {
        NetworkResult.Failure(
            message = "榜单数据解析失败",
            cause = exception,
        )
    } catch (throwable: Throwable) {
        NetworkResult.Failure(
            message = throwable.message?.takeIf { it.isNotBlank() } ?: "榜单接口请求失败",
            cause = throwable,
        )
    }

    private companion object {
        // 旧项目使用的百度 Ting API 只提供 HTTP；Manifest 已为该遗留接口打开 cleartext。
        const val BAIDU_TING_ENDPOINT = "http://tingapi.ting.baidu.com/v1/restserver/ting"
    }
}
