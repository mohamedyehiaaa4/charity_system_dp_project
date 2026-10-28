# Phase 1 tasks

Code goes under `src/main/java/com/fooddonation/`, tests under `src/test/java/com/fooddonation/`. See [plan.md](plan.md) for how the two parts fit together.

---

## Part 1: Shared setup

By the end of Part 1, the repository contains **every folder, every file and every class**, the database schema is written, and the project compiles. Nobody starts Part 2 until G1–G9 are all checked.

**Jana set up G1–G7.** Everyone else reviews them (G9): if a class, field or signature you own or use is wrong, say so before Part 2 starts.

### G1. Tools and repository *(Jana)*

- [ ] Everyone installs **JDK 17 or 21** (not newer, since test libraries may not support it yet), Maven 3.9+, Git and an IDE (IntelliJ, or VS Code with the Java extension). Check with `java -version` and `mvn -version`.
- [ ] Add all six as collaborators on the GitHub repository (`charity_system_dp_project`).
- [x] `.gitignore` with: `target/`, `*.db`, `logs/`, `.idea/`, `.vscode/`, `*.iml`.
- [x] `README.md`: how to build, links to the docs, where each design pattern lives, and what each package contains.
- [ ] Commit the setup and push it to `main`.
- [ ] Everyone clones the repository and builds it once.

### G2. Maven build file *(Jana)*

- [x] `pom.xml` at the repository root: Java 17, `sqlite-jdbc` 3.45.1.0, JUnit 5.10.2, Mockito 5.14.2. See the file itself for the exact contents. It also pins the compiler plugin and lets Mockito run on JDKs newer than it officially supports.

### G3. Folder and file structure *(Jana)*

All files below exist and compile. Methods that aren't written yet throw `new UnsupportedOperationException("TODO")`.

```text
pom.xml
src/main/java/com/fooddonation/
├── model/                              the M in MVC: data classes
│   ├── donation/                       (Ezz)
│   │   ├── Donation.java               abstract base class
│   │   ├── FoodDonation.java           abstract, extends Donation
│   │   ├── CookedFoodDonation.java     constructors are package-private:
│   │   ├── RefrigeratedFoodDonation.java   create donations through DonationFactory
│   │   ├── FreshProduceDonation.java
│   │   ├── PackagedFoodDonation.java
│   │   ├── MoneyDonation.java
│   │   ├── DonationRequest.java        input data for creating a donation
│   │   ├── DonationType.java           enum
│   │   ├── Unit.java                   enum
│   │   └── DonationFactory.java        Factory pattern
│   ├── user/                           (Jana)
│   │   ├── User.java                   abstract
│   │   ├── Donor.java
│   │   ├── Coordinator.java
│   │   ├── DonorType.java              enum
│   │   └── Role.java                   enum
│   └── lookup/                         (Jana)
│       ├── LookupEntry.java            base class for lookup rows
│       ├── FoodCategory.java
│       ├── StorageType.java
│       ├── DonationStatus.java
│       └── LookupType.java             enum
├── addon/                              (Resha) Decorator pattern
│   ├── AddOn.java                      enum
│   ├── DonationDecorator.java          abstract, extends Donation
│   ├── HomePickup.java
│   ├── RefrigeratedTransport.java
│   ├── UrgentPriority.java
│   └── SpecialPackaging.java
├── notification/                       (Mark) Strategy pattern
│   ├── NotificationStrategy.java       interface
│   ├── NotificationService.java        Strategy context
│   ├── InAppNotification.java
│   ├── EmailNotification.java
│   ├── SmsNotification.java
│   ├── InAppInbox.java
│   ├── InboxMessage.java
│   ├── Recipient.java
│   └── ChannelType.java                enum
├── event/                              (Ashry) Observer pattern
│   ├── DonationSubject.java            interface
│   ├── DonationObserver.java           interface
│   ├── DonationEvent.java
│   ├── EventType.java                  enum
│   ├── CoordinatorDashboardObserver.java
│   ├── DonorNotificationObserver.java
│   ├── VolunteerNotificationObserver.java
│   └── MessageTemplates.java
├── service/
│   ├── DonationService.java            (Ashry) implements DonationSubject
│   ├── ExpiryScheduler.java            (Ashry)
│   └── AuthService.java                (Jana)
├── persistence/
│   ├── DatabaseConnection.java         (Wael) Singleton pattern
│   ├── DonationRepository.java         (Wael)
│   ├── UserRepository.java             (Jana)
│   └── LookupRepository.java           (Jana)
├── controller/                         (later, for MVC)
└── app/                                (Ashry)
    ├── AppConfig.java
    └── AppContext.java

src/main/resources/
├── schema.sql                      (Wael)
├── seed.sql                        (Wael)
└── app.properties                  (Ashry) timezone, db path, log paths, volunteer contacts

src/test/java/com/fooddonation/
├── SetupCheckTest.java             temporary: checks JUnit, Mockito and SQLite work (see G9)
└── (each person adds test classes in the same package as their code)
```

