# wordpress-sass

## Profiles

- `local`: local development profile with MySQL defaults and verbose MyBatis logs.
- `dev`: environment-driven profile for shared dev deployment.

## Start

1. Prepare MySQL and create the target database.
2. Apply SQL scripts in `docs/database.sql` or incremental scripts in `docs/migrations/`.
   Migration naming and execution rules are documented in `docs/migration-guidelines.md`.
3. Start the app with a profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

For shared dev:

```bash
DB_HOST=127.0.0.1 DB_NAME=word_pass DB_USERNAME=root DB_PASSWORD=123456 \
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Health Check

After startup, verify:

```bash
curl http://localhost:8080/actuator/health
```

Expected result:

```json
{"status":"UP"}
```

## Demo Docs

- runbook: `docs/demo-runbook.md`
- regression checklist: `docs/demo-regression-checklist.md`
- seed script: `scripts/demo-seed.sh`
