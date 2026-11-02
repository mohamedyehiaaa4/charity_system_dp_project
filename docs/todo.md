# Phase 1 checklist

Details are in [phase1-tasks.md](phase1-tasks.md).

## Part 1: Shared setup

- [x] G1. `.gitignore` and README *(Jana)*
- [x] G1. GitHub collaborators added, setup committed and pushed to `main` *(Jana)*
- [x] G2. `pom.xml` (Java 17, SQLite, JUnit, Mockito) *(Jana)*
- [x] G3. All folders and files created, project compiles *(Jana)*
- [x] G4. All enums with agreed values *(Jana)*
- [x] G5. Data classes: fields, constructors, getters/setters *(Jana)*
- [x] G6. Shared method signatures written as empty methods *(Jana)*
- [x] G7. `schema.sql`, `seed.sql` *(Wael)*, `app.properties` *(Ashry)*
- [x] G8. Git branches and pull request rules agreed *(everyone)*
- [x] G9. `SetupCheckTest` passes, `mvn test` works on all six machines, everyone has reviewed their classes *(everyone)*. `SetupCheckTest` deleted afterwards.

## Part 2: Individual work (at the same time)

| Person | Step 1 | Step 2 | Reviewer |
| --- | --- | --- | --- |
| Ezz | [x] Donation types | [x] DonationFactory | Resha |
| Resha | [x] Decorator base | [x] Four wrappers | Ezz |
| Wael | [x] DatabaseConnection | [x] DonationRepository | Jana |
| Mark | [x] NotificationService | [x] Three channels | Ashry |
| Ashry | [x] DonationService and lifecycle | [x] Observers and AppContext | Mark |
| Jana | [x] Users and login | [x] Lookup CRUD | Wael |

## Final check (all six)

- [ ] Real classes connected, full scenario works
- [ ] `mvn clean verify` passes
- [ ] Class diagram matches the code

## Open issues (found in review, 2026-11-02)

Must fix before the final check:

- [ ] Write tests for the five donation types and `DonationFactory` (creating each type, rejected input, add-on order, `restore`) *(Ezz)*
- [ ] Remove `/src/test/java/com/fooddonation/model/donation/` from `.gitignore`, so factory tests get committed *(Ezz)*
- [ ] Write tests for the decorator base and the four wrappers (description text, priority bonuses, stacking, `unwrap`, `collectAddOns`). `DonationDecoratorTest` exists with 2 tests for `getBaseDonation()` only; add to it *(Resha)*
- [ ] `DatabaseConnectionTest` and `DonationRepositoryTest` call `reset()` on the real singleton, which wipes `fooddonation.db` in the project root on every `mvn test`. Point them at a temporary database *(Wael)*

Missing Phase 1 deliverables (from the grading table and `Food_Donation_System_Requirements.docx`):

- [ ] **MVC (2 marks):** add JavaFX to `pom.xml`, build the views and the controllers in `controller/`. `plan.md` still says "No UI work in this phase", which conflicts with the requirements doc; update it *(team to decide owners)*
- [ ] **User control panel (1 mark, DON-8):** donor screen to submit a donation, see their donations and statuses, and edit their profile and notification channel *(team to decide)*
- [ ] **CRUD lookup screens (0.5 marks, DON-7):** coordinator screen for categories, storage types and statuses. The logic in `LookupRepository` is done; only the screen is missing *(team to decide)*
- [ ] **Class diagram (2 marks):** whole-system domain diagram and Phase 1 patterns diagram. Not in the repository yet *(team to decide)*

Code doesn't match the requirements doc (fix the code or the doc):

- [ ] DON-3: food donations have no **food name** and no **pickup address** (missing in `FoodDonation`, `DonationRequest` and the `donations` table) *(Ezz, Wael)*
- [ ] DON-10: nothing generates a donation receipt or an annual tax summary. Implement, or move it to Phase 2 with Payment in the doc *(team to decide)*
- [ ] DON-1: the doc says donor type "company", the `DonorType` enum says `ORGANIZATION` *(Jana)*
- [ ] DON-7: the doc says units "portions", the `Unit` enum says `PIECES` *(Ezz)*

Minor, can wait:

- [ ] `AuthService.updateProfile` keeps the stored role but not the stored class: a `Coordinator` object with a donor's ID saves `donor_type = NULL`, and loading that user then crashes *(Jana)*
- [ ] `DonationService.changeStatus` checks the status, then updates it; the SQL `UPDATE` doesn't check the old status, so two changes at the same moment could both succeed *(Ashry)*
- [ ] Email and SMS log times use the computer's time zone, not `timezone` from `app.properties` *(Mark)*
- [ ] `users.email` is `UNIQUE` but case-sensitive in the database. `AuthService` lowercases emails, so this only matters if something calls `UserRepository.save` directly *(Wael)*
- [ ] `DonationFactory` only checks pickup start < pickup end. It allows pickup times in the past, pickup after expiry, and an expiry date that has already passed *(Ezz)*
- [ ] Files mix LF and CRLF line endings. Add a `.gitattributes` to make them consistent *(Jana)*

## Test results

| Task | Owner | Test / command | Result |
| --- | --- | --- | --- |
| Setup compiles | Jana | `mvn test` (no tests yet) | Passed (2026-10-29) |
| Observer, lifecycle, expiry, wiring | Ashry | `DonationServiceTest`, `ExpirySchedulerTest`, `ObserversTest`, `AppConfigTest` (32 tests, mocks) | Passed (2026-11-02) |
| `SetupCheckTest` | Jana | `javac` + JUnit directly (not `mvn test`), JDK 27 | Passed, 2 of 2 (2026-10-30) |
| Users and login | Jana | `UserTest`, `UserRepositoryTest`, `AuthServiceTest`, `javac` + JUnit directly, JDK 27 | Passed, 18 of 18 (2026-10-30) |
| Lookup CRUD | Jana | `LookupRepositoryTest`, `javac` + JUnit directly, JDK 27 | Passed, 13 of 13 (2026-10-30) |
| `SetupCheckTest` | Jana | `mvn test` or run in IntelliJ | Passed on Wael's machine (2026-10-31) |
| `DatabaseConnection` | Wael | `DatabaseConnectionTest` (6 tests) | Passed (2026-10-31) |
| `DonationRepository` | Wael | `DonationRepositoryTest` (10 tests, factory and decorator mocked) | Passed (2026-10-31) |
| Strategy and channels | Mark | `NotificationServiceTest`, `NotificationChannelsTest` (11 tests), `javac` + JUnit directly, JDK 27 | Passed, 11 of 11 (2026-11-02) |
| Donation types and factory | Ezz | No tests in the repository yet | Not written |
| Decorator and wrappers | Resha | Only `DonationDecoratorTest` (2 tests, `getBaseDonation()`); the wrappers themselves have no tests yet | Passed, 2 of 2 (2026-11-02) |
| Volunteer observer keeps going after one failed send | Jana | `ObserversTest.oneFailedVolunteerDoesNotStopTheOthers` (`ObserversTest` now has 5 tests) | Passed (2026-11-02) |
| All tests | Jana | Every test class, `javac` + JUnit directly, JDK 27 | Passed, 93 of 93 (2026-11-02) |
| Full scenario with real classes | Jana | Script outside the repo: register → submit with add-ons → reload → approve → change channel → pick up → reject → expiry check | Worked (2026-11-02). Still to be done together for the final check |
