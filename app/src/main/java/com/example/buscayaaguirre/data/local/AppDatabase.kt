package com.example.buscayaaguirre.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos Room de la aplicación.
 * Implementa el patrón Singleton para asegurar una única instancia activa.
 */
@Database(entities = [FarmaciaEntity::class, NegocioEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun farmaciaDao(): FarmaciaDao
    abstract fun negocioDao(): NegocioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "serviya_database"
                )
                    // ADVERTENCIA: fallbackToDestructiveMigration() destruye y recrea las tablas
                    // si cambia la versión de la base de datos. Es ÚNICAMENTE para desarrollo.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
