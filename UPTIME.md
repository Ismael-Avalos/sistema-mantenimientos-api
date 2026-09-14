# Monitoreo con UptimeRobot en Render

Después de desplegar estos cambios, configura un monitor HTTP(S):

- URL: `https://TU-SERVICIO.onrender.com/api/health` (reemplaza el dominio).
- Método: `GET`.
- Intervalo: 10 minutos.
- Estado esperado: `200`. Si utilizas validación de contenido, busca `OK`.
- Sin autenticación, cookies ni cabeceras personalizadas `Authorization` u `Origin`.

Verificación manual:

```sh
curl -i https://TU-SERVICIO.onrender.com/api/health
```

Devuelve `200 OK`, `Content-Type: text/plain` y cuerpo `OK`, con
`Cache-Control: no-store`. `HEAD` también está permitido, sin cuerpo.

## Alcance y seguridad

Únicamente GET y HEAD de la ruta exacta `/api/health` se agregan como públicos.
Se conserva la cadena de Spring Security, sus cabeceras, validación JWT,
restricciones de roles y configuración CORS. Las reglas existentes de OPTIONS
se conservan. Un Bearer inválido sigue produciendo 401; un Origin no autorizado,
403. UptimeRobot no necesita agregarse a la lista de orígenes del frontend.

La respuesta es constante: no revela versiones, usuarios, configuración ni
credenciales, no modifica datos y no consulta PostgreSQL ni servicios externos.
Confirma que el servidor HTTP responde; no certifica que la base de datos o todas
las funciones de negocio estén disponibles. No se requiere instalar Actuator.

## Límites de Render gratuito

Render suspende por inactividad después de 15 minutos sin tráfico entrante.
Una consulta exitosa cada 10 minutos ayuda a evitar esa suspensión, pero no
garantiza disponibilidad 24/7: pueden ocurrir reinicios, despliegues, fallos del
monitor o agotamiento de las 750 horas gratuitas mensuales compartidas por el
workspace. El arranque de una instancia suspendida puede tardar alrededor de
un minuto. Configura alertas en el monitor para detectar fallos.

Referencia: [limitaciones oficiales de Render Free](https://render.com/docs/free).
El endpoint no programa las consultas: es necesario desplegarlo y crear el
monitor en UptimeRobot.

## Actividad interna en Supabase

El backend ejecuta `SELECT 1` un minuto después de iniciar el programador y luego
cada 6 horas, contadas desde la finalización del intento anterior. Usa la conexión
existente de la aplicación, sin leer tablas ni escribir datos. Esta tarea es
independiente de `/api/health`: las peticiones públicas no disparan consultas.

Variables opcionales de Render (los valores indicados son los predeterminados):

- `APP_DATABASE_KEEP_ALIVE_ENABLED=true`: usa `false` para desactivar la tarea.
- `APP_DATABASE_KEEP_ALIVE_INTERVAL=6h`: intervalo entre intentos.
- `APP_DATABASE_KEEP_ALIVE_INITIAL_DELAY=1m`: espera inicial.

La consulta tiene un timeout JDBC de 5 segundos; la adquisición de conexión está
sujeta al timeout del pool (Hikari: 30 segundos por defecto). El timeout JDBC no
es un límite total de pared ante fallos de red. La tarea reutiliza el pool y cierra
sus recursos después de cada intento, sin cambiar los timeouts de consultas de negocio.
Registra solamente éxito o fallo, sin mensajes del driver ni credenciales. Ante
un fallo espera al siguiente intervalo, sin reintentos intensivos.

La tarea funciona mientras el proceso Java está activo; no puede reactivar un
proyecto Supabase ya pausado. `/api/health` puede devolver OK aunque falle la BD.
Verifica los registros de Render para comprobar el resultado de la tarea.
No hacen falta nuevas claves, permisos ni cambios en RLS o Spring Security.

Supabase puede pausar proyectos gratuitos con poca actividad durante 7 días.
No garantiza que `SELECT 1` satisfaga su criterio de actividad; esta medida no
garantiza disponibilidad ni evita todas las pausas. Consulta la
[política oficial de Supabase](https://supabase.com/docs/guides/platform/free-project-pausing).