- [x] All files above exist and `mvn test` passes.

### G4. Enums *(Jana)*

- [x] `DonationType`: `COOKED`, `REFRIGERATED`, `FRESH`, `PACKAGED`, `MONEY`
- [x] `Unit`: `KG`, `LITERS`, `PIECES`, `BOXES`
- [x] `AddOn`: `HOME_PICKUP`, `REFRIGERATED_TRANSPORT`, `URGENT_PRIORITY`, `SPECIAL_PACKAGING`
- [x] `Role`: `DONOR`, `COORDINATOR`
- [x] `DonorType`: `INDIVIDUAL`, `RESTAURANT`, `SUPERMARKET`, `ORGANIZATION`
- [x] `LookupType`: `FOOD_CATEGORY`, `STORAGE_TYPE`, `DONATION_STATUS`
- [x] `ChannelType`: `IN_APP`, `EMAIL`, `SMS`
- [x] `EventType`: `DONATION_SUBMITTED`, `STATUS_CHANGED`, `DONATION_REJECTED`, `NEAR_EXPIRY`

### G5. Data classes: fields, constructors, getters and setters *(fully written now)*

These classes hold data only. No business logic yet.

- [x] **`Donation`** (Ezz): `long id`, `Donor donor`, `DonationStatus status`, `LocalDateTime createdAt`, `String rejectionReason`, `DonationType type`, `LocalDate expiry`. Also `String getDescription()` and `int getPriority()`; these can be overridden, which is how decorators change them.
- [x] **`FoodDonation`** (Ezz): adds `double quantity`, `Unit unit`, `FoodCategory category`, `StorageType storageType`, `LocalDateTime pickupStart`, `LocalDateTime pickupEnd`.
- [x] **`MoneyDonation`** (Ezz): adds `BigDecimal amount`. Expiry is always `null`.
- [x] **`DonationRequest`** (Ezz): `DonationType type`, `Donor donor`, plus all the food fields above, `BigDecimal amount` and `LocalDate expiry`. Unused fields stay `null`.
- [x] **`User`** (Jana): `long id`, `String name`, `String email`, `String phone`, `String address`, `Role role`, `ChannelType preferredChannel`, plus `Recipient toRecipient()`.
- [x] **`Donor`** (Jana): adds `DonorType donorType`. **`Coordinator`** (Jana): no extra fields.
- [x] **`LookupEntry`** (Jana): `long id`, `LookupType type`, `String name`, `boolean system` (system rows are protected).
- [x] **`FoodCategory`, `StorageType`, `DonationStatus`** (Jana): extend `LookupEntry`. `DonationStatus` also has the constants `SUBMITTED`, `APPROVED`, `REJECTED`, `PICKED_UP`, `DELIVERED` holding the five system names ("Submitted", …).
- [x] **`Recipient`** (Mark): `String id`, `String name`, `String email`, `String phone`, `ChannelType preferredChannel`.
- [x] **`InboxMessage`** (Mark): `String recipientId`, `String subject`, `String message`, `LocalDateTime sentAt`, `boolean read`.
- [x] **`DonationEvent`** (Ashry): `EventType type`, `Donation donation`, `String message`, `LocalDateTime time`. Getters only (it is immutable).

### G6. Shared method signatures *(written as empty methods now)*

These are the methods **other people will call**. Write them exactly like this, with the body `throw new UnsupportedOperationException("TODO");`. Classes only used inside one person's part (e.g. the four wrappers, the observers, `AppContext`) only need to exist as files.

