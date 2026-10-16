# Mesa 2.0 — Portal de soporte IT y helpdesk web

**Mesa 2.0** es el portal de autoservicio y mesa de ayuda del universo ficticio
**RoyalTown**: los empleados consultan la base de conocimiento, abren
incidencias, siguen su ciclo de vida y valoran las soluciones; el equipo de
soporte trabaja en una bandeja técnica con SLA, notas internas e informes.

Es el compañero web del CLI del repositorio `ticketing` (mismo dominio, misma
semilla de datos) y forma parte del portfolio de **Alejandro Prieto Díez**.

- **Backend**: Java 21 · Spring Boot 3.5 · Spring Security (JWT en cookie httpOnly) · Spring Data JPA · Flyway · PostgreSQL (H2 modo PostgreSQL en tests y modo local)
- **Frontend**: React 19 · TypeScript · Vite · TanStack Query · react-markdown · CSS propio con tokens de diseño (accesibilidad AA, `prefers-reduced-motion`)
- **Calidad**: 25 tests de API de punta a punta + 22 tests de frontend · CI en GitHub Actions · Dockerfile multi-stage

---

## Funcionalidades

**Portal de autoservicio (público)**
- Centro de ayuda con búsqueda instantánea sobre artículos en Markdown.
- Feedback «¿te ha servido?» con contador de votos útil / no útil.
- Los artículos no publicados quedan como borrador: solo los ve el equipo.

**Empleados**
- Alta de ticket con **sugerencias del centro de ayuda mientras se escribe el título** (autoservicio primero).
- «Mis tickets» con filtros por estado, prioridad y texto.
- Conversación con soporte, confirmación de la solución o **reapertura** si el problema sigue.
- **Valoración 1–5** (única) cuando la incidencia queda cerrada.

**Equipo de soporte**
- **Bandeja** con filtros (estado, prioridad, texto, «sin asignar», «asignados a mí»).
- Detalle con acciones: transiciones de estado, resolución obligatoria,
  asignación/desasignación, cambio de prioridad (recalcula el SLA) y categoría.
- **Notas internas** en la conversación (invisibles para quien abrió el ticket).
- **Panel de indicadores**: abiertos, SLA incumplidos, reparto por estado y
  rendimiento por técnico, calculado por **vistas SQL**.
- **Administración de la KB**: crear, editar, publicar o dejar en borrador y eliminar artículos.

## Cuentas de demostración

Contraseña para todas: `royaltown2026`

| Perfil | Correo | Notas |
| --- | --- | --- |
| Empleada | `marta.sanchez@royaltown.local` | Dueña de varios tickets de la semilla |
| Empleado | `javier.torres@royaltown.local` | Dueño del ticket #1 |
| Técnica | `laura.gomez@royaltown.local` | Bandeja, panel y KB |
| Técnico | `diego.fernandez@royaltown.local` | Bandeja, panel y KB |
| Administrador | `alejandro.ortiz@royaltown.local` | Todo + administración de la KB |

La semilla trae **48 tickets** con su historial (185 cambios), 106 comentarios,
12 valoraciones y **12 artículos** de centro de ayuda. Las fechas son de
septiembre–octubre de 2026: los tickets abiertos de la semilla aparecen con el
SLA ya vencido, lo que sirve para ver los avisos en acción.

## Cómo ejecutarlo

### 1. Docker (SPA + API con datos demo, sin configurar nada)

```bash
docker build -t mesa2 .
docker run --rm -p 8080:8080 mesa2
# http://localhost:8080
```

La imagen arranca con el perfil `local` (base H2 en fichero dentro del
contenedor). Para usar PostgreSQL, define `SPRING_PROFILES_ACTIVE` a otro valor
y las variables de datasource (ver *Despliegue*).

### 2. Desarrollo con dos procesos

```bash
# Terminal 1 — API en :8080 (perfil local, H2 en ./backend/datos)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Terminal 2 — SPA en :5173 con proxy /api → :8080
cd frontend
npm install
npm run dev
```

`http://localhost:5173` · La API desnuda (Swagger UI) en
`http://localhost:8080/swagger-ui.html`.

