# Modelo de datos — AgroTrack

Diagrama entidad-relación del esquema actual de la base de datos (MySQL).
Renderiza automáticamente en GitHub / VS Code (Mermaid).

```mermaid
erDiagram
    APP_USER ||--o{ PARCELA : tiene
    APP_USER ||--o{ CONVERSACION : abre
    PARCELA ||--o{ CULTIVO_PARCELA : contiene
    CONVERSACION ||--o{ MENSAJE_CHAT : agrupa
    CATEGORIA ||--o{ PRODUCTO : clasifica
    PRODUCTO ||--o{ CULTIVO_PARCELA : se_cultiva_en
    PRODUCTO ||--o{ PRECIO_MAYORISTA : tiene_precio
    MERCADO_MAYORISTA ||--o{ PRECIO_MAYORISTA : cotiza

    APP_USER {
        bigint id PK
        varchar email UK
        varchar nombre
        varchar apellidos
        varchar password_hash
        longtext foto "base64, opcional"
        enum rol "ADMIN | AGRICULTOR"
        datetime fecha_alta
    }

    CATEGORIA {
        bigint id PK
        varchar nombre UK "FRUTAS | HORTALIZAS"
    }

    PRODUCTO {
        bigint id PK
        varchar nombre UK
        text descripcion
        varchar imagen_url
        int temporada_inicio
        int temporada_fin
        bigint categoria_id FK
    }

    MERCADO_MAYORISTA {
        bigint id PK
        varchar nombre UK
        varchar ciudad
    }

    PRECIO_MAYORISTA {
        bigint id PK
        date fecha
        decimal precio_kg
        bigint producto_id FK
        bigint mercado_id FK
    }

    PARCELA {
        bigint id PK
        varchar nombre
        decimal superficie_m2
        text descripcion
        longtext imagen "base64, opcional"
        datetime fecha_creacion
        bigint usuario_id FK
    }

    CULTIVO_PARCELA {
        bigint id PK
        date fecha_siembra
        enum estado "SEMBRADO | CRECIENDO | COSECHADO | RETIRADO"
        text notas
        bigint parcela_id FK
        bigint producto_id FK
    }

    CONVERSACION {
        bigint id PK
        varchar titulo
        datetime fecha_creacion
        datetime fecha_actualizacion
        bigint usuario_id FK
    }

    MENSAJE_CHAT {
        bigint id PK
        enum rol "USER | ASSISTANT"
        text contenido
        datetime fecha
        bigint conversacion_id FK
    }

    SCRAPING_LOG {
        bigint id PK
        enum estado "EXITOSO | FALLIDO | PARCIAL"
        datetime fecha_ejecucion
        int duracion_ms
        int filas_insertadas
        text mensaje
    }
```

> `SCRAPING_LOG` no tiene relaciones: es una tabla de auditoría independiente de cada
> ejecución del scraper de Mercasa.

## Notas del esquema

- **Una parcela sin cultivos se considera "en barbecho"** (estado deducido, no almacenado).
- `app_user.foto` y `parcela.imagen` guardan imágenes como **data URL base64** (`LONGTEXT`).
- `conversacion` + `mensaje_chat` dan **persistencia a los chats** del asistente: se
  reconstruye el contexto (últimos 10 mensajes) desde la BD en cada petición.
- Al borrar una `parcela` se eliminan sus `cultivo_parcela`; al borrar una
  `conversacion` se eliminan sus `mensaje_chat`.
