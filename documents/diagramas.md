# Arquitectura del Proyecto — Electromechanics Ingeniería SpA

## 1. Diagrama de Arquitectura del Proyecto

```mermaid
flowchart TB
%%{init: {"theme":"base","flowchart":{"curve":"basis","htmlLabels":true,"nodeSpacing":50,"rankSpacing":55,"padding":16},"themeVariables":{"fontFamily":"Inter, Segoe UI, Roboto, Arial, sans-serif","fontSize":"15px","background":"#ffffff","lineColor":"#64748b","textColor":"#1e293b","titleColor":"#0f172a","edgeLabelBackground":"#ffffff","clusterBkg":"#f8fafc","clusterBorder":"#cbd5e1"}}}%%
flowchart TB
    subgraph ANDROID["📱 <b>Aplicación Android</b> · Kotlin · una sola app, navegación según rol"]
        ROLES(["👥 <b>Roles</b><br/>Técnico · Supervisor"])
        VIEW["🖼️ <b>View</b><br/>Jetpack Compose<br/>+ Navigation Compose"]
        NATIVE["📷 <b>Recursos nativos</b><br/>Técnico: cámara y GPS<br/>Ambos roles: NotificationManager"]
        VM["⚙️ <b>ViewModel</b>"]
        REPO["📦 <b>Repository</b>"]
        ROOM["🗃️ <b>Room / DAO</b><br/>sesión y autenticación local"]
        SQLITE[("<b>SQLite local</b>")]
        RETROFIT["🌐 <b>Retrofit / API</b><br/>una base URL por microservicio"]
        ROLES --> VIEW
        VIEW --> VM
        VM --> REPO
        REPO --> ROOM
        ROOM --> SQLITE
        REPO --> RETROFIT
        VIEW -.->|"usa"| NATIVE
    end

    subgraph LOCAL["🐳 <b>Desarrollo local</b> · Docker Compose"]
        AUTH_L["<b>auth-service</b><br/>contenedor Docker"]
        OPS_L["<b>operations-service</b><br/>contenedor Docker"]
        PG_L[("<b>PostgreSQL</b><br/>contenedor Docker")]
    end

    subgraph PROD["☁️ <b>Producción</b>"]
        subgraph RENDER["Render · un contenedor Docker por microservicio"]
            AUTH_R["<b>auth-service</b>"]
            OPS_R["<b>operations-service</b>"]
        end
        subgraph SUPABASE["Supabase"]
            PG_S[("<b>PostgreSQL</b><br/>base de datos central")]
            BUCKET[("<b>Storage</b><br/>bucket de fotografías")]
        end
    end

    RETROFIT -.->|"solicitudes (desarrollo)"| AUTH_L
    RETROFIT -.->|"solicitudes (desarrollo)"| OPS_L
    RETROFIT ==>|"URL pública"| AUTH_R
    RETROFIT ==>|"URL pública"| OPS_R

    AUTH_L --> PG_L
    OPS_L --> PG_L
    OPS_L -.->|"bucket real de Supabase"| BUCKET

    AUTH_R --> PG_S
    OPS_R --> PG_S
    OPS_R ==>|"sube fotografías"| BUCKET

    %% Enlace invisible: mantiene la app compacta y deja los entornos debajo de ella
    SQLITE ~~~ RETROFIT

    %% ---- Estilos de nodos ----
    classDef role  fill:#F1F5F9,stroke:#64748B,stroke-width:1.5px,color:#0F172A;
    classDef app   fill:#E0F2FE,stroke:#0284C7,stroke-width:1.5px,color:#0C4A6E;
    classDef nat   fill:#FCE7F3,stroke:#DB2777,stroke-width:1.5px,color:#831843;
    classDef dev   fill:#FEF3C7,stroke:#D97706,stroke-width:1.5px,color:#78350F;
    classDef prod  fill:#DCFCE7,stroke:#16A34A,stroke-width:1.5px,color:#14532D;
    classDef db    fill:#FFEDD5,stroke:#EA580C,stroke-width:1.5px,color:#7C2D12;
    classDef store fill:#EDE9FE,stroke:#7C3AED,stroke-width:1.5px,color:#4C1D95;

    class ROLES role;
    class VIEW,VM,REPO,ROOM,RETROFIT app;
    class NATIVE nat;
    class AUTH_L,OPS_L dev;
    class AUTH_R,OPS_R prod;
    class SQLITE,PG_L,PG_S db;
    class BUCKET store;

    %% ---- Estilos de contenedores ----
    style ANDROID  fill:#F0F9FF,stroke:#0284C7,stroke-width:2px
    style LOCAL    fill:#FFFBEB,stroke:#D97706,stroke-width:2px,stroke-dasharray:6 4
    style PROD     fill:#F0FDF4,stroke:#16A34A,stroke-width:2px
    style RENDER   fill:#FFFFFF,stroke:#86EFAC
    style SUPABASE fill:#FFFFFF,stroke:#C4B5FD

    %% ---- Estilos de conexiones (por orden de definición) ----
    %% 0-5: flujo interno de la app | 6: recursos nativos
    %% 7,8,11,12,13: desarrollo (naranja) | 9,10,14,15,16: producción (verde)
    linkStyle 0,1,2,3,4,5 stroke:#0284C7,stroke-width:2px
    linkStyle 6 stroke:#DB2777,stroke-width:1.5px
    linkStyle 7,8,11,12,13 stroke:#D97706,stroke-width:2px
    linkStyle 9,10,14,15,16 stroke:#16A34A,stroke-width:3px

```

## 2. Diagrama de Arquitectura de Microservicios

