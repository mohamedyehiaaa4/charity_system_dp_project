# Phase 1 checklist

Details are in [phase1-tasks.md](phase1-tasks.md).

## Part 1: Shared setup

- [x] G1. `.gitignore` and README *(Jana)*
- [ ] G1. GitHub collaborators added, setup committed and pushed to `main` *(Jana)*
- [x] G2. `pom.xml` (Java 17, SQLite, JUnit, Mockito) *(Jana)*
- [x] G3. All folders and files created, project compiles *(Jana)*
- [x] G4. All enums with agreed values *(Jana)*
- [x] G5. Data classes: fields, constructors, getters/setters *(Jana)*
- [x] G6. Shared method signatures written as empty methods *(Jana)*
- [x] G7. `schema.sql`, `seed.sql`, `app.properties` *(Jana)*
- [ ] G8. Git branches and pull request rules agreed *(everyone)*
- [ ] G9. `SetupCheckTest` passes, `mvn test` works on all six machines, everyone has reviewed their classes *(everyone)*

## Part 2: Individual work (at the same time)

| Person | Step 1 | Step 2 | Reviewer |
| --- | --- | --- | --- |
| Ezz | [ ] Donation types | [ ] DonationFactory | Resha |
| Resha | [ ] Decorator base | [ ] Four wrappers | Ezz |
| Wael | [x] DatabaseConnection | [x] DonationRepository | Jana |
| Mark | [ ] NotificationService | [ ] Three channels | Ashry |
| Ashry | [ ] DonationService and lifecycle | [ ] Observers and AppContext | Mark |
| Jana | [ ] Users and login | [ ] Lookup CRUD | Wael |

## Final check (all six)

- [ ] Real classes connected, full scenario works
- [ ] `mvn clean verify` passes
- [ ] Class diagram matches the code

## Test results

| Task | Owner | Test / command | Result |
| --- | --- | --- | --- |
| Setup compiles | Jana | `mvn test` (no tests yet) | Passed (2026-10-29) |
| `SetupCheckTest` | Jana | `mvn test` or run in IntelliJ | Passed on Wael's machine (2026-10-31) |
| `DatabaseConnection` | Wael | `DatabaseConnectionTest` (6 tests) | Passed (2026-10-31) |
| `DonationRepository` | Wael | `DonationRepositoryTest` (10 tests, factory and decorator mocked) | Passed (2026-10-31) |