¿Quieres probar contra **PostgreSQL de verdad** en desarrollo? `docker compose
up -d` levanta un PostgreSQL 17 con las mismas credenciales que espera
`application.yml`; arranca el backend sin perfil (`mvn spring-boot:run`) y
Flyway aplicará las migraciones ahí.

### 3. Jar único como en producción

```powershell
./build-all.ps1        # npm ci + lint + tests + build, copia el dist al backend y empaqueta
java -jar backend/target/mesa2-1.0.0.jar --spring.profiles.active=local
```

## Estructura

```
mesa2/
├── backend/                  API Spring Boot
│   ├── src/main/java/…       dominio (modelo, repo, servicio), web, seguridad, errores
│   ├── src/main/resources/
│   │   ├── db/migration/     V1 esquema · V2 semilla · V3 centro de ayuda (Flyway)
│   │   └── application*.yml  config por perfil (local/tests) y variables de entorno
│   └── src/test/java/…       25 tests de API (MockMvc sobre las migraciones reales)
├── frontend/                 SPA React + TypeScript
│   └── src/{api,componentes,contexto,paginas,rutas,utils,estilos,prueba}
├── Dockerfile                imagen de producción (la SPA vive dentro del jar)
├── docker-compose.yml        PostgreSQL 17 local para el perfil por defecto
├── build-all.ps1             build local de punta a punta
└── .github/workflows/ci.yml  CI: backend + frontend en cada push
```

## Migraciones y datos

- **Flyway es la única fuente de verdad del esquema** (`ddl-auto: none`).
- Los **tests cargan las mismas migraciones** sobre H2 en modo PostgreSQL: si
  el esquema o la semilla se rompen, los tests se rompen.
- La semilla **no fija ningún id**: las tablas usan identidad
  (`GENERATED BY DEFAULT AS IDENTITY`) y se insertan en orden, de modo que la
  numeración coincide con el repositorio original (tickets 1–48) y la secuencia
  queda lista para las altas nuevas (el primero que se cree es el 49). Los
  mismos scripts valen en PostgreSQL y en H2, sin migraciones específicas por
  motor.
- Las **vistas SQL** (`vw_tickets_abiertos`, `vw_sla_incumplido`,
  `vw_tickets_por_estado`, `vw_resolucion_por_tecnico`) alimentan el panel de
  indicadores y son las mismas consultas que usa el CLI de `ticketing`.

## API (resumen)

| Método y ruta | Acceso | Qué hace |
| --- | --- | --- |
| `POST /api/auth/login` · `/registro` · `/logout` · `GET /me` | Público / sesión | Sesión en cookie `mesa2_sesion` (JWT httpOnly) |
| `GET /api/tickets` | Sesión | Listado con filtros y paginación (el empleado solo ve lo suyo) |
| `POST /api/tickets` | Sesión | Alta de ticket con SLA según prioridad |
| `GET /api/tickets/{id}` | Sesión | Detalle con comentarios, historial y valoración |
| `PATCH /api/tickets/{id}` | Sesión | Cambio de prioridad y/o categoría (recalcula SLA) |
| `POST /api/tickets/{id}/comentarios` | Sesión | Comentar (nota interna si es staff) |
| `POST /api/tickets/{id}/transiciones` | Según matriz | Cambiar de estado (resolución obligatoria al resolver) |
| `POST /api/tickets/{id}/asignacion` | Staff | Asignar técnico o desasignar (`idTecnico: null`) |
| `POST /api/tickets/{id}/valoracion` | Creador | Valoración única tras el cierre |
| `GET /api/usuarios/tecnicos` | Staff | Equipo de soporte para los selectores |
| `GET /api/catalogo` | Sesión | Estados, prioridades, categorías y matriz de transiciones |
| `GET /api/informes/kpis` | Staff | KPIs del panel (vistas SQL) |
| `GET /api/kb/articulos` · `/{slug}` | Público | Centro de ayuda (solo publicados sin sesión de staff) |
| `POST /api/kb/articulos/{id}/feedback` | Público | Voto útil / no útil |
| `POST · PUT · DELETE /api/kb/articulos` | Administrador | CRUD de la base de conocimiento |

