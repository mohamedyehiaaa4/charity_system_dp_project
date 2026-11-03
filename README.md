# charity_system_dp_project

Food donation system for CSE336 (Software Design Patterns).

- Needs JDK 17 or 21 and Maven 3.9+
- Build and test: `mvn test`
- Start the GUI: `mvn javafx:run`, or run `com.fooddonation.app.Launcher` in IntelliJ. Sign in with `coordinator@fooddonation.org`, or register a donor
- Docs: [requirements](docs/requirements/), [Phase 1 decisions](docs/phase1/contracts.md), [class diagrams](docs/diagrams/)

## Design patterns

Packages are named by what the code does. This is where each pattern lives:

| Pattern | Where |
| --- | --- |
| Factory | `model/donation/DonationFactory` |
| Decorator | `model/donation/addon/` (`DonationDecorator` and the four add-on wrappers) |
| Singleton | `persistence/DatabaseConnection` |
| Observer | `service/donationevent/` + `service/DonationService` (the subject) |
| Strategy | `notification/` (`NotificationStrategy`, `NotificationService`) |

## Packages

| Package | MVC role | Contents |
| --- | --- | --- |
| `app` | (wiring) | Start-up and wiring: `Launcher`, `FoodDonationApp`, `AppContext`, `AppConfig`, `ControllerFactory` |
| `model.donation`, `model.user`, `model.lookup`, `model.receipt` | Model | Domain objects |
| `model.donation.addon` | Model | Optional donation services |
| `service` | Model | Business logic (`DonationService`, `AuthService`, `ReceiptService`, `LookupService`, `ExpiryScheduler`) |
| `service.donationevent` | Model | Donation events and observers |
| `notification` | Model | Notification channels |
| `persistence` | Model | Database connection and repositories |
| `controller` | Controller | One JavaFX controller per view, plus `Navigator` (switches screens) and `Session` (who is signed in) |
| `view` | View | View helpers: `View` (the screen list), `Formats`, `Controls` |
| `resources/com/fooddonation/view` | View | FXML views and `styles.css` |

The Model is everything that runs without the GUI, so it covers four packages, not only `model`. They are split by layer: domain objects (`model`), business logic (`service`), storage (`persistence`) and outgoing messages (`notification`). None of them imports JavaFX.

## GUI

| Screen | What it tests |
| --- | --- |
| Sign in / Register | DON-1 registration and login |
| Donor › Submit a donation | Factory (five types), Decorator (extra services), DON-3 food name and pickup address |
| Donor › My donations | DON-8 status list, DON-10 receipts and annual summary |
| Donor and coordinator › Inbox | Observer events delivered through the in-app Strategy |
| Donor and coordinator › Profile | Changing the notification channel at runtime (Strategy) |
| Coordinator › Donations | DON-5 lifecycle (approve, reject with reason, picked up, delivered), near-expiry check |
| Coordinator › Lookup tables | DON-7 CRUD, protected system rows |
| Coordinator › Email and SMS log | What the simulated email and SMS channels sent |
