# Ejecutar la API en Docker Desktop

Configuracion para desarrollo local con PostgreSQL existente. Docker Desktop
debe estar abierto y usar contenedores Linux. Java 21 se usa para compilar.

1. Copia `.env.docker.example` a `.env.docker` y completa la conexion a tu base.
   Este archivo esta excluido de Git y de la imagen. Si la contrasena contiene
   `$`, usa comillas simples alrededor del valor para evitar interpolacion de Compose.
2. Si PostgreSQL esta en esta PC, usa `host.docker.internal` como servidor.
   Si esta en otro equipo, usa su IP o nombre habitual. PostgreSQL debe aceptar
   conexiones desde el contenedor.
3. Ajusta `APP_CORS_ALLOWED_ORIGINS` al origen exacto de tu frontend.
4. Ejecuta en PowerShell desde la raiz del proyecto:

```powershell
.\mvnw.cmd -B package
docker compose up -d --build
docker compose logs -f api
```

Ejecuta Compose solo si la compilacion termina con BUILD SUCCESS.
La API estara en http://localhost:8080. Deten antes cualquier instancia del IDE
que ocupe ese puerto. Esta configuracion publica el puerto solo en esta PC.
Si docker no se reconoce tras instalarlo, abre una terminal nueva.

Al arrancar, Flyway puede aplicar migraciones pendientes a la base configurada.
Esta configuracion no crea ni mueve PostgreSQL ni sus datos.

Para detener y eliminar el contenedor de la API:

```powershell
docker compose down
```

Para incorporar cambios de codigo, repite la compilacion y `docker compose up -d --build`.
Las claves JWT de desarrollo se generan al arrancar: los tokens de acceso
anteriores pueden dejar de funcionar al reiniciar. Para un servidor de produccion
configura claves persistentes, HTTPS, cookies seguras y el origen del frontend.