- [x] **`DonationFactory`** (Ezz). The concrete donation constructors are package-private, so all other code creates donations through these methods:
  - `Donation createDonation(DonationRequest request)`
  - `Donation applyAddOns(Donation donation, Set<AddOn> addOns)`
  - `Donation restore(DonationRequest data, long id, DonationStatus status, LocalDateTime createdAt, String rejectionReason, Set<AddOn> addOns)`: rebuilds a saved donation without new-donation checks.
- [x] **`DonationDecorator`** (Resha):
  - `protected DonationDecorator(Donation wrapped)`
  - `abstract AddOn getAddOn()`
  - `static Donation unwrap(Donation donation)`: returns the innermost donation.
  - `static Set<AddOn> collectAddOns(Donation donation)`: returns an empty set for a plain donation.
- [x] **`DatabaseConnection`** (Wael):
  - `static synchronized DatabaseConnection getInstance()`
  - `Connection getConnection()`
  - `void initialize()`: runs `schema.sql` and `seed.sql`.
  - `void reset()`
  - `void close()`
- [x] **`DonationRepository`** (Wael), constructor `DonationRepository(DatabaseConnection db, DonationFactory factory)`:
  - `Donation save(Donation donation)`: returns it with its new ID.
  - `Optional<Donation> findById(long id)`
  - `List<Donation> findAll()`
  - `List<Donation> findByDonor(long donorId)`
  - `List<Donation> findByStatus(DonationStatus status)`
  - `void updateStatus(long donationId, DonationStatus status, String rejectionReason)`
- [x] **`UserRepository`** (Jana), constructor `UserRepository(DatabaseConnection db)`:
  - `User save(User user)`, `Optional<User> findById(long id)`, `Optional<User> findByEmail(String email)`, `void update(User user)`
- [x] **`LookupRepository`** (Jana), constructor `LookupRepository(DatabaseConnection db)`:
  - `List<LookupEntry> readAll(LookupType type)`
  - `Optional<LookupEntry> findByName(LookupType type, String name)`
  - `LookupEntry create(User caller, LookupType type, String name)`
  - `void update(User caller, long id, String newName)`
  - `void delete(User caller, long id)`
- [x] **`AuthService`** (Jana), constructor `AuthService(UserRepository users)`:
  - `Donor register(String name, String email, String phone, String address, DonorType type, ChannelType channel)`
  - `Optional<User> login(String email)`
  - `void updateProfile(User user)`
- [x] **`NotificationStrategy`** (Mark): `void send(Recipient recipient, String subject, String message)`
- [x] **`NotificationService`** (Mark), constructor `NotificationService(Map<ChannelType, NotificationStrategy> strategies)`:
  - `void setStrategy(NotificationStrategy strategy)`
  - `void notify(Recipient recipient, String subject, String message)`: uses the current strategy.
  - `void notifyByPreference(Recipient recipient, String subject, String message)`: uses the recipient's preferred channel.
- [x] **`DonationSubject`** (Ashry): `void attach(DonationObserver o)`, `void detach(DonationObserver o)`, `void notifyObservers(DonationEvent event)`
- [x] **`DonationObserver`** (Ashry): `void update(DonationEvent event)`
- [x] **`DonationService`** (Ashry), constructor `DonationService(DonationFactory factory, DonationRepository donations, LookupRepository lookups, UserRepository users, Clock clock)`:
  - `Donation submitDonation(User caller, DonationRequest request, Set<AddOn> addOns)`
  - `List<Donation> getMyDonations(User caller)`, `List<Donation> getAllDonations(User caller)`
  - `void approve(User caller, long id)`, `void reject(User caller, long id, String reason)`, `void markPickedUp(User caller, long id)`, `void markDelivered(User caller, long id)`
  - `List<Donation> checkExpiringDonations()`
- [x] **`ExpiryScheduler`** (Ashry), constructor `ExpiryScheduler(DonationService service)`: `void start()`, `void stop()`, `void runNow()`

Rules for all classes:

- Collaborators are passed in through the **constructor**, so tests can pass mocks.
- Never call `DatabaseConnection.getInstance()` inside a repository or service method. Only `AppContext` calls it.

### G7. Database schema and seed data *(Jana; Wael and Ashry review)*

- [x] `schema.sql` starts with `PRAGMA foreign_keys = ON;` and creates:

