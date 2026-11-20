package com.example.electromechanics.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.electromechanics.data.local.dao.ActividadDao
import com.example.electromechanics.data.local.dao.EmpleadoDao
import com.example.electromechanics.data.local.dao.EvidenciaDao
import com.example.electromechanics.data.local.dao.RegistroJornadaDao
import com.example.electromechanics.data.local.dao.ServicioDao
import com.example.electromechanics.data.local.dao.SesionDao
import com.example.electromechanics.data.local.dao.SucursalDao
import com.example.electromechanics.data.local.entity.ActividadEntity
import com.example.electromechanics.data.local.entity.CuentaLocalEntity
import com.example.electromechanics.data.local.entity.EmpleadoEntity
import com.example.electromechanics.data.local.entity.EvidenciaEntity
import com.example.electromechanics.data.local.entity.RegistroJornadaEntity
import com.example.electromechanics.data.local.entity.ServicioEntity
import com.example.electromechanics.data.local.entity.SesionEntity
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
        OrdenTrabajoEntity::class,
        SesionEntity::class,
        CuentaLocalEntity::class
    ],
    version = 3,
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
    abstract fun sesionDao(): SesionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // v1 -> v2: solo agrega la tabla "sesion". No toca ni borra las tablas existentes.
        // El SQL debe coincidir con la entidad SesionEntity (nombres, tipos y NOT NULL).
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sesion` (
                        `id` INTEGER NOT NULL,
                        `idUsuario` TEXT NOT NULL,
                        `username` TEXT NOT NULL,
                        `rol` TEXT NOT NULL,
                        `token` TEXT NOT NULL,
                        `expiraEnMs` INTEGER NOT NULL,
                        `verificadorLocal` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS cuentas_locales (
                        username TEXT NOT NULL PRIMARY KEY,
                        idUsuario TEXT NOT NULL,
                        rol TEXT NOT NULL,
                        token TEXT NOT NULL,
                        expiraEnMs INTEGER NOT NULL,
                        verificadorLocal TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO cuentas_locales (username, idUsuario, rol, token, expiraEnMs, verificadorLocal)
                    SELECT username, idUsuario, rol, token, expiraEnMs, verificadorLocal FROM sesion
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "electromechanics_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
