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

| Package | Contents |
| --- | --- |
| `app` | Start-up and wiring: `Launcher`, `FoodDonationApp`, `AppContext`, `AppConfig`, `ControllerFactory` |
| `model.donation`, `model.user`, `model.lookup`, `model.receipt` | Data classes (the Model in MVC) |
| `model.donation.addon` | Optional donation services |
| `service` | Business logic (`DonationService`, `AuthService`, `ReceiptService`, `ExpiryScheduler`) |
| `service.donationevent` | Donation events and observers |
| `notification` | Notification channels |
| `persistence` | Database connection and repositories |
| `controller` | One JavaFX controller per view (the Controller in MVC) |
| `ui` | GUI helpers: `Navigator`, `Session`, `View`, `Formats`, `Controls` |
| `resources/com/fooddonation/view` | FXML views and `styles.css` (the View in MVC) |

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