Los errores siguen **RFC 7807** (`application/problem+json`) también en la
cadena de seguridad, y añaden contexto útil: `errores` de validación por campo
o `transicionesValidas` cuando un cambio de estado no está permitido.

## Reglas de negocio

- **SLA** = `fecha_creación + horas de la prioridad` (Baja 72 h, Media 24 h,
  Alta 8 h, Crítica 4 h). Cambiar la prioridad recalcula la fecha límite.
- `fecha_cierre` se fija al entrar en **Resuelto** o **Cerrado** y se limpia al
  reabrir; el SLA deja de correr en cuanto hay fecha de cierre.
- La **matriz de transiciones** (10 transiciones) vive en la base de datos y se
  valida en el servicio. Pasar a **En progreso** autoasigna al técnico que
  actúa; pasar a **Resuelto** exige una resolución (≥ 10 caracteres) que se
  publica como comentario «Solución aplicada».
- El empleado solo puede **cerrar** (confirmar solución) o **reabrir** sus
  tickets resueltos, y valorar (una vez) los cerrados.
- **Autorización en los servicios**: para un empleado, un ticket ajeno se
  comporta como inexistente (404) para no filtrar existencia; los endpoints de
  rol puro (informes, administración de KB) usan `@PreAuthorize`.
- Las **notas internas** se filtran en el backend: quien abrió el ticket jamás
  las recibe, aunque llame a la API directamente.

## Decisiones de diseño (destacadas)

- **Cookie httpOnly + SameSite=Lax en lugar de tokens en localStorage**: el
  navegador la envía sola y JavaScript no puede leerla. Como la SPA se sirve
  desde el mismo origen (en desarrollo, vía proxy de Vite), CSRF queda
  desactivado a propósito: Lax no viaja en peticiones mutantes cross-site. En
  producción se activa `MESA2_COOKIE_SECURE=true` (requiere HTTPS).
- **`open-in-view: false`**: los DTOs se construyen dentro de la transacción y
  las consultas piden lo que necesitan con `@EntityGraph`.
- **Sin dependencias de UI**: los estilos son CSS propio con tokens (color,
  espaciado, radios) pensados para contraste AA, foco visible y movimiento
  reducido; el HTML usa `nav`, `fieldset`, `caption`, `aria-live`… donde toca.
- **Código de errores uniforme en toda la aplicación**, incluida la fase de
  seguridad, para que el frontend tenga un único camino de manejo de errores.

## Tests

```bash
cd backend && mvn test          # 25 tests de API (Auth, Tickets, KB, Informes)
cd frontend && npm test         # 22 tests (utilidades, cliente HTTP, insignias)
```

Los tests de backend usan `MockMvc` sobre una base H2 en **modo PostgreSQL**
que ejecuta las **mismas migraciones Flyway** que producción, así que cubren
esquema, semilla, seguridad, reglas de negocio y formato de errores. El
frontend prueba la capa de presentación pura: formateo de fechas y SLA,
etiquetas, insignias y el parseo de `ProblemDetail` del cliente HTTP.

## Despliegue

La imagen es autosuficiente (SPA dentro del jar). Para un hosting gratuito
típico (Render/Railway) con PostgreSQL gestionado (p. ej. Neon):

| Variable | Valor |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `produccion` (cualquier perfil que no sea `local`) |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://…/mesa2?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` / `…_PASSWORD` | credenciales de Neon |
| `MESA2_JWT_SECRET` | secreto aleatorio de ≥ 32 bytes |
| `MESA2_COOKIE_SECURE` | `true` (el portal va por HTTPS) |
| `PORT` | lo inyecta la plataforma; la app lo respeta |

Flyway aplica las migraciones en el primer arranque contra PostgreSQL; no hace
falta ningún paso manual. El endpoint `GET /actuator/health` sirve de sonda.

El repositorio incluye un **blueprint de Render** (`render.yaml`): en Render,
*New → Blueprint*, selecciona el repositorio y rellena las variables anteriores
cuando te las pida. En el plan gratuito el servicio se suspende tras un rato
sin tráfico, así que el primer acceso del día puede tardar medio minuto.

---

Hecho por **Alejandro Prieto Díez** como parte de su portfolio · universo RoyalTown.
