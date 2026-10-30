package com.example.miniproyecto03.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproyecto03.data.AppDatabase
import com.example.miniproyecto03.data.Publicacion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PublicacionViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).publicacionDao()

    val publicaciones: StateFlow<List<Publicacion>> = dao.obtenerTodas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun agregarPublicacion(titulo: String, descripcion: String, fotoUri: String, fecha: String) {
        viewModelScope.launch {
            dao.insertar(Publicacion(titulo = titulo, descripcion = descripcion, fotoUri = fotoUri, fecha = fecha))
        }
    }

    fun eliminarPublicacion(publicacion: Publicacion) {
        viewModelScope.launch {
            dao.eliminar(publicacion)
        }
    }
}