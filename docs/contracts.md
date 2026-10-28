# Shared decisions (Part 1)

All six members agreed to every decision below. After Part 1, don't change these without telling the group.

The exact class list, fields, method signatures and database tables are in [phase1-tasks.md](phase1-tasks.md#part-1-shared-setup) (G3–G7).

## Decisions

- [x] **Status:** `Donation.status` is a `DonationStatus` lookup object. The five system statuses (Submitted, Approved, Rejected, Picked up, Delivered) are seeded with `is_system = 1` and can't be renamed or deleted. The lifecycle checks status by name using the constants in `DonationStatus`. Custom statuses can exist but never take part in the lifecycle.
- [x] **Add-ons in the database:** one `add_ons` text column holding comma-separated enum names, e.g. `HOME_PICKUP,URGENT_PRIORITY`. Empty means no add-ons.
- [x] **Add-on order:** `applyAddOns` always wraps in the order of the `AddOn` enum, and ignores duplicates.
- [x] **Coordinator-only actions:** every service/lookup method that changes data takes `User caller` as its first parameter and throws `SecurityException` if the caller isn't a coordinator.
- [x] **User → Recipient:** `User.toRecipient()` copies name, email, phone and preferred channel, and uses the user ID as the recipient ID. Volunteers from `app.properties` become `Recipient`s directly.
- [x] **Missing/invalid channel:** `notifyByPreference` falls back to `IN_APP` if the preference is missing or has no strategy. `notify` with no strategy set throws `IllegalStateException`.
- [x] **Expiry rule:** a donation expires at the end of its expiry day (timezone from `app.properties`). Warn about active food donations expiring within 48 hours. Skip money, Rejected, Delivered and already expired donations. Warn once per donation per program run. Check every 60 seconds and when asked.
- [x] **Database and threads:** repositories wrap each database operation in `synchronized (db) { ... }`, so the expiry scheduler and the main code never use the connection at the same time.
- [x] **Errors:** invalid input throws `IllegalArgumentException` with a readable message. Illegal status changes throw `IllegalStateException`.
- [x] **Testing:** collaborators come in through the constructor. Only `AppContext` calls `DatabaseConnection.getInstance()`.

## Done when

- [x] Every decision above is agreed.
- [ ] G1–G9 in [phase1-tasks.md](phase1-tasks.md) are all done.
