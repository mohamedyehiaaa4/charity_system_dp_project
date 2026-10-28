# charity_system_dp_project

Food donation system for CSE336 (Software Design Patterns).

- Build and test: `mvn test`
- Plan and tasks: [docs/plan.md](docs/plan.md), [docs/phase1-tasks.md](docs/phase1-tasks.md)

## Design patterns

Packages are named by what the code does. This is where each pattern lives:

| Pattern | Where |
| --- | --- |
| Factory | `model/donation/DonationFactory` |
| Decorator | `addon/` (`DonationDecorator` and the four add-on wrappers) |
| Singleton | `persistence/DatabaseConnection` |
| Observer | `event/` + `service/DonationService` (the subject) |
| Strategy | `notification/` (`NotificationStrategy`, `NotificationService`) |

## Packages

| Package | Contents |
| --- | --- |
| `model.donation`, `model.user`, `model.lookup` | Data classes (the Model in MVC) |
| `addon` | Optional donation services |
| `notification` | Notification channels |
| `event` | Donation events and observers |
| `service` | Business logic (`DonationService`, `AuthService`, `ExpiryScheduler`) |
| `persistence` | Database connection and repositories |
| `app` | Wiring everything together |
