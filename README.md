# Electromechanics App

App Android (MVP) para **Electromechanics Ingeniería SpA**, empresa de mantención industrial.
Reemplaza el registro por WhatsApp, papel y llamadas: el técnico inicia sesión, ve su hoja de ruta,
cambia el estado de cada orden de trabajo y registra las actividades realizadas.

Proyecto de la asignatura **DSY1105** (Duoc UC) — EP2 (encargo) y EP3 (defensa).

## Integrantes
 GitHub: Freymuth
 GitHub: jhonfire1

## Funcionalidades
- **Login** con RUT (validación módulo 11) y contraseña, autenticado contra Room.
- **Hoja de ruta**: lista de órdenes del técnico logueado, con filtros por estado.
- **Detalle de orden**: información, cambio de estado (Pendiente → En proceso → Completada) e historial de actividades.
- **Formulario de actividad validado**: ícono y mensaje de error por campo, con la validación en `domain/util`.
- **Persistencia local** con Room (SQLite), sin conexión a red.
- **Marcar jornada con GPS**: entrada/salida con hora y ubicación (Google Play Services Location), guardadas en Room.
- **Evidencia fotográfica**: foto inicial/final de cada orden con la cámara (FileProvider), guardada en Room y mostrada con borde redondeado.
- **Recursos nativos**: GPS, cámara, batería, linterna y vibración (con manejo de permisos en tiempo de ejecución).
- **Animaciones**: transiciones deslizantes entre pantallas, botón de jornada con cambio de color animado, confirmación animada al marcar y temblor al guardar una actividad.

## Arquitectura (MVVM)
```
app/
 ├─ data/
 │   ├─ local/        entity/ (tablas Room), dao/ (consultas), AppDatabase, DatabaseSeeder
 │   ├─ repository/   OrdenTrabajoRepository (aísla Room de la lógica)
 │   └─ session/      SessionManager (empleado logueado)
 ├─ domain/util/      LoginValidator, ActividadValidator (validaciones fuera de la UI)
 ├─ ui/
 │   ├─ theme/        Material Design 3 (colores, tipografía)
 │   ├─ navigation/   NavGraph, Screen
 │   ├─ components/   CampoTextoValidado, TarjetaOrdenTrabajo, TarjetaBateria, TarjetaLinterna
 │   └─ screens/      login, hojaruta, detalle (cada una con Screen + ViewModel + UiState)
 └─ util/             Vibracion
```
- **Composable**: solo dibuja y lee el estado.
- **ViewModel**: lógica de la pantalla, expone `StateFlow`.
- **Repository**: único punto de acceso a los datos (hoy Room).
- **Entity / DAO**: modelo de datos y consultas.

## Cómo ejecutar
1. Abrir la carpeta del proyecto en Android Studio y esperar el *Gradle Sync*.
2. Ejecutar en un emulador o dispositivo Android (minSdk 23).
3. Iniciar sesión con el técnico de prueba (datos ficticios):
   - RUT: `12.345.678-5`
   - Contraseña: `123456`

> La linterna requiere un dispositivo con flash (el emulador no lo tiene).
> Si cambias las entidades de Room, desinstala la app o borra sus datos antes de volver a ejecutar.
