# 🌱 AgroTrack — Seguimiento de Cultivos y Precios Mayoristas

> Proyecto individual — Desarrollo Rápido de Aplicaciones (DRA)
> Universidad de Almería · Curso 2025/2026
> Alumno: Juan José Fernández Requena (jfr498@inlumine.ual.es)

---

## 📌 Descripción

**AgroTrack** es una aplicación web para agricultores que centraliza el seguimiento de precios mayoristas y la gestión personal de cultivos. La plataforma cubre tres necesidades reales del sector:

1. **Precios mayoristas actualizados** — scraping automatizado de [mercasa.es](https://www.mercasa.es/precios-y-mercados-mayoristas/) dos veces por semana mediante `@Scheduled`. Solo se incluyen los productos de los que Mercasa publica precio real: **frutas y hortalizas**.
2. **Gestión del huerto personal** — el agricultor registra sus parcelas, asocia cultivos del catálogo existente y hace seguimiento del estado de cada uno.
3. **Asistente IA experto en tendencias** — agente basado en Llama 3.2 (servido localmente por Ollama) que analiza el histórico real de precios de MySQL y asesora al agricultor sobre cuándo vender, cómo evoluciona el mercado y qué esperar la próxima semana. Todo el procesamiento ocurre en infraestructura propia, sin enviar datos a servicios externos.

---

## 🛠️ Stack tecnológico

| Capa | Tecnología | Versión |
|---|---|---|
| Frontend | Angular | 17+ |
| Backend | Spring Boot + Java | 3.5 / Java 21 |
| Base de datos | MySQL | 8.0 |
| Scraping | Jsoup | 1.18+ |
| IA — LLM | Spring AI + Llama 3.2 (Ollama) | spring-ai 1.1+ / llama3.2:3b |
| IA — Herramientas | Spring AI tool-calling (`@Tool`) | spring-ai 1.1+ |
| Automatización | Spring `@Scheduled` | — |
| Email | Spring Mail + Mailpit (dev) | — |
| Autenticación | Spring Security + JWT | — |
| Contenedores | Docker + Docker Compose | — |
| Patrones de diseño | Repository, Service Facade, Strategy, Observer | — |

---

## 🏗️ Estructura del proyecto

```
agrotrack/
├── backend/
│   ├── src/main/java/es/ual/dra/agrotrack/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── CorsConfig.java
│   │   │   ├── AiConfig.java
│   │   │   └── MailConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── ProductoController.java
│   │   │   ├── PrecioController.java
│   │   │   ├── ParcelaController.java
│   │   │   ├── AlertaController.java
│   │   │   ├── AsistenteController.java
│   │   │   └── AdminController.java
│   │   ├── dto/
│   │   ├── model/entity/
│   │   │   ├── AppUser.java
│   │   │   ├── Categoria.java          # FRUTAS | HORTALIZAS
│   │   │   ├── Producto.java
│   │   │   ├── MercadoMayorista.java
│   │   │   ├── PrecioMayorista.java
│   │   │   ├── Parcela.java
│   │   │   ├── CultivoParcela.java
│   │   │   ├── AlertaPrecio.java
│   │   │   └── ScrapingLog.java
│   │   ├── repository/
│   │   ├── service/
│   │   │   ├── scraping/
│   │   │   │   ├── ScrapingService.java    # Jsoup → Mercasa
│   │   │   │   └── ScrapingScheduler.java  # @Scheduled lunes y jueves 07:00
│   │   │   ├── ai/
│   │   │   │   ├── AsistenteService.java
│   │   │   │   └── mcp/
│   │   │   │       └── AgroTools.java      # MCP tools internas
│   │   │   ├── AlertaService.java
│   │   │   └── NotificacionService.java
│   │   ├── init/
│   │   │   └── DataInitializer.java        # Pobla BD al arrancar
│   │   └── security/
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/
│   ├── src/app/
│   │   ├── auth/
│   │   ├── components/
│   │   │   ├── shared/         # Header, Footer
│   │   │   ├── home/           # Precios del día
│   │   │   ├── catalogo/       # Catálogo por categoría
│   │   │   ├── precios/        # Gráficas históricas + análisis IA
│   │   │   ├── asistente/      # Chat con el agente IA
│   │   │   ├── mi-parcela/     # Gestión de parcelas y cultivos
│   │   │   ├── alertas/        # Alertas de precio
│   │   │   └── admin/          # Panel administración
│   │   ├── services/
│   │   └── models/
│   ├── nginx.conf
│   └── Dockerfile
│
├── docker-compose.yml
└── README.md
```

---

## 🗄️ Modelo de datos

### Entidades

| Entidad | Descripción |
|---|---|
| `AppUser` | Usuario con rol AGRICULTOR o ADMIN |
| `Categoria` | `FRUTAS` o `HORTALIZAS` — únicos con precio en Mercasa |
| `Producto` | Ficha del cultivo pre-cargada por el `DataInitializer` |
| `MercadoMayorista` | Mercamadrid, Mercabarna, Mercabilbao, Mercavalencia, Mercasevilla |
| `PrecioMayorista` | Precio €/kg scrapeado por mercado, producto y fecha |
| `Parcela` | Parcela personal del agricultor |
| `CultivoParcela` | Asocia una parcela con un producto del catálogo existente |
| `AlertaPrecio` | Umbral de precio personalizado por usuario y producto |
| `ScrapingLog` | Auditoría de cada ejecución del scraper |

### Relaciones

```
AppUser          1 ── N  Parcela
Parcela          1 ── N  CultivoParcela
CultivoParcela   N ── 1  Producto        ← agricultor elige del catálogo existente
Producto         N ── 1  Categoria
Producto         1 ── N  PrecioMayorista
MercadoMayorista 1 ── N  PrecioMayorista
AppUser          1 ── N  AlertaPrecio
AlertaPrecio     N ── 1  Producto
ScrapingLog      (sin FK — registro independiente de auditoría)
```

### Catálogo inicial (DataInitializer)

Al arrancar por primera vez, `DataInitializer` puebla la BD con los productos exactos que publica Mercasa. El scraper mapea por nombre contra estos registros para asociar cada precio al producto correcto.

**FRUTAS** (38 productos): aguacate, albaricoques, caqui, castañas, cerezas, chirimoyas, ciruelas, fresones, higos, kiwi, limones, mandarina clementina, mandarinas, mango, manzana golden, manzana roja, manzana starking, melocotones, melón piel de sapo, naranja navel, naranja navelina, nectarinas, nísperos, papaya, pera blanquilla, pera ercolini, piñas, plátanos, pomelos, sandías, uva blanca, uva italia, uva moscatel...

**HORTALIZAS** (27 productos): ajo, alcachofas, berenjenas, brócoli, calabacines, calabaza, cebollas, coliflor, endivia, escarola, espárragos, judías verdes, lechugas, lombarda, patatas, pepinos, pimientos verdes, puerro, repollo, tomate maduro, tomate verde, zanahorias...

**MERCADOS** (5): Mercamadrid, Mercabarna, Mercabilbao, Mercavalencia, Mercasevilla

---

## 🤖 Asistente IA — Experto en tendencias de mercado

El asistente combina **Spring AI + Llama 3.2** (servido por Ollama en un contenedor propio) con un conjunto de **herramientas internas anotadas con `@Tool`** que exponen datos reales de MySQL al LLM. Llama decide bajo demanda qué herramientas invocar para responder cada pregunta — el patrón es equivalente al de un MCP Server interno, sin dependencia de proveedores cloud.

```
Usuario: "¿Cuándo debo vender mi tomate?"
        ↓
POST /api/asistente/consulta
        ↓
Llama decide qué datos necesita e invoca las tools:
  ├── getHistorialPrecios("tomate", 60)   → precios reales MySQL
  ├── getMiCultivos(usuarioId)            → cultivos activos del agricultor
  └── getProductosTemporada()             → contexto estacional
        ↓
Responde como experto con datos reales, no genéricos
```

**Por qué Llama local en lugar de Gemini cloud:**
- Privacidad por diseño: ninguna consulta del agricultor sale de la infraestructura.
- Sin coste por token ni claves de API.
- El sistema funciona completamente offline tras la descarga inicial del modelo.
- Demuestra el desacoplamiento de la abstracción `ChatClient` de Spring AI: cambiar de proveedor (Gemini ↔ Llama ↔ OpenAI) es solo cambiar dependencia y configuración, sin tocar el código de negocio.

### Herramientas internas (`AgroTools.java`)

```java
@Tool("Historial de precios de un producto los últimos N días")
List<PrecioDTO> getHistorialPrecios(String producto, int dias)

@Tool("Precios actuales de un producto en todos los mercados")
List<PrecioDTO> getPreciosActuales(String producto)

@Tool("Comparativa de precio entre mercados mayoristas")
Map<String, Double> compararMercados(String producto)

@Tool("Cultivos activos del agricultor y estado de cada uno")
List<CultivoDTO> getMiCultivos(Long usuarioId)

@Tool("Productos de temporada óptima en este momento")
List<ProductoDTO> getProductosTemporada()
```

---

## ⏱️ Flujo de automatización (@Scheduled)

```java
// ScrapingScheduler.java
@Scheduled(cron = "0 0 7 * * MON,THU")
public void ejecutarScrapingYNotificar() {
    // 1. Jsoup parsea mercasa.es → extrae precios de frutas y hortalizas
    // 2. Mapea nombre → Producto en BD → guarda PrecioMayorista
    // 3. Registra en ScrapingLog (EXITOSO / FALLIDO)
    // 4. AlertaService evalúa umbrales activos contra nuevos precios
    // 5. NotificacionService envía email a usuarios con alertas disparadas
}
```

El ADMIN puede disparar el scraping manualmente desde el panel sin esperar al horario automático.

---

## 🐳 Servicios Docker

```yaml
services:
  mysql:      # Puerto 3306   — Base de datos
  ollama:     # Puerto 11434  — Servidor local de Llama 3.2
  backend:    # Puerto 8080   — API REST + Spring AI + tool-calling + @Scheduled
  frontend:   # Puerto 80     — Angular (Nginx)
  mailpit:    # Puerto 8025   — SMTP local para desarrollo
```

---

## 🔐 Autenticación y roles

| Rol | Permisos |
|---|---|
| **AGRICULTOR** | Ver catálogo y precios, gestionar sus parcelas, configurar alertas, usar asistente IA |
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
| POST | `/api/parcelas` | Crear nueva parcela | AGRICULTOR |
| PUT | `/api/parcelas/{id}` | Editar nombre, superficie o descripción | AGRICULTOR |
| DELETE | `/api/parcelas/{id}` | Eliminar parcela y sus cultivos | AGRICULTOR |
| POST | `/api/parcelas/{id}/cultivos` | Añadir cultivo a parcela (`{ productoId, fechaSiembra, notas }`) | AGRICULTOR |
| PUT | `/api/cultivos/{id}` | Actualizar estado o notas del cultivo | AGRICULTOR |
| DELETE | `/api/cultivos/{id}` | Eliminar cultivo de la parcela | AGRICULTOR |

### Alertas
| Método | Endpoint | Descripción | Rol |
|---|---|---|---|
| GET | `/api/alertas` | Lista alertas del usuario | AGRICULTOR |
| POST | `/api/alertas` | Crear alerta de precio | AGRICULTOR |
| PUT | `/api/alertas/{id}` | Activar o desactivar alerta | AGRICULTOR |
| DELETE | `/api/alertas/{id}` | Eliminar alerta | AGRICULTOR |

### Asistente IA
| Método | Endpoint | Descripción | Rol |
|---|---|---|---|
| POST | `/api/asistente/consulta` | Envía pregunta al agente Llama (con tool-calling) | AGRICULTOR |

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
| `/asistente` | Chat con el agente IA experto en tendencias | AGRICULTOR |
| `/mi-parcela` | Gestión de parcelas y cultivos | AGRICULTOR |
| `/alertas` | Configuración de alertas de precio | AGRICULTOR |
| `/perfil` | Datos del usuario y preferencias de notificación | AGRICULTOR |
| `/admin` | Panel: catálogo, scraping log, disparo manual | ADMIN |

---

## 🧩 Patrones de diseño aplicados

| Patrón | Aplicación concreta |
|---|---|
| **Repository** | Todos los repositorios Spring Data JPA |
| **Service Facade** | Servicios desacoplan controllers de repositorios y lógica |
| **Strategy** | `ScrapingStrategy` — interfaz intercambiable por fuente de datos |
| **Singleton** | Beans Spring gestionados por el contenedor IoC |
| **Observer** | `AlertaService` reacciona a cada actualización de precios |
| **DTO** | Separación estricta entre entidades JPA y representación API |

---

## 🚀 Arranque local

```bash
docker-compose up --build

# La primera vez, tras el arranque, descargar el modelo de Llama:
docker exec -it agrotrack-ollama ollama pull llama3.2:3b
# (~2 GB, queda persistido en el volumen ollama_data para los siguientes arranques)

# Frontend:  http://localhost
# Backend:   http://localhost:8080
# Mailpit:   http://localhost:8025
# MySQL:     localhost:3306
# Ollama:    http://localhost:11434
```

---

## 📚 Tecnologías del temario aplicadas

| Tema DRA | Tecnología | Aplicación en el proyecto |
|---|---|---|
| Tema 2 | Docker + Docker Compose | 5 servicios orquestados |
| Tema 3/5 | Angular | SPA completa con routing, guards, interceptores |
| Tema 4 | Spring Boot REST + JPA | API REST + persistencia MySQL |
| Prácticas CSS | Angular styles | Diseño visual de la app |
| Scraping | Jsoup | Extracción de precios de Mercasa |
| LLM local | Spring AI + Llama 3.2 (Ollama) | Asistente experto con tool-calling sobre datos reales de MySQL, ejecutado en infraestructura propia |
| Automatización | Spring `@Scheduled` | Job de scraping + evaluación de alertas |
| Patrones GoF | Repository, Strategy, Observer, Facade | Aplicados en capa de servicio y datos |
