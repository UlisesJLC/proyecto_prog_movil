/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ItemsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * ViewModel to retrieve, update and delete an item from the [ItemsRepository]'s data source.
 */
class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository,
) : ViewModel() {

    private val itemId: Int = checkNotNull(savedStateHandle[ItemDetailsDestination.itemIdArg])

    /**
     * Holds the item details ui state. The data is retrieved from [ItemsRepository] and mapped to
     * the UI state.
     */
    val uiState: StateFlow<ItemDetailsUiState> =
        itemsRepository.getItemStream(itemId)
            .filterNotNull()
            .map { item ->
                ItemDetailsUiState(
                    isCompleted = item.estado,
                    itemDetails = item.toItemDetails()
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ItemDetailsUiState()
            )

    // Funciones para extraer URIs de multimedia
    fun getPhotoUris(): List<String> {
        val fotoUri = uiState.value.itemDetails.fotoUri
        return if (!fotoUri.isNullOrEmpty()) {
            Gson().fromJson(fotoUri, object : TypeToken<List<String>>() {}.type)
        } else {
            emptyList()
        }
    }

    fun getVideoUris(): List<String> {
        val videoUri = uiState.value.itemDetails.videoUri
        return if (!videoUri.isNullOrEmpty()) {
            Gson().fromJson(videoUri, object : TypeToken<List<String>>() {}.type)
        } else {
            emptyList()
        }
    }

    fun getAudioUris(): List<String> {
        val audioUri = uiState.value.itemDetails.audioUri
        return if (!audioUri.isNullOrEmpty()) {
            Gson().fromJson(audioUri, object : TypeToken<List<String>>() {}.type)
        } else {
            emptyList()
        }
    }

    // Completar item
    fun completeItem() {
        viewModelScope.launch {
            val currentItem = uiState.value.itemDetails.toItem()
            if (!currentItem.estado) {
                itemsRepository.updateItem(currentItem.copy(estado = true))
            }
        }
    }

    // Eliminar item
    fun deleteItem() {
        viewModelScope.launch {
            itemsRepository.deleteItem(uiState.value.itemDetails.toItem())
        }
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * UI state for ItemDetailsScreen
 */
data class ItemDetailsUiState(
    val isTask: Boolean = false,
    val isCompleted: Boolean = false,
    val itemDetails: ItemDetails = ItemDetails()
)

