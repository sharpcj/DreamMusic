package com.sharpcj.dreammusic.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharpcj.dreammusic.core.network.NetworkResult
import com.sharpcj.dreammusic.core.network.datasource.RemoteMusicDataSource
import com.sharpcj.dreammusic.core.network.dto.BillboardType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val remoteMusicDataSource: RemoteMusicDataSource,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverUiState(isLoading = true))
    val uiState: StateFlow<DiscoverUiState> = _uiState

    init {
        refresh()
    }

    fun selectBillboard(type: BillboardType) {
        if (type == _uiState.value.selectedBillboardType) return
        _uiState.update {
            DiscoverUiState(
                selectedBillboardType = type,
                isLoading = true,
            )
        }
        loadBillboard(type)
    }

    fun refresh() {
        val type = _uiState.value.selectedBillboardType
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        loadBillboard(type)
    }

    private fun loadBillboard(type: BillboardType) {
        viewModelScope.launch {
            when (val result = remoteMusicDataSource.getBillboard(type = type, pageSize = 20)) {
                is NetworkResult.Success -> _uiState.update {
                    it.copy(
                        selectedBillboardType = result.data.type,
                        billboardSongs = result.data.songs,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
                is NetworkResult.Failure -> _uiState.update {
                    it.copy(
                        billboardSongs = emptyList(),
                        isLoading = false,
                        errorMessage = "${type.displayName}加载失败：${result.message}",
                    )
                }
            }
        }
    }
}
