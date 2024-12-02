package com.example.inventory.ui.item

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.ItemsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ItemEditViewModel(
    savedStateHandle: SavedStateHandle?,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    // Estado actual del ítem en la UI
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    // URIs temporales de multimedia
    var tempPhotoUris by mutableStateOf(listOf<String>())
        private set
    var tempVideoUris by mutableStateOf(listOf<String>())
        private set
    var tempAudioUris by mutableStateOf(listOf<String>())
        private set

    // ID del ítem, solo relevante para edición
    private val itemId: Int? = savedStateHandle?.get(ItemEditDestination.itemIdArg)

    init {
        if (itemId != null) {
            // Cargar datos de un ítem existente para edición
            viewModelScope.launch {
                val item = itemsRepository.getItemStream(itemId)
                    .filterNotNull()
                    .first()

                itemUiState = item.toItemUiState(true)

                tempPhotoUris = Gson().fromJson(
                    item.fotoUri ?: "[]",
                    object : TypeToken<List<String>>() {}.type
                )
                tempVideoUris = Gson().fromJson(
                    item.videoUri ?: "[]",
                    object : TypeToken<List<String>>() {}.type
                )
                tempAudioUris = Gson().fromJson(
                    item.audioUri ?: "[]",
                    object : TypeToken<List<String>>() {}.type
                )
            }
        }
    }

    /**
     * Guarda o actualiza un ítem en el repositorio.
     */
    suspend fun saveOrUpdateItem() {
        try {
            if (validateInput(itemUiState.itemDetails)) {
                val updatedItem = itemUiState.itemDetails.copy(
                    fotoUri = Gson().toJson(tempPhotoUris),
                    videoUri = Gson().toJson(tempVideoUris),
                    audioUri = Gson().toJson(tempAudioUris)
                )

                Log.d("ItemEditViewModel", "Updated Item: $updatedItem")

                if (itemId == null) {
                    itemsRepository.insertItem(updatedItem.toItem())
                } else {
                    itemsRepository.updateItem(updatedItem.toItem())
                }
            }
        } catch (e: Exception) {
            Log.e("ItemEditViewModel", "Error saving item: ${e.message}", e)
        }
    }


    /**
     * Actualiza el estado de la UI.
     */
    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState = ItemUiState(
            itemDetails = itemDetails,
            isEntryValid = validateInput(itemDetails)
        )
    }

    /**
     * Validación de entrada de datos.
     */
    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            titulo.isNotBlank() && descripcion.isNotBlank()
        }
    }

    // Métodos para manejar multimedia temporal
    fun addTempImageUri(uri: String) {
        tempPhotoUris = tempPhotoUris + uri
    }

    fun addTempVideoUri(uri: String) {
        tempVideoUris = tempVideoUris + uri
    }

    fun addTempAudioUri(uri: String) {
        tempAudioUris = tempAudioUris + uri
    }

    fun removeTempImageUri(uri: String) {
        tempPhotoUris = tempPhotoUris - uri
    }

    fun removeTempVideoUri(uri: String) {
        tempVideoUris = tempVideoUris - uri
    }

    fun removeTempAudioUri(uri: String) {
        tempAudioUris = tempAudioUris - uri
    }


}