```mermaid
flowchart LR
%%{init: {"theme":"base","flowchart":{"curve":"basis","htmlLabels":true,"nodeSpacing":35,"rankSpacing":80,"padding":14},"themeVariables":{"fontFamily":"Inter, Segoe UI, Roboto, Arial, sans-serif","fontSize":"15px","background":"#ffffff","lineColor":"#64748b","textColor":"#1e293b","titleColor":"#0f172a","edgeLabelBackground":"#ffffff","clusterBkg":"#f8fafc","clusterBorder":"#cbd5e1"}}}%%
flowchart LR
    CLIENT(["📱 <b>Android · Retrofit</b><br/>Técnico y Supervisor"])
    SECRET(["🔑 <b>Secreto HS256 compartido</b><br/>variable de entorno<br/>en ambos servicios"])

    subgraph AUTH["🔐 <b>auth-service</b> · dominio: identidad y acceso"]
        A_LOGIN["<b>Login</b><br/>valida credenciales con BCrypt<br/>emite JWT HS256, vigencia 8 h"]
        A_JWTV{{"<b>Validación JWT</b><br/>firma, expiración y autorización por rol"}}
        A_USERS["<b>Gestión de usuarios</b><br/>crear cuentas de Técnico y Supervisor<br/>consultar lista de técnicos"]
        A_JWTV --> A_USERS
    end

    subgraph OPS["⚙️ <b>operations-service</b> · dominio: operación de servicios"]
        O_JWT{{"<b>Validación JWT</b><br/>firma, expiración y autorización por rol"}}
        subgraph MODS["Módulos internos del mismo microservicio"]
            M_ORD["📋 <b>Órdenes</b><br/>crear, asignar por ID de técnico,<br/>consultar, estado, prioridad y urgencia"]
            M_ATT["📍 <b>Asistencia y GPS</b><br/>entrada y salida con coordenadas<br/>radio de 150 m respecto de la sucursal<br/>siempre guarda, genera alerta si está fuera"]
            M_EVD["📷 <b>Evidencias fotográficas</b><br/>actividades y fotografías"]
            M_APR["✅ <b>Aprobaciones</b><br/>aprobar o rechazar asistencia y horas"]
            M_NOT["🔔 <b>Notificaciones</b><br/>endpoint central de cambios"]
        end
        O_JWT -->|"crear orden, asignar, consultar"| M_ORD
        O_JWT -->|"registrar asistencia"| M_ATT
        O_JWT -->|"registrar actividad y foto"| M_EVD
        O_JWT -->|"aprobar o rechazar"| M_APR
        O_JWT -->|"consultar cambios cada 15 s"| M_NOT
    end

    subgraph PG["🐘 <b>PostgreSQL</b> · una instancia y base de datos compartida; tablas separadas por servicio"]
        T_AUTH[("<b>Tablas de auth-service</b><br/>usuarios y roles")]
        T_OPS[("<b>Tablas de operations-service</b><br/>órdenes y asignaciones<br/>asistencia y coordenadas<br/>sucursal (carga inicial por SQL)<br/>actividades, referencias y metadatos de fotos<br/>aprobaciones, alertas<br/><i>guarda solo el ID del técnico, no lee usuarios</i>")]
    end

    BUCKET[("🗂️ <b>Supabase Storage</b><br/>bucket de fotografías")]

    CLIENT -->|"login"| A_LOGIN
    CLIENT -->|"crear cuenta, listar técnicos<br/>con token JWT"| A_JWTV
    CLIENT -->|"todas las solicitudes<br/>con token JWT"| O_JWT

    SECRET -.-> A_LOGIN
    SECRET -.-> A_JWTV
    SECRET -.-> O_JWT

    A_LOGIN --> T_AUTH
    A_USERS --> T_AUTH
    MODS --> T_OPS

    M_EVD ==>|"sube foto y genera URL firmada"| BUCKET
    BUCKET -.->|"el Supervisor consulta con URL firmada"| CLIENT

    %% ---- Estilos de nodos ----
    classDef client fill:#CCFBF1,stroke:#0D9488,stroke-width:2px,color:#134E4A;
    classDef secret fill:#FEE2E2,stroke:#DC2626,stroke-width:2px,color:#7F1D1D;
    classDef auth   fill:#DBEAFE,stroke:#2563EB,stroke-width:1.5px,color:#1E3A8A;
    classDef ops    fill:#DCFCE7,stroke:#16A34A,stroke-width:1.5px,color:#14532D;
    classDef db     fill:#FFEDD5,stroke:#EA580C,stroke-width:1.5px,color:#7C2D12;
    classDef store  fill:#EDE9FE,stroke:#7C3AED,stroke-width:1.5px,color:#4C1D95;

    class CLIENT client;
    class SECRET secret;
    class A_LOGIN,A_JWTV,A_USERS auth;
    class O_JWT,M_ORD,M_ATT,M_EVD,M_APR,M_NOT ops;
    class T_AUTH,T_OPS db;
    class BUCKET store;

    %% ---- Estilos de contenedores ----
    style AUTH fill:#EFF6FF,stroke:#2563EB,stroke-width:2px
    style OPS  fill:#F0FDF4,stroke:#16A34A,stroke-width:2px
    style MODS fill:#FFFFFF,stroke:#86EFAC,stroke-dasharray:4 3
    style PG   fill:#FFF7ED,stroke:#EA580C,stroke-width:2px

    %% ---- Estilos de conexiones ----
    %% auth: 0,6,7,12,13 | ops: 1-5,8,14 | secreto: 9,10,11 | storage: 15,16
    linkStyle 0,6,7,12,13 stroke:#2563EB,stroke-width:2px
    linkStyle 1,2,3,4,5,8,14 stroke:#16A34A,stroke-width:2px
    linkStyle 9,10,11 stroke:#DC2626,stroke-width:1.5px
    linkStyle 15,16 stroke:#7C3AED,stroke-width:2.5px

```