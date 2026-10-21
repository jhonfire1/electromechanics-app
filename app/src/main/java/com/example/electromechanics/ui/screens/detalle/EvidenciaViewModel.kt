package com.example.electromechanics.ui.screens.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electromechanics.data.local.entity.EvidenciaEntity
import com.example.electromechanics.data.repository.EvidenciaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EvidenciaViewModel(
    private val repository: EvidenciaRepository,
    private val idOrden: String
) : ViewModel() {

    // Lista de fotos de la orden, siempre actualizada desde Room
    val evidencias: StateFlow<List<EvidenciaEntity>> = repository.observarPorOrden(idOrden)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // tipo: "INICIAL" o "FINAL"
    fun guardarEvidencia(rutaFoto: String, tipo: String) {
        viewModelScope.launch {
            val ahora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            repository.registrar(
                EvidenciaEntity(
                    idEvidencia = UUID.randomUUID().toString(),
                    idServicio = idOrden,
                    tipo = tipo,
                    fotoPath = rutaFoto,
                    fechaHora = ahora
                )
            )
        }
    }
}
