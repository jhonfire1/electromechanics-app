package com.example.electromechanics.data.local

import com.example.electromechanics.data.local.entity.EmpleadoEntity
import com.example.electromechanics.data.local.entity.OrdenTrabajoEntity
import com.example.electromechanics.data.local.entity.ServicioEntity
import com.example.electromechanics.data.local.entity.SucursalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseSeeder {

    suspend fun insertInitialData(db: AppDatabase) {
        withContext(Dispatchers.IO) {
            // Verificar si ya existen empleados cargados
            if (db.empleadoDao().obtenerPorCorreo("tecnico@electromechanics.cl") == null) {

                // 1. Insertar técnico de prueba
                val tecnico = EmpleadoEntity(
                    idEmpleado = "emp-001",
                    rut = "123456785",
                    nombreCompleto = "Juan Pérez",
                    rol = "TECNICO",
                    especialidadTecnica = "Electromecánica Industrial",
                    correoCorporativo = "tecnico@electromechanics.cl",
                    passwordHash = "123456"
                )
                db.empleadoDao().insertarEmpleado(tecnico)

                // 2. Insertar sucursal de prueba
                val sucursal = SucursalEntity(
                    idSucursal = "suc-001",
                    nombreCliente = "Planta Industrial San Bernardo",
                    direccionSucursal = "Av. Industrial 1234",
                    latitudRef = -33.5922,
                    longitudRef = -70.7044
                )
                db.sucursalDao().insertarSucursal(sucursal)

                // 3. Insertar servicio inicial de prueba
                val servicio = ServicioEntity(
                    idServicio = "srv-101",
                    idEmpleadoAsignado = "emp-001",
                    idSucursal = "suc-001",
                    fechaProgramada = "2026-10-15",
                    descripcionTrabajo = "Mantención preventiva de motor trifásico de 50 HP",
                    estado = "PENDIENTE"
                )
                db.servicioDao().insertarServicio(servicio)

                // 4. Insertar Órdenes de Trabajo para la Hoja de Ruta
                val ordenesPrueba = listOf(
                    OrdenTrabajoEntity(
                        idOrden = "1001",
                        idEmpleadoAsignado = "emp-001",
                        clienteNombre = "Planta Papelera Bío Bío",
                        direccion = "Av. Industriales 450, Concepción",
                        descripcionProblema = "Mantenimiento preventivo de motor trifásico y cambio de rodamientos.",
                        estado = "PENDIENTE",
                        fechaProgramada = "2026-10-20 09:00"
                    ),
                    OrdenTrabajoEntity(
                        idOrden = "1002",
                        idEmpleadoAsignado = "emp-001",
                        clienteNombre = "Textiles del Sur S.A.",
                        direccion = "Calle San Martín 1200, Talcahuano",
                        descripcionProblema = "Falla de variador de frecuencia en línea de producción 2.",
                        estado = "EN_PROCESO",
                        fechaProgramada = "2026-10-18 14:30"
                    ),
                    OrdenTrabajoEntity(
                        idOrden = "1003",
                        idEmpleadoAsignado = "emp-001",
                        clienteNombre = "Molinos San Pedro",
                        direccion = "Ruta 160 Km 12, Coronel",
                        descripcionProblema = "Inspección de tablero eléctrico principal e instalación de protecciones.",
                        estado = "COMPLETADA",
                        fechaProgramada = "2026-10-15 11:00"
                    )
                )
                db.ordenTrabajoDao().insertAll(ordenesPrueba)
            }
        }
    }
}