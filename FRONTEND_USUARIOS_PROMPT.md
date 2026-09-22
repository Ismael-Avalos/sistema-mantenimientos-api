# Prompt para Codex en el proyecto frontend

Implementa la administración de usuarios conectada al backend existente. Primero revisa la arquitectura, el cliente HTTP, los formularios y la autenticación del frontend; reutiliza sus componentes y convenciones. Completa la implementación y ejecuta las verificaciones disponibles.

## Acceso e interfaz

- La pantalla y sus acciones son exclusivas del administrador. Reconoce los nombres de rol `ADMIN` y `ADMINISTRADOR` conforme a la normalización de roles existente. El backend aplica la autorización real.
- Agrega navegación a Usuarios y una tabla con nombre, correo, rol, estado activo/inactivo y si tiene pendiente cambiar la contraseña; incluye búsqueda/filtro local, carga, errores y estado vacío.
- Implementa crear, editar nombre/correo/rol/estado, activar/desactivar, eliminar y restablecer contraseña temporal, con confirmación para eliminar y restablecer.
- Obtén los roles desde `GET /maintenances/roles`: devuelve una lista de `{id,nombre,descripcion}`. No inventes UUID. El campo `rol` de usuario contiene el nombre; resuelve el `rolId` del formulario comparando con el catálogo.
- Impide eliminar, desactivar o cambiar el rol de la cuenta actualmente autenticada; permite editar nombre/correo. Para cambiar su propia contraseña usa el flujo personal existente.

## Contrato HTTP

Usa la URL base configurada y `Authorization: Bearer <accessToken>` del cliente actual. Las siguientes rutas requieren administrador y no llevan el prefijo `/api`.

`UserResponse` tiene `{id,nombre,correo,rol,activo,debeCambiarContrasena,createdAt,updatedAt}`. Nunca contiene contraseñas.

| Acción | Método y ruta | JSON enviado | Respuesta |
|---|---|---|---|
| Listar | `GET /maintenances/users` | Sin cuerpo | 200, lista de UserResponse |
| Consultar | `GET /maintenances/users/{id}` | Sin cuerpo | 200, UserResponse |
| Crear | `POST /maintenances/users` | `{nombre,correo,contrasena,rolId}` | 200, UserResponse |
| Editar | `PUT /maintenances/users/{id}` | `{nombre,correo,rolId,activo}`; todos obligatorios | 200, UserResponse |
| Estado | `PATCH /maintenances/users/{id}/estado` | `{activo: true}` o `{activo: false}` | 200, UserResponse |
| Restablecer | `POST /maintenances/users/{id}/restablecer-contrasena` | `{contrasenaTemporal}` | 204 sin cuerpo |
| Eliminar | `DELETE /maintenances/users/{id}` | Sin cuerpo | 204 sin cuerpo |

Nombre obligatorio, máximo 100 caracteres. Correo válido obligatorio, máximo 150 caracteres. Contraseña temporal obligatoria, entre 8 y 128 caracteres, sin exigir composición adicional ni permitir solo espacios. No recortes ni transformes contraseñas.

El administrador introduce la contraseña temporal al crear o restablecer; el backend la almacena como hash, no la genera ni la devuelve ni envía correos. Usa un campo de contraseña con opción de mostrar/ocultar y confirmación local; limpia el valor al cerrar o terminar. No guardes contraseñas en almacenamiento del navegador, logs o analítica. Indica al administrador que debe comunicarla al usuario por un canal privado.

Crear deja la cuenta activa y con `debeCambiarContrasena=true`. Restablecer obliga nuevamente al cambio y revoca las sesiones del usuario; no reactiva cuentas inactivas. Los cambios de correo, rol o estado también invalidan las sesiones. Si el administrador cambia su propio correo, limpia la sesión y llévalo al login tras el éxito.

Eliminar borra la cuenta físicamente cuando no tiene mantenimientos asociados. Si tiene historial, el backend rechaza la eliminación para conservarlo; ofrece desactivar en su lugar, con confirmación del usuario. Tras cada operación actualiza los datos visibles desde la API. No intentes leer JSON de respuestas 204.

## Contraseñas temporales y sesión

Conserva o completa el flujo obligatorio de cambio de contraseña: `POST /api/auth/login` recibe `{correo,contrasena}` y devuelve `{accessToken,tokenType,expiresIn,usuario}`; `usuario` incluye `id,nombre,correo,rol,activo,debeCambiarContrasena`. Si esta última bandera es true, dirige a cambiar contraseña y bloquea la navegación funcional.

`POST /api/auth/cambiar-contrasena` requiere Bearer y recibe `{contrasenaActual,nuevaContrasena}` (sin usuarioId). La nueva debe ser diferente de la actual, tener 8-128 caracteres e incluir al menos un número. Devuelve 204 y revoca la sesión: limpia el estado y solicita login con la nueva contraseña. `GET /api/auth/me` devuelve el usuario actual. Reutiliza el refresh por cookie HttpOnly y evita bucles de reintento ante sesiones revocadas.

## Errores y verificaciones

Los errores son `application/problem+json` con `{type,title,status,detail,instance,code,timestamp}`. Muestra `detail` de forma segura como texto y contempla:

- 400 `VALIDATION_ERROR`, `INVALID_REQUEST`, `AUTH_PASSWORD_REUSED`.
- 404 `USER_NOT_FOUND`.
- 409 `USER_EMAIL_EXISTS` o `DUPLICATE_ENTRY`: correo duplicado.
- 409 `USER_SELF_MODIFICATION`: intento de eliminar/desactivar/cambiar rol propio.
- 409 `USER_HAS_MAINTENANCES`: ofrece desactivar para conservar historial.
- 401: recuperación de sesión según el cliente actual; si falla, login. Login de cuenta inactiva devuelve `AUTH_ACCOUNT_INACTIVE`.
- 403 `AUTH_ACCESS_DENIED`: acceso denegado; `AUTH_PASSWORD_CHANGE_REQUIRED`: redirigir al cambio obligatorio.

Verifica acceso por rol, CRUD, estado, errores de correo, validación de temporales, eliminación con historial, confirmaciones y flujo login con temporal → cambio obligatorio → nuevo login. No simules éxito ni sustituyas la API con mocks en producción. Al finalizar informa archivos cambiados y resultados de las verificaciones.
