# 04 - Codex Runbook

## 1. How Codex should work this repo
For each phase in `03-exec-plan.md`:
1. restate the phase goal,
2. inspect relevant files,
3. implement the smallest complete slice,
4. run tests for affected scope,
5. update documentation,
6. update `05-status-log.md`.

## 2. Commit style
Use small coherent commits/messages if committing is part of the workflow:
- `feat(site): add site and domain core`
- `feat(storefront): render home page by host`
- `feat(order): implement checkout and order creation`

## 3. Validation commands
Prefer these commands when relevant:
```bash
mvn test
mvn -q -DskipTests package
mvn spring-boot:run
```

## 4. Local demo checklist
- MySQL running.
- Database created.
- App config points to local DB.
- Hosts file maps demo domain to localhost or server IP.
- Seed data loaded.

## 5. Simplification policy
If a requested feature is broad, choose the smallest viable implementation that preserves the architecture direction.
Examples:
- coupon system -> skip in demo unless explicitly scheduled,
- PayPal/Stripe -> provider stubs + mock payment first,
- CMS page builder -> homepage config JSON only,
- supply-chain integration -> internal status table only.

## 6. Guardrails
Do not:
- add microservices,
- add Kafka/RabbitMQ unless required by a later explicit phase,
- add React/Vue frontend before backend demo is working,
- build registrar/OA engines,
- build actual cloaking logic.

## 7. What to report after each phase
Use this exact structure:

```text
Phase: <number and title>
Status: done | partial | blocked
Summary:
- ...
Touched files:
- ...
Validation:
- commands run
- results
Manual verification:
- ...
Open issues:
- ...
```
