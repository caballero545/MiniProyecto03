package com.example.miniproyecto03.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PublicacionDao {
    @Query("SELECT * FROM publicaciones ORDER BY id DESC")
    fun obtenerTodas(): Flow<List<Publicacion>>

    @Insert
    suspend fun insertar(publicacion: Publicacion)

    @Delete
    suspend fun eliminar(publicacion: Publicacion)
}