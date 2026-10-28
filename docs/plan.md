# Phase 1 plan

We work in two parts:

1. **Part 1: Shared setup.** One shared base project that everyone builds on: structure, classes, method signatures and database.
2. **Part 2: Individual work.** Each person builds their own design pattern at the same time as everyone else.

At the end we meet once more to connect everything and run the demo (see "Final check" below).

Task details are in [phase1-tasks.md](phase1-tasks.md), the decisions we agreed on are recorded in [contracts.md](contracts.md), and progress is tracked in [todo.md](todo.md).

## Part 1: Shared setup

**Goal:** a project that compiles and contains every class we need. Methods can be empty at this point. After Part 1, nobody has to wait for anyone else.

**Jana set up the project** (G1–G7): repository files, `pom.xml`, the full package structure, all enums, data classes and shared method signatures, plus the schema, seed data and `app.properties`. Everyone else's part of Part 1 is to review it:

1. Clone the repository and open it in IntelliJ (or run `mvn test`). It must build on your machine.
2. Read the classes you own and the classes you will call. Check the fields and method signatures match what you need.
3. Report anything wrong to the group **before** Part 2 starts. Changes are cheap now and expensive later.

Full details in [phase1-tasks.md](phase1-tasks.md#part-1-shared-setup).

**Part 1 is done when:** the setup is pushed to `main`, `mvn test` passes on all six machines, and everyone has confirmed their classes and signatures.

## Part 2: Individual work (everyone at the same time)

| Person | Pattern | Builds | Reviewer |
| --- | --- | --- | --- |
| Ezz | Factory | The five donation types and `DonationFactory` | Resha |
| Resha | Decorator | The decorator base and the four add-on wrappers | Ezz |
| Wael | Singleton | The database connection and the donation repository | Jana |
| Mark | Strategy | The notification service and the in-app, email and SMS channels | Ashry |
| Ashry | Observer | `DonationService`, the donation lifecycle, expiry checks and the observers | Mark |
| Jana | (users/lookups) | Registration, login, profiles and lookup add/edit/delete | Wael |

Rules during Part 2:

- **Don't change a method name or parameters agreed in Part 1** without telling the group and fixing everyone's code that uses it.
- If you need someone else's class and it isn't finished yet, use a mock (Mockito) or a small fake class **in your tests**. Never copy their class into your own code.
- **Create donations only through `DonationFactory`.** The concrete donation constructors are package-private, so code outside `model.donation` can't call them.
- Write tests for your own part. Tests go in `src/test/java/com/fooddonation/`, in the same package as the code they test. Code goes in `src/main/java/com/fooddonation/`.
- Work on your own branch and merge through a pull request approved by your reviewer (table above).
- Only Wael edits the schema/seed files. Only Ezz edits `pom.xml`. Ask them if you need a change.

## Final check (all six)

Once everyone is done, we replace the mocks with the real classes and run one full scenario:

create a donation (Factory) → add services (Decorator) → save and reload it (Singleton) → status change sends events (Observer) → notification goes out on the chosen channel (Strategy).

Also check rejection, changing the notification channel, protected lookups and expiry warnings. Run `mvn clean verify` and check the class diagram against the code together.

## Scope decisions

- Five fixed statuses: Submitted, Approved, Rejected, Picked up, Delivered. Rejected and Delivered are final. Rejecting needs a reason. Custom statuses added by coordinators do not change the lifecycle.
- Units are fixed (not editable lookups).
- Money donations are stored only; there is no real payment.
- Email and SMS are written to log files; nothing is really sent.
- Volunteers are listed in `app.properties`.
- No UI work and no Phase 2 patterns in this phase. The structure is ready for MVC: the `model` packages are the Model, and views and controllers (`controller/`) come later.
