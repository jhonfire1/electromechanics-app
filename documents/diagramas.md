# Diagramas de arquitectura

Los diagramas muestran el flujo implementado para la Experiencia 3. Las acciones del supervisor y del técnico usan el mismo ID de usuario para relacionar la orden con su asignación.

## Aplicación Android y MVVM

```mermaid
flowchart LR
    Usuarios["Supervisor y técnico"] --> Login["Login y cierre de sesión"]
    Login --> LoginVM["LoginViewModel"]
    LoginVM --> AuthRepo["AuthRepository"]
    AuthRepo --> Sesion["Room / SQLite: sesión activa y cuentas locales"]
    AuthRepo --> AuthApi["Retrofit: autenticación y técnicos"]
    Login --> Navegacion["NavGraph: selecciona la ruta según el rol"]
    Navegacion --> Supervisor["Supervisor: asignar orden"]
    Navegacion --> Tecnico["Técnico: hoja de ruta, detalle y jornada"]
    Supervisor --> SupervisorVM["SupervisorViewModel"]
    SupervisorVM --> AuthRepo
    SupervisorVM --> OrdenRepo["OrdenTrabajoRepository"]
    Supervisor --> Notificacion["NotificationManager"]
    Tecnico --> TecnicoVM["ViewModels de hoja de ruta y detalle"]
    TecnicoVM --> OrdenRepo
    OrdenRepo --> OrdenApi["Retrofit: crear, listar y cambiar estado"]
    OrdenRepo --> Cache["Room: órdenes consultadas y actividades"]
    Tecnico --> Nativo["GPS, cámara, linterna y vibración"]
    Tecnico --> Local["Room y archivos: jornada y evidencia local"]
```

## Microservicios y PostgreSQL

```mermaid
flowchart LR
    App["Android / Retrofit"] -->|"Login y consulta de técnicos"| Auth["auth-service · Spring Boot"]
    App -->|"Crear, listar y cambiar estado con JWT"| Operaciones["operations-service · Spring Boot"]
    Operaciones -->|"Verifica que el ID corresponde a un técnico"| Auth
    Auth --> Usuarios["JPA: usuarios, contraseñas BCrypt y roles"]
    Operaciones --> Ordenes["JPA: órdenes, técnico asignado y estado"]
    Usuarios --> PostgreSQL[("PostgreSQL")]
    Ordenes --> PostgreSQL
    Auth -->|"Emite JWT HS256 al iniciar sesión"| App
```
