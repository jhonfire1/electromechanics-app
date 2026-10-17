package com.example.electromechanics.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.electromechanics.data.local.dao.ActividadDao
import com.example.electromechanics.data.local.dao.EmpleadoDao
import com.example.electromechanics.data.local.dao.EvidenciaDao
import com.example.electromechanics.data.local.dao.RegistroJornadaDao
import com.example.electromechanics.data.local.dao.ServicioDao
import com.example.electromechanics.data.local.dao.SucursalDao
import com.example.electromechanics.data.local.entity.ActividadEntity
import com.example.electromechanics.data.local.entity.EmpleadoEntity
import com.example.electromechanics.data.local.entity.EvidenciaEntity
import com.example.electromechanics.data.local.entity.RegistroJornadaEntity
import com.example.electromechanics.data.local.entity.ServicioEntity
import com.example.electromechanics.data.local.entity.SucursalEntity
import com.example.electromechanics.data.local.entity.OrdenTrabajoEntity
import com.example.electromechanics.data.local.dao.OrdenTrabajoDao

@Database(
    entities = [
        EmpleadoEntity::class,
        SucursalEntity::class,
        ServicioEntity::class,
        ActividadEntity::class,
        RegistroJornadaEntity::class,
        EvidenciaEntity::class ,
        OrdenTrabajoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun empleadoDao(): EmpleadoDao
    abstract fun sucursalDao(): SucursalDao
    abstract fun servicioDao(): ServicioDao
    abstract fun actividadDao(): ActividadDao
    abstract fun registroJornadaDao(): RegistroJornadaDao
    abstract fun evidenciaDao(): EvidenciaDao
    abstract fun ordenTrabajoDao(): OrdenTrabajoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "electromechanics_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}