# GoCode API Deployment

This service is the execution backend for the Android app.

It must run as a container because it needs:

- Python/FastAPI
- OpenJDK 17
- `javac` and `java` subprocess execution
- `gcc` subprocess execution for C
- server-side OpenAI access for hints

## Required Environment Variables

```bash
OPENAI_API_KEY=sk-...
PORT=8080
```

Do not commit real `.env` files. Use `.env.example` as the template.

## Local Container Check

From the `server` directory:

```bash
docker compose up --build
```

Health check:

```bash
curl http://localhost:8080/health
```

## Cloudflare Deployment Notes

This API cannot be deployed as a plain Cloudflare Pages static app because it runs a Dockerized Java execution service.

Use one of these production shapes:

1. Cloudflare in front of a container host:
   - Deploy this Docker image to a container platform.
   - Point a Cloudflare DNS record such as `api.gocode.app` to that host.
   - Enable HTTPS through Cloudflare.

2. Cloudflare Containers, if available on the account:
   - Build from `server/Dockerfile`.
   - Configure `OPENAI_API_KEY` as a secret.
   - Expose port `8080` or set `PORT` to the platform-provided port.

## Android Release API URL

Build release with the public HTTPS API URL:

```bash
./gradlew :app:assembleRelease -PGOCODE_API_BASE_URL=https://api.gocode.app/
```

On Windows PowerShell:

```powershell
$env:GOCODE_API_BASE_URL = "https://api.gocode.app/"
.\gradlew.bat :app:assembleRelease
```

Debug builds use:

```text
http://10.0.2.2:8080/
```

which maps the Android emulator to the host machine.

## Production Checklist

- Confirm `GET /health` returns `{"ok": true}` and includes `c` in `languages`.
- Confirm `/run` compiles and runs Java.
- Confirm `/run` compiles and runs C with `language: "c"`.
- Confirm `/lint` returns compiler errors.
- Confirm `/hint` works only when `OPENAI_API_KEY` is configured.
- Use HTTPS API URLs in Android release builds.
- Keep `OPENAI_API_KEY` only on the server/container platform.
