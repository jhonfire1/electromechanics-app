# Electromechanics App

App de mantención industrial para el proyecto de Aplicaciones Móviles de Duoc UC. La implementación usa Kotlin, Jetpack Compose, MVVM, Retrofit y Room sobre SQLite.

**Integrantes:** [Freymuth](https://github.com/Freymuth) y [jhonfire1](https://github.com/jhonfire1).

## Ejecutar la demostración

1. Levanta los dos servicios del backend y carga las cuentas ficticias siguiendo su README.
2. Para usar el APK firmado de demostración, abre un emulador o conecta un teléfono con depuración USB. Con un solo dispositivo conectado, ejecuta:

   ```powershell
   adb reverse tcp:8081 tcp:8081
   adb reverse tcp:8082 tcp:8082
   adb install -r .\Electromechanics-demo-firmado.apk
   ```

   Si `adb` no está disponible en la consola, ejecútalo desde la carpeta `platform-tools` de tu SDK de Android. El APK `demo` apunta al equipo a través de esos puertos reenviados. La variante `debug` funciona con el emulador a través de `10.0.2.2` y los mismos puertos, sin `adb reverse`.

3. Ingresa como supervisor con `11.111.111-1` y `DemoSupervisor26`. Elige el técnico, completa cliente, dirección, descripción y fecha `AAAA-MM-DD`. Asigna la orden y usa **Mostrar notificación** para probar el recurso nativo del supervisor.
4. Cierra sesión e ingresa como técnico con `12.345.678-5` y `DemoTecnico26`. Abre la orden, cámbiala a **En proceso** y luego a **Completada**. La hoja de ruta tiene el botón **Actualizar** para consultar cambios.
5. En técnico puedes registrar jornada con GPS y adjuntar evidencia con la cámara. Jornada, actividades y fotografías se guardan en el dispositivo. Para probar la linterna, usa un equipo con flash.

## Sesión y datos

El primer ingreso de cada cuenta valida las credenciales en Spring Boot. Room guarda la sesión activa y un verificador de contraseña por cuenta autorizada. Cerrar sesión limpia la sesión activa. Si el servidor no responde, ambos roles pueden volver a entrar con sus credenciales locales mientras el JWT siga vigente. Una respuesta de credenciales inválidas del servidor se respeta. Las órdenes ya consultadas se pueden leer desde Room durante una desconexión. Asignar órdenes o actualizar su estado requiere comunicación con el backend.

Los diagramas de MVVM y microservicios están en [documents](documents/diagramas.md). Para una futura compilación `release` con servicios HTTPS, se pueden configurar `-PauthBaseUrl=https://.../` y `-PoperationsBaseUrl=https://.../`. La variante `demo` conserva las direcciones locales. La clave de firma se administra fuera del repositorio.