| Table | Columns |
| --- | --- |
| `users` | `id` INTEGER PK, `name` TEXT NOT NULL, `email` TEXT NOT NULL UNIQUE, `phone`, `address`, `role` TEXT NOT NULL, `donor_type` TEXT (null for coordinator), `preferred_channel` TEXT NOT NULL |
| `food_categories` | `id` INTEGER PK, `name` TEXT NOT NULL UNIQUE, `is_system` INTEGER NOT NULL DEFAULT 0 |
| `storage_types` | same columns as `food_categories` |
| `donation_statuses` | same columns as `food_categories` |
| `donations` | `id` INTEGER PK, `donor_id` FK → users, `type` TEXT NOT NULL, `status_id` FK → donation_statuses, `created_at` TEXT NOT NULL, `rejection_reason` TEXT, `expiry` TEXT, `quantity` REAL, `unit` TEXT, `category_id` FK → food_categories, `storage_type_id` FK → storage_types, `pickup_start` TEXT, `pickup_end` TEXT, `amount` TEXT, `add_ons` TEXT |

- [x] Dates are stored as ISO text (`2026-10-29T14:30`). `add_ons` is stored as comma-separated enum names (`HOME_PICKUP,URGENT_PRIORITY`), or empty.
- [x] Foreign keys have no `ON DELETE CASCADE`, so deleting a lookup that is in use fails.
- [x] `seed.sql` uses `INSERT OR IGNORE`, so running it twice adds nothing new:
  - one coordinator (`Coordinator`, `coordinator@fooddonation.org`, `IN_APP`)
  - five system statuses (`is_system = 1`): Submitted, Approved, Rejected, Picked up, Delivered
  - a few starting categories (e.g. Bakery, Dairy, Meals, Produce) and storage types (Room temperature, Refrigerated, Frozen)
- [x] `app.properties` (Ashry): `db.path=fooddonation.db`, `timezone=Africa/Cairo`, `log.email=logs/email.log`, `log.sms=logs/sms.log`, `expiry.check.seconds=60`, `expiry.warning.hours=48`, plus the volunteer list (name, email, phone, channel for each).

### G8. Git workflow *(everyone agrees)*

- [ ] `main` must always compile. Nobody pushes directly to `main` after Part 1.
- [ ] Each person works on their own branch: `ezz/factory`, `resha/decorator`, `wael/singleton`, `mark/strategy`, `ashry/observer`, `jana/users-lookups`.
- [ ] Merge through a pull request, approved by your reviewer (see the table in [plan.md](plan.md)). Run `mvn test` before opening it.
- [ ] Only Ezz changes `pom.xml`. Only Wael changes `schema.sql` / `seed.sql`. Changing a G6 signature needs a message to the group first.

### G9. Final setup check *(everyone)*

- [ ] The setup is pushed to `main`.
- [ ] `SetupCheckTest` passes: it mocks `DonationFactory` and `DatabaseConnection` and runs a query on an in-memory SQLite database. In IntelliJ: open the file and click ▶ next to the class name. In a terminal: `mvn test`.
- [ ] `mvn test` passes on **all six** machines.
- [ ] Each person reads the classes they own and the ones they will call, and confirms the fields and signatures. If something is wrong, fix it now, not in Part 2.
- [ ] Delete `SetupCheckTest` once everyone has run it.
- [x] The decisions in [contracts.md](contracts.md) are agreed.

---

## Part 2: Individual tasks

Each person does their two steps in order. Use mocks or fake classes in your tests for anyone else's unfinished code.

### Ezz: Factory

**Step 1: Donation types.** `FoodDonation`, the four food types (cooked, refrigerated, fresh, packaged), `MoneyDonation`, `DonationType`, `Unit`.

- [ ] Each type has the right description and expiry. Money has no expiry and no food fields.
- [ ] Base priorities: cooked 8, refrigerated 7, fresh 6, packaged 3, money 1.

**Step 2: `DonationFactory`.**

- [ ] Creates all five types, and rejects missing fields, amounts/quantities of zero or less, and bad pickup times, with clear error messages.
- [ ] `applyAddOns` wraps the donation in the add-on decorators in a fixed order, with no duplicates.
- [ ] Rebuilding a donation from the database keeps its ID, status and date, and skips new-donation checks.

### Resha: Decorator

**Step 1: `DonationDecorator` base.**

- [ ] Extends `Donation` and wraps another `Donation`.
- [ ] All getters/setters pass through to the wrapped donation, so the two never get out of sync.
- [ ] `unwrap()` and `collectAddOns()` work for one wrapper and for several stacked wrappers.

