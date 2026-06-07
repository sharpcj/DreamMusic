package com.sharpcj.dreammusic.core.network.datasource

import com.sharpcj.dreammusic.core.network.NetworkResult
import com.sharpcj.dreammusic.core.network.dto.Billboard
import com.sharpcj.dreammusic.core.network.dto.BillboardType

interface RemoteMusicDataSource {
    suspend fun getBillboard(
        type: BillboardType,
        pageSize: Int = 20,
        offset: Int = 0,
    ): NetworkResult<Billboard>
}
