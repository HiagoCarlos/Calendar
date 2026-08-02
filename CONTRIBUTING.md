# Contributing

## Enabling the commit template

This repository has a commit message template at `.gitmessage`
(following the [Conventional Commits](https://www.conventionalcommits.org/) standard).
To enable it locally:

```bash
git config commit.template .gitmessage
```

When you run `git commit` (without `-m`), your editor will open with the template pre-filled.

## Running with Docker

**Development** (hot reload):

```bash
cp .env.example .env
docker compose -f docker-compose.dev.yml up --build
```

- Backend: http://localhost:8080
- Frontend: http://localhost:4200
- Postgres: localhost:5432

**Production** (optimized build):

```bash
cp .env.example .env
docker compose up -d --build
```

- Backend: http://localhost:8080
- Frontend (SSR): http://localhost:4000

## Modules without a runtime Dockerfile

- `desktop/`: Swing/JavaFX desktop application. Has a `Dockerfile.build` used
  only in CI to compile/test reproducibly — it doesn't run via `docker run`
  (it's a desktop GUI app).
- `mobile/`: Flutter app. CI uses `subosito/flutter-action` directly,
  without Docker (Flutter images are heavy and APK/iOS builds have
  specific toolchain requirements).

## Known issues

- **Backend returns 404 (Whitelabel Error Page) on `/`**: expected as long as
  there's no `@RestController` mapped. This isn't a Docker error.
- **Frontend: `Header "host" with value "..." is not allowed`**: Angular SSR's
  (`@angular/ssr`) anti-SSRF protection. Set `allowedHosts` in `angular.json`
  (`architect.build.options.security.allowedHosts`) to the real host(s) used
  to access the app, and/or the `NG_ALLOWED_HOSTS` env var (see `.env.example`)
  for extra hosts at runtime.

## CI/CD

- `ci-backend.yml`, `ci-frontend.yml`, `ci-desktop.yml`, `ci-mobile.yml`:
  build + tests per module, triggered only when the respective directory changes.
- `codeql.yml`: static security analysis (GitHub Advanced Security).
- `docker-build-push.yml`: builds and publishes the `backend` and
  `frontend` images to GHCR (GitHub Container Registry) on every push to `main` or a `vX.Y.Z` tag.

## Commit and PR conventions

- Commits follow Conventional Commits (see `.gitmessage`).
- PRs automatically use the template at `.github/PULL_REQUEST_TEMPLATE.md`.