**Step 2: Four wrappers.** `HomePickup`, `RefrigeratedTransport`, `UrgentPriority`, `SpecialPackaging`.

- [ ] Each adds its text to the description and its priority bonus: +1, +1, +5, +0.
- [ ] Stacked wrappers keep the original type, expiry and ID and add up all bonuses.

Test with a small fake `Donation` subclass; you don't need Ezz's real types.

### Wael: Singleton and donation storage

**Step 1: `DatabaseConnection`.**

- [ ] Private constructor and `getInstance()`, so there is only one connection; `close()` closes it.
- [ ] Creates the schema with foreign keys on, the seeded coordinator, the five statuses and add-on storage.
- [ ] Running setup/reset twice doesn't create duplicate seed rows.

**Step 2: `DonationRepository`.** `save`, `findById`, `findAll`, `findByDonor`, `findByStatus`, `updateStatus`.

- [ ] Saves and loads all donation fields plus add-ons, using parameterized SQL.
- [ ] Loading uses the factory's rebuild method, so ID/status/date are kept.
- [ ] Two donors only see their own donations, and a failed save leaves nothing half-written.

Test with a temporary SQLite file and a mocked factory.

### Mark: Strategy

**Step 1: `NotificationService` and `NotificationStrategy`.**

- [ ] The service sends through whichever strategy is set.
- [ ] The strategy can be changed at runtime (`setStrategy`) or picked from the recipient's preference (`notifyByPreference`).
- [ ] A missing or invalid channel gives a clear, documented result.

**Step 2: Three channels.** `InAppNotification` (with `InAppInbox`), `EmailNotification`, `SmsNotification`.

- [ ] In-app messages are stored per recipient; nobody sees anyone else's messages.
- [ ] Email and SMS write recipient, subject and message to local log files.
- [ ] Changing the preference changes the channel used for the next message.

### Ashry: Observer

**Step 1: `DonationService` and lifecycle.** `DonationSubject`, `DonationObserver`, `DonationEvent`, `EventType`, `DonationService`, `ExpiryScheduler`.

- [ ] Observers can attach/detach. Events are sent only after a save or status update succeeds.
- [ ] Only coordinators can change status: Submitted → Approved/Rejected, Approved → Picked up, Picked up → Delivered. Rejecting needs a reason, and final statuses can't change.
- [ ] Expiry check (every 60 seconds and on demand) warns about food expiring within 48 hours, skips money/finished/expired donations, warns only once per donation, and stops cleanly.

**Step 2: Observers and app wiring.** `CoordinatorDashboardObserver`, `DonorNotificationObserver`, `VolunteerNotificationObserver`, `MessageTemplates`, `AppConfig`, `AppContext`, `app.properties`.

- [ ] New donation → coordinator and volunteers are notified. Status change/rejection → donor is notified once (with the reason). Near expiry → coordinator only.
- [ ] Observers send through `NotificationService` using each person's current preference.
- [ ] `AppContext` creates everything, registers the observers once, and starts/stops the scheduler.

Test with mocks for the factory, repositories and notification service, and a fake clock.

### Jana: Users and lookups

**Step 1: Users and login.** `User`, `Donor`, `Coordinator`, `DonorType`, `Role`, `UserRepository`, `AuthService`.

- [ ] Registration saves name, phone, email, address, donor type and channel. Email must be unique, and registration can't create a coordinator.
- [ ] Login by email works for donors and the seeded coordinator. Profile updates keep the same user and their channel preference.
- [ ] A user can be turned into a `Recipient` for Mark's notifications, and data is still there after reopening the database.

**Step 2: Lookup CRUD.** `FoodCategory`, `StorageType`, `DonationStatus`, `LookupEntry`, `LookupType`, `LookupRepository`.

- [ ] Add, view, edit and delete work for categories, storage types and statuses. Blank or duplicate names are rejected.
- [ ] The five system statuses can't be renamed or deleted, and a lookup used by a donation can't be deleted.
- [ ] Only coordinators can manage lookups; donors are refused.

Test with your own temporary SQLite database built from the shared schema. Insert test donations with plain SQL.

---

## Final check (all six)

- [ ] Replace mocks with the real classes and run the full scenario from [plan.md](plan.md).
- [ ] `mvn clean verify` passes.
- [ ] Class diagram matches the code, and each person can explain their part.
