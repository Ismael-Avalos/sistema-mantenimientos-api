# Contrato de autenticación y autorización

La API usa sesiones stateless: JWT de acceso RS256 (15 minutos por defecto) y refresh token opaco rotatorio (7 días por defecto). El JWT se envía como `Authorization: Bearer <accessToken>`. El refresh token nunca aparece en JSON: se entrega en una cookie `HttpOnly` cuyo alcance predeterminado es `/api/auth`.

## Contrato HTTP

### `POST /api/auth/login`

Solicitud: `{"correo":"usuario@dominio.com","contrasena":"..."}`.

Respuesta 200:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "usuario": {
    "id": "uuid",
    "nombre": "Nombre",
    "correo": "usuario@dominio.com",
    "rol": "ADMIN",
    "activo": true,
    "debeCambiarContrasena": false
  }
}
```

### `POST /api/auth/refresh`

Usa la cookie de refresh, la rota y devuelve el mismo formato de sesión que login más una cookie nueva. La reutilización de una cookie ya rotada revoca toda su familia.

### `POST /api/auth/logout`

Requiere Bearer y la cookie. Revoca la familia de refresh en el servidor, elimina la cookie y responde 204.

### `GET /api/auth/me`

Requiere Bearer. Responde los datos actuales obtenidos de PostgreSQL.

### `POST /api/auth/cambiar-contrasena`

Requiere Bearer. Solicitud: `{"contrasenaActual":"...","nuevaContrasena":"..."}`. No acepta `usuarioId`. La nueva contraseña debe tener 12-128 caracteres e incluir mayúscula, minúscula, número y símbolo. Responde 204, revoca todas las sesiones e invalida inmediatamente todos los access tokens anteriores; el cliente debe iniciar sesión de nuevo.

Si `debeCambiarContrasena=true`, el backend solamente permite `/me`, `/refresh`, `/logout` y `/cambiar-contrasena`.

## Permisos

| Rutas | ADMIN | TECNICO |
|---|---:|---:|
| `/maintenances/users/**`, `/maintenances/roles/**` | Sí | No |
| GET equipos, ubicaciones y categorías | Sí | Sí |
| Crear/modificar/eliminar equipos, ubicaciones y categorías | Sí | No |
| Consultar y crear `/api/mantenimientos/**` | Sí | Sí |

No se aplicó una regla de propiedad/asignación porque el dominio actual no define una política verificable para ello.

## Errores

Los errores usan `application/problem+json` con `type`, `title`, `status`, `detail`, `instance`, `code` y `timestamp`. Códigos de autenticación principales: `AUTH_INVALID_CREDENTIALS`, `AUTH_ACCOUNT_INACTIVE`, `AUTH_TOKEN_MISSING`, `AUTH_TOKEN_INVALID`, `AUTH_REFRESH_INVALID`, `AUTH_REFRESH_EXPIRED`, `AUTH_REFRESH_REUSED`, `AUTH_PASSWORD_CHANGE_REQUIRED`, `AUTH_ACCESS_DENIED` y `VALIDATION_ERROR`.

## Variables de entorno

- `APP_JWT_ISSUER`, `APP_JWT_AUDIENCE`
- `APP_JWT_ACCESS_TTL` (predeterminado `15m`), `APP_REFRESH_TOKEN_TTL` (predeterminado `7d`)
- `APP_JWT_PUBLIC_KEY`: RSA pública X.509 en PEM
- `APP_JWT_PRIVATE_KEY`: RSA privada PKCS#8 en PEM
- `APP_JWT_ALLOW_EPHEMERAL_DEV_KEYS`: `true` solo en desarrollo; en producción debe ser `false`
- `APP_CORS_ALLOWED_ORIGINS`: lista separada por comas, sin comodín
- `APP_REFRESH_COOKIE_SECURE`: `true` en HTTPS/producción
- `APP_REFRESH_COOKIE_SAME_SITE`: normalmente `Lax` o `Strict`; `None` requiere HTTPS
- `APP_REFRESH_COOKIE_NAME`, `APP_REFRESH_COOKIE_PATH`
- `DB_URL`, `DB_USERAPI`, `DB_PASSWORDAPI`

La cookie no define `Domain`: es *host-only* y el navegador sólo la devuelve al mismo host que respondió
al login. En desarrollo use un único nombre de host en toda la aplicación (por ejemplo,
frontend `http://localhost:5173` y API `http://localhost:8080`); no alterne `localhost` con
`127.0.0.1`. Tanto login como refresh deben enviarse con credenciales habilitadas en Axios.

Los endpoints funcionales usan Bearer y por ello no dependen de cookies. Refresh/logout validan cualquier encabezado `Origin` contra la lista CORS y la cookie usa SameSite. Para un despliegue cross-site configure `SameSite=None`, `Secure=true`, una lista de orígenes exacta y mantenga la validación de Origin. Clientes móviles/escritorio pueden usar un almacén seguro de cookies; nunca deben exponer el refresh token a JavaScript o almacenamiento web.

Ejemplo sin secretos reales:

```bash
curl -i -c cookies.txt -H 'Content-Type: application/json' \
  -d '{"correo":"usuario@dominio.com","contrasena":"CAMBIAR"}' \
  http://localhost:8080/api/auth/login
curl -i -b cookies.txt -X POST http://localhost:8080/api/auth/refresh
```
