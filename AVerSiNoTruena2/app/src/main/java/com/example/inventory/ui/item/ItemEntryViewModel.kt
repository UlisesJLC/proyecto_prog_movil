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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import java.text.NumberFormat

/**
 * ViewModel to validate and insert items in the Room database.
 */
class ItemEntryViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {

    /**
     * Mantiene el estado actual del item en la UI.
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    /**
     * Actualiza el [itemUiState] con el valor proporcionado en el argumento. Este método también activa
     * una validación para los valores de entrada.
     */
    fun updateUiState(itemDetails: ItemDetails) {
        itemUiState = ItemUiState(
            itemDetails = itemDetails,
            isEntryValid = validateInput(itemDetails)
        )
    }

    /**
     * Inserta un [Item] en la base de datos Room.
     */
    suspend fun saveItem() {
        if (validateInput()) {
            itemsRepository.insertItem(itemUiState.itemDetails.toItem())
        }
    }

    /**
     * Valida los campos obligatorios del item.
     */
    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        return with(uiState) {
            titulo.isNotBlank() && descripcion.isNotBlank() && clasificacion.isNotBlank()
        }
    }
}

/**
 * Representa el estado de la UI para un Item.
 */
data class ItemUiState(
    val itemDetails: ItemDetails = ItemDetails(),
    val isEntryValid: Boolean = false
)


/**
 * Extensión para convertir [Item] a [ItemUiState].
 */
fun Item.toItemUiState(isEntryValid: Boolean = false): ItemUiState = ItemUiState(
    itemDetails = this.toItemDetails(),
    isEntryValid = isEntryValid
)

/**
 * Extensión para convertir [Item] a [ItemDetails].
 */


data class ItemDetails(
    val id: Int = 0,
    val titulo: String = "",
    val descripcion: String = "",
    val clasificacion: String = "",
    val horaCumplimiento: Long? = null,
    val estado: Boolean = false,
    val videoUri: String? = null, // Added field
    val fotoUri: String? = null,  // Added field
    val audioUri: String? = null  // Added field
)

fun ItemDetails.toItem(): Item = Item(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    clasificacion = clasificacion,
    horaCumplimiento = horaCumplimiento,
    estado = estado,
    videoUri = videoUri,
    fotoUri = fotoUri,
    audioUri = audioUri
)

fun Item.toItemDetails(): ItemDetails = ItemDetails(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    clasificacion = clasificacion,
    horaCumplimiento = horaCumplimiento,
    estado = estado,
    videoUri = videoUri,
    fotoUri = fotoUri,
    audioUri = audioUri
)
