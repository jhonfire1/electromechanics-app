# Electromechanics App

App de mantención industrial para el proyecto de Aplicaciones Móviles de Duoc UC. La implementación usa Kotlin, Jetpack Compose, MVVM, Retrofit y Room sobre SQLite.

**Integrantes:** [Freymuth](https://github.com/Freymuth) y [jhonfire1](https://github.com/jhonfire1).

## Demostración en el teléfono sin internet

La compilación `debug` de esta copia arranca en modo local. El supervisor puede asignar órdenes, el técnico puede consultarlas y cambiar su estado, y el cliente puede crear su cuenta. Room guarda las cuentas y los cambios en el teléfono. Después del primer ingreso, la sesión se conserva hasta que se cierra.

| Rol | RUT | Contraseña de prueba |
| --- | --- | --- |
| Supervisor | 11.111.111-1 | `DemoSupervisor26` |
| Técnico | 12.345.678-5 | `12345` |
| Técnico | 22.222.222-2 | `DemoTecnico22` |
| Técnico | 33.333.333-3 | `DemoTecnico33` |

El primer técnico tiene tres órdenes de muestra, una por estado. El segundo tiene una orden pendiente en Demo Subestación Oriente y el tercero parte con la hoja de ruta vacía. Para generar un APK desde el proyecto, usa `gradlew.bat :app:assembleDebug`. La variante `debug` también admite el backend con `-PdemoLocal=false`.

## Demostración con el backend

1. Levanta los dos servicios del backend y carga las cuentas ficticias siguiendo su README.
2. Para usar el APK firmado de demostración, abre un emulador o conecta un teléfono con depuración USB. Con un solo dispositivo conectado, ejecuta:

   ```powershell
   adb reverse tcp:8081 tcp:8081
   adb reverse tcp:8082 tcp:8082
   adb install -r .\Electromechanics-demo-firmado.apk
   ```

   Si `adb` no está disponible en la consola, ejecútalo desde la carpeta `platform-tools` de tu SDK de Android. El APK `demo` apunta al equipo a través de esos puertos reenviados. Para usar `debug` en el emulador con Docker, desactiva el modo local y establece `authBaseUrl` y `operationsBaseUrl` con `10.0.2.2` en los puertos 8081 y 8082.

3. Ingresa como supervisor con `11.111.111-1` y `DemoSupervisor26`. Elige el técnico, completa cliente, dirección, descripción y fecha `AAAA-MM-DD`. Asigna la orden y usa **Mostrar notificación** para probar el recurso nativo del supervisor.
4. Cierra sesión e ingresa como técnico con `12.345.678-5` y `12345`. Abre la orden, cámbiala a **En proceso** y luego a **Completada**. La hoja de ruta tiene el botón **Actualizar** para consultar cambios.
5. En técnico puedes registrar jornada con GPS y adjuntar evidencia con la cámara. Jornada, actividades y fotografías se guardan en el dispositivo. Para probar la linterna, usa un equipo con flash.

## Sesión y datos

El primer ingreso de cada cuenta valida las credenciales en Spring Boot. Room guarda la sesión activa y un verificador de contraseña por cuenta autorizada. Cerrar sesión limpia la sesión activa. Si el servidor no responde, ambos roles pueden volver a entrar con sus credenciales locales mientras el JWT siga vigente. Una respuesta de credenciales inválidas del servidor se respeta. Las órdenes ya consultadas se pueden leer desde Room durante una desconexión. Asignar órdenes o actualizar su estado requiere comunicación con el backend.

Los diagramas de MVVM y microservicios están en [documents](documents/diagramas.md). Para una futura compilación `release` con servicios HTTPS, se pueden configurar `-PauthBaseUrl=https://.../` y `-PoperationsBaseUrl=https://.../`. La variante `demo` conserva las direcciones locales. La clave de firma se administra fuera del repositorio.
