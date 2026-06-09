# 🌱 AgroTrack — Seguimiento de Cultivos y Precios Mayoristas

> Proyecto individual — Desarrollo Rápido de Aplicaciones (DRA)
> Universidad de Almería · Curso 2025/2026
> Alumno: Juan José Fernández Requena (jfr498@inlumine.ual.es)

---

## 📌 Descripción

**AgroTrack** es una aplicación web para agricultores que centraliza el seguimiento de precios mayoristas y la gestión personal de cultivos. La plataforma cubre tres necesidades reales del sector:

1. **Precios mayoristas actualizados** — scraping automatizado de [mercasa.es](https://www.mercasa.es/precios-y-mercados-mayoristas/) dos veces por semana mediante `@Scheduled`. Solo se incluyen los productos de los que Mercasa publica precio real: **frutas y hortalizas**.
2. **Gestión del huerto personal** — el agricultor registra y edita sus parcelas (con imagen opcional), asocia cultivos del catálogo y hace seguimiento de cada uno; también gestiona su perfil (nombre, apellidos y foto). Todo desde la interfaz **o pidiéndoselo al asistente** en lenguaje natural.
3. **Asistente IA experto en tendencias con MCP** — agente que analiza el histórico real de precios de MySQL a través de un **MCP server independiente**. Recibe consejos sobre cuándo y dónde vender, comparativas entre mercados y recomendaciones de siembra según el terreno. El proveedor de LLM es **hibridable por configuración** (LM Studio local, Groq o Gemini en la nube) sin tocar código. Las conversaciones se **persisten** para poder recuperarlas y continuarlas, y el asistente es **público** (un invitado consulta precios; las funciones sobre parcelas requieren registro).

---

## 🛠️ Stack tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Frontend | Angular | 17+ |
| Backend | Spring Boot + Java | 3.5 / Java 21 |
| Base de datos | MySQL | 8.0 |
| Scraping | Jsoup | 1.18+ |
| IA — LLM (hibridable) | Spring AI + proveedor OpenAI-compatible: LM Studio (Qwen, local) **o** Groq **o** Gemini | spring-ai 1.1+ |
| IA — MCP Server | Spring AI MCP server (módulo aparte) | spring-ai 1.1+ |
| IA — MCP Client | Spring AI MCP client (en backend) | spring-ai 1.1+ |
| Automatización | Spring `@Scheduled` | — |
| Autenticación | Spring Security + JWT | — |
| Contenedores | Docker + Docker Compose | — |
| Patrones de diseño | Repository, Service Facade, Strategy, DTO | — |

---

## 🏗️ Estructura del proyecto

```
agrotrack/
├── pom.xml                                      # Parent multi-módulo (Maven, packaging=pom)
│
├── backend/                                     # API REST + ChatClient + cliente MCP
│   ├── src/main/java/es/ual/dra/agrotrack/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   └── AiConfig.java                    # ChatClient → LM Studio + MCP client
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── ProductoController.java
│   │   │   ├── PrecioController.java
│   │   │   ├── ParcelaController.java
│   │   │   ├── AsistenteController.java
│   │   │   ├── AdminController.java
│   │   │   └── mcp/                             # Endpoints específicos para el MCP
│   │   │       └── McpInternalController.java   # Operaciones que el frontend no expone
│   │   ├── dto/
│   │   ├── model/
│   │   │   ├── entity/                          # AppUser, Producto, PrecioMayorista, …
│   │   │   └── enums/                           # Rol, EstadoScraping, …
│   │   ├── repository/                          # 9 repositorios Spring Data JPA
│   │   ├── service/
│   │   │   ├── scraping/
│   │   │   │   ├── ScrapingService.java         # Jsoup → Mercasa
│   │   │   │   └── ScrapingScheduler.java       # @Scheduled lunes y jueves 07:00
│   │   │   └── AsistenteService.java            # ChatClient (proveedor hibridable) + MCP client
│   │   ├── init/
│   │   │   └── DataInitializer.java             # Pobla BD al arrancar
│   │   └── security/
│   ├── Dockerfile
│   └── pom.xml
│
├── mcp-server/                                  # MCP server independiente (cliente REST puro)
│   ├── src/main/java/es/ual/dra/agrotrack/mcp/
│   │   ├── AgrotrackMcpApplication.java
│   │   ├── config/
│   │   │   └── McpServerConfig.java             # Registro de tools
│   │   ├── client/
│   │   │   └── BackendClient.java               # RestClient → http://backend:8080
│   │   ├── dto/                                 # POJOs propios (espejo del API del backend)
│   │   └── tools/
│   │       └── AgroTools.java                   # @Tool que delegan al BackendClient
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/                                    # Angular SPA
│   ├── src/app/
│   │   ├── auth/
│   │   ├── components/
│   │   │   ├── shared/         # Header, Footer
│   │   │   ├── home/           # Precios del día
│   │   │   ├── catalogo/       # Catálogo por categoría
│   │   │   ├── precios/        # Gráficas históricas + análisis IA
│   │   │   ├── asistente/      # Chat con el agente IA
│   │   │   ├── mi-parcela/     # Gestión de parcelas y cultivos
│   │   │   ├── perfil/         # Perfil del usuario (datos y foto)
│   │   │   └── admin/          # Panel administración
│   │   ├── services/
│   │   └── models/
│   ├── nginx.conf
│   └── Dockerfile
│
└── docker-compose.yml
```

> **Nota arquitectónica:** backend y mcp-server son módulos Maven hermanos pero **no comparten código compilado**. El mcp-server se relaciona con el backend exclusivamente a través de su API REST (con un token de servicio). Esto evita el shared-database antipattern y mantiene el backend como única fuente de verdad sobre el dominio.

---

## 🗄️ Modelo de datos

### Entidades

| Entidad | Descripción |
|---|---|
| `AppUser` | Usuario con rol AGRICULTOR o ADMIN. Incluye nombre, apellidos y foto (base64) |
| `Categoria` | `FRUTAS` o `HORTALIZAS` — únicos con precio en Mercasa |
| `Producto` | Ficha del cultivo pre-cargada por el `DataInitializer` |
| `MercadoMayorista` | Mercamadrid, Mercabarna, Mercabilbao, Mercavalencia, Mercasevilla |
| `PrecioMayorista` | Precio €/kg scrapeado por mercado, producto y fecha |
| `Parcela` | Parcela personal del agricultor, con imagen opcional (base64) |
| `CultivoParcela` | Asocia una parcela con un producto del catálogo (estado: SEMBRADO, CRECIENDO, COSECHADO, RETIRADO). Una parcela sin cultivos se considera *en barbecho* |
| `Conversacion` | Chat persistido del asistente, por usuario, para recuperarlo y continuarlo |
| `MensajeChat` | Mensaje (USER / ASSISTANT) dentro de una conversación |
| `ScrapingLog` | Auditoría de cada ejecución del scraper |

### Relaciones

```
AppUser          1 ── N  Parcela
AppUser          1 ── N  Conversacion
Parcela          1 ── N  CultivoParcela
Conversacion     1 ── N  MensajeChat
CultivoParcela   N ── 1  Producto        ← agricultor elige del catálogo existente
Producto         N ── 1  Categoria
Producto         1 ── N  PrecioMayorista
MercadoMayorista 1 ── N  PrecioMayorista
ScrapingLog      (sin FK — registro independiente de auditoría)
```

> El diagrama ER completo (Mermaid) está en [`docs/diagrams/`](docs/diagrams/).

### Catálogo inicial (DataInitializer)

Al arrancar por primera vez, `DataInitializer` puebla la BD con los productos exactos que publica Mercasa. El scraper mapea por nombre contra estos registros para asociar cada precio al producto correcto.

**FRUTAS** (38 productos): aguacate, albaricoques, caqui, castañas, cerezas, chirimoyas, ciruelas, fresones, higos, kiwi, limones, mandarina clementina, mandarinas, mango, manzana golden, manzana roja, manzana starking, melocotones, melón piel de sapo, naranja navel, naranja navelina, nectarinas, nísperos, papaya, pera blanquilla, pera ercolini, piñas, plátanos, pomelos, sandías, uva blanca, uva italia, uva moscatel...

**HORTALIZAS** (27 productos): ajo, alcachofas, berenjenas, brócoli, calabacines, calabaza, cebollas, coliflor, endivia, escarola, espárragos, judías verdes, lechugas, lombarda, patatas, pepinos, pimientos verdes, puerro, repollo, tomate maduro, tomate verde, zanahorias...

**MERCADOS** (5): Mercamadrid, Mercabarna, Mercabilbao, Mercavalencia, Mercasevilla

---

## 🤖 Asistente IA — Experto en tendencias de mercado vía MCP

El asistente combina **Spring AI** con un **MCP server independiente** que expone las herramientas de acceso a datos. El backend actúa como **cliente MCP** y descubre las tools al arrancar — las mismas tools son consumibles también por cualquier otro cliente MCP (LM Studio chat UI, Claude Desktop, Cursor…) sin código adicional.

### Proveedor de LLM hibridable (sin tocar código)

Todos los proveedores hablan el **protocolo OpenAI**, así que cambiar de uno a otro es solo editar tres variables en `.env` (ver [`.env.example`](.env.example)) y reiniciar el backend:

| Proveedor | Dónde corre | Cuándo usarlo |
|---|---|---|
| **LM Studio** (Qwen) | Local, en el host | Privacidad total / offline; más lento según hardware |
| **Groq** (`openai/gpt-oss-20b`) | Nube (free tier) | Rápido y fiable en tool-calling; ideal para demo |
| **Gemini** (`gemini-2.5-flash`) | Nube (free tier) | Buena redacción; free tier muy limitado en peticiones |

`docker-compose.yml` trae LM Studio como valor por defecto; el `.env` lo sobreescribe. No hace falta recompilar.

### Conversaciones persistentes

Las conversaciones de los usuarios registrados se **guardan en BD** (`Conversacion` + `MensajeChat`): se pueden listar, abrir, continuar y borrar desde una barra lateral. El **contexto** que se envía al modelo (últimos 10 mensajes) se reconstruye desde la BD en cada petición, de modo que la conversación sobrevive a reinicios del backend. Los invitados tienen un chat efímero (sin persistencia; el contexto lo aporta el frontend).

### Resolución determinista de nombres

Las tools que necesitan un producto o una parcela reciben su **nombre** (tal como lo dice el usuario), no un id. Un resolver del mcp-server convierte el nombre al id real contra los datos del backend, tolerando singular/plural y avisando si hay ambigüedad ("¿tomate maduro o tomate verde?"). Así el modelo nunca puede "inventar" un id y registrar el cultivo equivocado.

### Flujo completo

```
Usuario en Angular: "¿Cuándo debo vender mi tomate?"
        ↓ HTTP REST
POST /api/asistente/consulta  →  backend (Spring Boot)
        ↓
   ChatClient (Spring AI)
        ├─► LM Studio (host.docker.internal:1234)  ◄── Qwen 2.5 razona
        │
        └─► MCP Client (descubre tools al arrancar)
                ↓ JSON-RPC sobre HTTP
        mcp-server (contenedor aparte, puerto 8081)
                ↓ HTTP REST (con service token)
            backend (Spring Boot)
                ↓ JPA
            MySQL  ──► getHistorialPrecios("tomate", 60)
                       getMiCultivos(usuarioId)
                       getProductosTemporada()
                       registrarCultivo(parcelaId, productoId, fecha)
        ↓
Qwen sintetiza respuesta con datos reales
        ↓
Backend devuelve JSON a Angular → UI muestra al agricultor
```

### La dicotomía: el mcp-server es **servidor MCP** Y **cliente REST** del backend

Es una de esas piezas raras del sistema que tiene dos caras según con quién esté hablando, y conviene aclararlo porque al leer el diagrama parece un loop conceptual.

**Cara A — como servidor MCP** (lo que ve el LLM):

El mcp-server publica un **catálogo de tools** vía el protocolo MCP (JSON-RPC sobre HTTP/SSE). Cualquier cliente MCP puede conectarse, descubrir qué tools hay y pedir su ejecución. Estos clientes son:

- El propio backend, como parte del flujo del asistente en Angular.
- LM Studio chat UI directamente (sin pasar por Angular).
- Claude Desktop, Cursor o cualquier otra app MCP-aware.

En esta cara, el mcp-server **responde**: "tengo estas 7 tools, dime cuál ejecutar y con qué parámetros".

**Cara B — como cliente REST del backend** (lo que hace cuando ejecuta una tool):

Cuando llega una invocación MCP del tipo `getHistorialPrecios(productoId=123)`, el mcp-server **no toca la BD**. Hace una llamada HTTP normal al backend (`GET /api/precios/123`) con un service token, recoge la respuesta y se la devuelve al cliente MCP.

En esta cara, el mcp-server **pide**: "backend, dame los datos del producto 123 que tu API ya sabe calcular".

**¿Por qué no es un loop?**

Porque MCP y REST son **dos protocolos distintos sirviendo dos propósitos distintos**, aunque ambos viajen sobre HTTP:

| | MCP (backend → mcp-server) | REST (mcp-server → backend) |
|---|---|---|
| Quién inicia | El cliente MCP (el LLM, vía ChatClient o vía chat de LM Studio) | El mcp-server, al ejecutar la tool |
| Qué pide | "Descúbreme y ejecuta tools" | "Dame estos datos o ejecuta esta operación de dominio" |
| Quién decide qué llamar | El LLM, al razonar sobre la pregunta del usuario | El código fijo de la tool |
| Capa de abstracción | "Capacidades disponibles para IA" | "Operaciones de negocio del sistema" |

Visto de otra forma: **el mcp-server es un *BFF para LLMs*** (Backend For Frontend, donde el frontend es un modelo de lenguaje). Traduce entre lo que el LLM sabe pedir (MCP + lenguaje natural en los nombres de las tools) y lo que el backend sabe ofrecer (REST con su modelo de dominio). Añade por el camino formato, agregaciones específicas de IA y aislamiento del modelo respecto al esquema interno del backend.

### Por qué esta arquitectura

- **Una única fuente de verdad del dominio**: toda la lógica de negocio (validaciones, autorizaciones, reglas) vive en el backend. El mcp-server **nunca** reimplementa reglas — siempre delega vía API.
- **mcp-server desacoplado**: no comparte clases Java con el backend; podría reescribirse en Python o TypeScript sin tocar el backend. Solo necesita la URL del backend + un token de servicio.
- **Interoperabilidad real del estándar MCP**: cualquier cliente MCP (LM Studio chat UI, Claude Desktop, Cursor…) consume las mismas tools sin código adicional. El backend mismo es cliente MCP de su propio mcp-server.
- **Patrón profesional canónico**: los MCP servers oficiales de GitHub, Linear, Slack, Notion y Stripe siguen exactamente este patrón (MCP server como cliente de la API existente, no como acceso directo a la BD).
- **Privacidad por diseño**: ninguna consulta del agricultor sale de la infraestructura local; LM Studio corre en la máquina del usuario.

### Endpoints específicos para MCP

Cuando una tool necesita un cálculo que el frontend NO usa (medias móviles, tendencias, ventanas variables…), el backend expone un endpoint dedicado bajo `/api/mcp/...` que **solo el mcp-server consume**. Esto evita ensuciar el API público con operaciones internas, y al mismo tiempo mantiene la regla de "todo pasa por la API del backend".

### Coste asumido respecto a Ollama containerizado

LM Studio es una app de escritorio y **no se distribuye como contenedor oficial**. El evaluador necesita:
1. Instalar LM Studio en su máquina.
2. Cargar el modelo `qwen2.5-7b-instruct` desde la UI.
3. Activar el server local (`Serve on Local Network` en puerto 1234).
4. Solo entonces `docker-compose up` levanta el stack.

Se acepta esta fricción a cambio de demostrar el patrón MCP real (no simulado).

### Tools expuestas por `mcp-server`

Cada tool delega a un endpoint REST del backend. El mcp-server inyecta un `BackendClient` (RestClient de Spring) y no toca JPA ni MySQL.

```java
// mcp-server/src/main/java/es/ual/dra/agrotrack/mcp/tools/AgroTools.java

// LECTURA — endpoint público existente
@Tool("Historial de precios de un producto los últimos 90 días")
List<PrecioData> getHistorialPrecios(Long productoId) {
    return backendClient.getHistorial(productoId);   // → GET /api/precios/{productoId}
}

// LECTURA — endpoint específico para MCP (cálculo que la UI no necesita)
@Tool("Productos con tendencia bajista en los últimos 14 días")
List<TendenciaData> getProductosTendenciaBajista() {
    return backendClient.getTendenciaBajista(14);    // → GET /api/mcp/tendencias/bajista?dias=14
}

// ESCRITURA — endpoint REST del backend, valida todas las reglas de negocio
@Tool("Registra un cultivo nuevo en una parcela del usuario")
CultivoData registrarCultivo(Long parcelaId, Long productoId, LocalDate fecha) {
    return backendClient.crearCultivo(parcelaId,
        new CrearCultivoPayload(productoId, fecha)); // → POST /api/parcelas/{parcelaId}/cultivos
}
```

Estas tools se publican por el protocolo MCP en `http://mcp-server:8081/mcp` (dentro de la red Docker) y en `http://localhost:8081/mcp` (desde el host, para clientes MCP de escritorio).

---

## ⏱️ Flujo de automatización (@Scheduled)

```java
// ScrapingScheduler.java
@Scheduled(cron = "0 0 7 * * MON,THU")
public void ejecutarScrapingYNotificar() {
    // 1. Jsoup parsea mercasa.es → extrae precios de frutas y hortalizas
    // 2. Mapea nombre → Producto en BD → guarda PrecioMayorista
    // 3. Registra en ScrapingLog (EXITOSO / FALLIDO)
}
```

El ADMIN puede disparar el scraping manualmente desde el panel sin esperar al horario automático.

---

## 🐳 Servicios Docker

```yaml
services:
  mysql:        # Puerto 3306   — Base de datos
  backend:      # Puerto 8080   — API REST + Spring AI ChatClient + MCP Client + @Scheduled
  mcp-server:   # Puerto 8081   — Spring AI MCP Server (tools sobre MySQL)
  frontend:     # Puerto 80     — Angular (Nginx)
```

Adicionalmente, **fuera del stack Docker**, en la máquina del usuario:

```
LM Studio (app de escritorio):
  Puerto 1234 — Qwen 2.5 servido vía API OpenAI-compatible
  Accesible desde el backend en host.docker.internal:1234
```

---

## 🔐 Autenticación y roles

| Rol | Permisos |
|---|---|
| **AGRICULTOR** | Ver catálogo y precios, gestionar sus parcelas y cultivos, editar su perfil, usar el asistente IA con historial |
| **ADMIN** | Todo lo anterior + CRUD catálogo, ver ScrapingLog, disparar scraping manual |

Flujo: registro → login → JWT → Angular interceptor adjunta `Authorization: Bearer <token>` en cada request protegido.

---

## 🌐 API REST

### Auth
| Método | Endpoint | Descripción | Rol |
|---|---|---|---|
| POST | `/api/auth/register` | Registro de usuario | Público |
| POST | `/api/auth/login` | Login, devuelve JWT | Público |

### Catálogo
| Método | Endpoint | Descripción | Rol |
|---|---|---|---|
| GET | `/api/categorias` | Lista de categorías con sus productos | Público |
| GET | `/api/productos` | Catálogo completo (filtrable por categoría) | Público |
| GET | `/api/productos/{id}` | Detalle de producto + últimos precios | Público |
| POST | `/api/productos` | Crear producto en el catálogo | ADMIN |
| PUT | `/api/productos/{id}` | Editar ficha de un producto | ADMIN |
| DELETE | `/api/productos/{id}` | Eliminar producto del catálogo | ADMIN |

### Precios
| Método | Endpoint | Descripción | Rol |
|---|---|---|---|
| GET | `/api/precios` | Últimos precios de todos los productos (con mercado embebido) | Público |
| GET | `/api/precios/{productoId}` | Historial de precios de un producto para la gráfica | Público |
| POST | `/api/admin/precios/actualizar` | Dispara scraping manualmente | ADMIN |

### Parcelas y cultivos
| Método | Endpoint | Descripción | Rol |
|---|---|---|---|
| GET | `/api/parcelas` | Lista parcelas del usuario autenticado | AGRICULTOR |
| POST | `/api/parcelas` | Crear nueva parcela (nombre, superficie, descripción, imagen) | AGRICULTOR |
| PUT | `/api/parcelas/{id}` | Editar parcela (incluida la imagen) | AGRICULTOR |
| DELETE | `/api/parcelas/{id}` | Eliminar parcela y sus cultivos | AGRICULTOR |
| GET | `/api/cultivos` | Lista cultivos del usuario (filtrable por `parcelaId`) | AGRICULTOR |
| POST | `/api/cultivos` | Añadir cultivo (`{ parcelaId, productoId, fechaSiembra, estado, notas }`) | AGRICULTOR |
| DELETE | `/api/cultivos/{id}` | Eliminar cultivo de la parcela | AGRICULTOR |

### Perfil
| Método | Endpoint | Descripción | Rol |
|---|---|---|---|
| GET | `/api/perfil` | Datos del usuario autenticado | AGRICULTOR |
| PUT | `/api/perfil` | Editar nombre, apellidos y foto | AGRICULTOR |

### Conversaciones del asistente
| Método | Endpoint | Descripción | Rol |
|---|---|---|---|
| GET | `/api/conversaciones` | Lista los chats guardados del usuario | AGRICULTOR |
| GET | `/api/conversaciones/{id}` | Abre un chat con todos sus mensajes | AGRICULTOR |
| DELETE | `/api/conversaciones/{id}` | Elimina un chat | AGRICULTOR |

### Asistente IA
| Método | Endpoint | Descripción | Rol |
|---|---|---|---|
| POST | `/api/asistente/consulta` | Envía una pregunta al agente (con tool-calling y persistencia). **Público**: el invitado consulta precios/catálogo; las funciones sobre parcelas requieren estar autenticado | Público |

### Admin
| Método | Endpoint | Descripción | Rol |
|---|---|---|---|
| GET | `/api/admin/scraping-log` | Historial de ejecuciones del scraper | ADMIN |

---

## 🎨 Secciones Angular

| Ruta | Descripción | Acceso |
|---|---|---|
| `/` | Home — precios del día destacados | Público |
| `/catalogo` | Catálogo por categoría (frutas / hortalizas) | Público |
| `/catalogo/:id` | Detalle de producto + gráfica de precios histórica | Público |
| `/precios` | Comparador de precios entre los 5 mercados | Público |
| `/login` | Formulario de login | Público |
| `/register` | Formulario de registro | Público |
| `/asistente` | Chat con el agente IA (con historial de conversaciones para registrados) | Público |
| `/mis-parcelas` | Gestión de parcelas y cultivos (crear, editar, imagen, eliminar) | AGRICULTOR |
| `/perfil` | Datos del usuario y foto de perfil | AGRICULTOR |
| `/admin` | Panel: catálogo, scraping log, disparo manual | ADMIN |

---

## 🧩 Patrones de diseño aplicados

| Patrón | Aplicación concreta |
|---|---|
| **Repository** | Todos los repositorios Spring Data JPA |
| **Service Facade** | Servicios desacoplan controllers de repositorios y lógica |
| **Strategy** | `ScrapingStrategy` — interfaz intercambiable por fuente de datos |
| **Singleton** | Beans Spring gestionados por el contenedor IoC |
| **DTO** | Separación estricta entre entidades JPA y representación API |

---

## 🚀 Arranque local

### Elegir el proveedor de LLM (`.env`)

Copia `.env.example` a `.env` y elige UNO de los tres proveedores (ver tabla en la sección del asistente):

- **Nube (Groq o Gemini)** — la opción más sencilla: pega tu API key en `.env` y listo, sin instalar nada.
- **Local (LM Studio)** — para privacidad/offline:
  1. Instala [LM Studio](https://lmstudio.ai/) y descarga un modelo Qwen (p. ej. `qwen2.5-7b-instruct`).
  2. *Local Server* → cargar el modelo → activar *Serve on Local Network* en el puerto `1234`.

Si no defines `.env`, el stack usa LM Studio por defecto.

### Levantar el stack

```bash
docker-compose up --build

# Frontend:    http://localhost
# Backend:     http://localhost:8080
# MCP Server:  http://localhost:8081/mcp   (consumible por LM Studio, Claude Desktop, Cursor…)
# MySQL:       localhost:3306
```

---

## 📚 Tecnologías del temario aplicadas

| Tema DRA | Tecnología | Aplicación en el proyecto |
|---|---|---|
| Tema 2 | Docker + Docker Compose | 4 servicios orquestados (LM Studio se ejecuta en host) |
| Tema 3/5 | Angular | SPA completa con routing, guards, interceptores |
| Tema 4 | Spring Boot REST + JPA | API REST + persistencia MySQL |
| Prácticas CSS | Angular styles | Diseño visual de la app |
| Scraping | Jsoup | Extracción de precios de Mercasa |
| LLM (hibridable) | Spring AI + proveedor OpenAI-compatible | Asistente consumido vía `ChatClient`; proveedor elegible por `.env` (LM Studio local, Groq o Gemini) con conversaciones persistidas |
| MCP (Model Context Protocol) | Spring AI MCP Server + Client | Tools delegadas a la API REST del backend; consumibles por backend y por cualquier cliente MCP externo |
| Multi-módulo Maven | Parent POM + `backend/` + `mcp-server/` | Dos servicios Spring Boot hermanos sin código compartido — el mcp-server es cliente REST del backend |
| Automatización | Spring `@Scheduled` | Job de scraping de precios de Mercasa |
| Patrones GoF | Repository, Strategy, Facade, DTO | Aplicados en capa de servicio y datos |
