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

- [x] Write tests for the five donation types and `DonationFactory` (creating each type, rejected input, add-on order, `restore`) *(Ezz; done by Mark: `DonationFactoryTest`, 29 tests)*
- [x] Remove `/src/test/java/com/fooddonation/model/donation/` from `.gitignore`, so factory tests get committed *(Ezz; done by Mark)*
- [x] Write tests for the decorator base and the four wrappers (description text, priority bonuses, stacking, `unwrap`, `collectAddOns`). `DonationDecoratorTest` exists with 2 tests for `getBaseDonation()` only; add to it *(Resha; done by Mark: now 14 tests)*
- [x] `DatabaseConnectionTest` and `DonationRepositoryTest` call `reset()` on the real singleton, which wipes `fooddonation.db` in the project root on every `mvn test`. Point them at a temporary database *(Wael; done by Mark: package-private `DatabaseConnection(String dbPath)` used with a JUnit `@TempDir`)*

Missing Phase 1 deliverables (from the grading table and `Food_Donation_System_Requirements.docx`):

- [x] **MVC (2 marks):** add JavaFX to `pom.xml`, build the views and the controllers in `controller/`. `plan.md` still says "No UI work in this phase", which conflicts with the requirements doc; update it *(done by Mark: JavaFX 21, 11 FXML views with one controller each, `ui/` for start-up and navigation; `plan.md` updated. Run with `mvn javafx:run`)*
- [x] **User control panel (1 mark, DON-8):** donor screen to submit a donation, see their donations and statuses, and edit their profile and notification channel *(done by Mark: donor panel with Submit, My donations (with receipts and annual summary), Inbox and Profile tabs)*
- [x] **CRUD lookup screens (0.5 marks, DON-7):** coordinator screen for categories, storage types and statuses. The logic in `LookupRepository` is done; only the screen is missing *(done by Mark: Lookup tables tab in the coordinator dashboard)*
- [ ] **Class diagram (2 marks):** whole-system domain diagram and Phase 1 patterns diagram. Not in the repository yet *(team to decide)*

Code doesn't match the requirements doc (fix the code or the doc):

- [x] DON-3: food donations have no **food name** and no **pickup address** (missing in `FoodDonation`, `DonationRequest` and the `donations` table) *(Ezz, Wael; done by Mark)*. Both are now in `FoodDonation`, `DonationRequest` and the `food_name` / `pickup_address` columns. The food name is required; a blank pickup address defaults to the donor's address. `DatabaseConnection.initialize()` adds the two columns to an existing `fooddonation.db`, so nobody has to delete theirs
- [x] DON-10: nothing generates a donation receipt or an annual tax summary. Implement, or move it to Phase 2 with Payment in the doc *(done by Mark)*. `ReceiptService` builds a `DonationReceipt` for any accepted donation (Approved, Picked up or Delivered) and an `AnnualDonationSummary` per donor and year (money total plus food listed in kind). `ReceiptObserver` sends the donor their receipt when a donation is approved. Receipts are built from the saved donation, so no new table. Payment details (PAY-1) stay in Phase 2
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
| Donation types and factory | Ezz | `DonationFactoryTest` (29 tests, written by Mark) | Passed, 29 of 29 (2026-11-02) |
| Decorator and wrappers | Resha | `DonationDecoratorTest` (14 tests: each wrapper, stacking, `unwrap`, `collectAddOns`, pass-through; extended by Mark) | Passed, 14 of 14 (2026-11-02) |
| Volunteer observer keeps going after one failed send | Jana | `ObserversTest.oneFailedVolunteerDoesNotStopTheOthers` (`ObserversTest` now has 5 tests) | Passed (2026-11-02) |
| All tests | Jana | Every test class, `javac` + JUnit directly, JDK 27 | Passed, 93 of 93 (2026-11-02) |
| Full scenario with real classes | Jana | Script outside the repo: register → submit with add-ons → reload → approve → change channel → pick up → reject → expiry check | Worked (2026-11-02). Still to be done together for the final check |
| DON-3 and DON-10 | Mark | `DonationRepositoryTest` (food name, pickup address), `DatabaseConnectionTest` (old database gets new columns), `ReceiptServiceTest` (9), `ReceiptObserverTest` (3) | Passed (2026-11-02) |
| All tests | Mark | `mvn clean verify`, real `fooddonation.db` left untouched | Passed, 147 of 147 (2026-11-02) |
| DON-3/DON-10 scenario with real classes | Mark | Script outside the repo, temporary database: register → submit food with add-on and money → reload (food name, address kept) → approve both → receipt in donor's inbox → annual summary | Worked (2026-11-02) |
| GUI loads every screen | Mark | Script outside the repo: real `AppContext` on a scratch database, opens sign-in, register, all 4 donor tabs and all 5 coordinator tabs, takes a screenshot of each | Worked, no errors (2026-11-02). Clicking through by hand still to do |
