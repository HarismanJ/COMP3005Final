# Harisman Jeyakanthan

**Student ID:** 101325107

---

## Objective

Implement a PostgreSQL database using the provided schema and write an application in Java that connects to this database to perform specific CRUD (Create, Read, Update, Delete) operations for a Gym Management System managing Members, Trainers, and Admins.

---

## Database Schema

### Member Table

| Field      | Type    | Constraints     |
|------------|---------|-----------------|
| memberId   | SERIAL  | PRIMARY KEY     |
| password   | TEXT    | NOT NULL        |
| firstname  | TEXT    | NOT NULL        |
| lastname   | TEXT    | NOT NULL        |
| dob        | DATE    | NOT NULL        |
| gender     | TEXT    | NOT NULL        |
| primPhone  | TEXT    | NOT NULL        |
| secPhone   | TEXT    | *(Optional)*    |

> Additional tables include: `Trainer`, `Admin`, `Biometrics`, `Goal`, `Availabilities`, `Room`, `RoomBooking`, `Billing`, and `BillingLineItem`.  
> All are created and initialized automatically when the program launches.

---

## Initial Data

```sql
INSERT INTO Member (password, firstname, lastname, DOB, gender, primPhone, secPhone)
VALUES 
('12345', 'Alice', 'Smith', '1990-05-15', 'Female', '1234567890', '0987654321');
```

Seeded automatically on program startup.

---

## Project Overview

### Key Files

- `Main.java` – Entry point. Connects to the database and initializes tables and data.
- `MainMenu.java` – Login screen and landing menu.
- `NewUser.java`, `NewTrainer.java`, `NewAdmin.java` – Handle user registration for each role.
- `Dashboard.java` – A member's dashboard for viewing personal info, appointments, biometrics, and goals.
- `Biometrics.java` – Member health tracking input form.
- `Goal.java` – Member goal-setting form and progress tracker.
- `TrainerDashboard.java` – Trainer view for availability and member data.
- `Availability.java` – Used by trainers to set available time slots.
- `AdminDashboard.java` – Used by admins to assign rooms, issue bills, and track payments.

---

## Implemented Functions

### Member Functions

| Function             | Description |
|---------------------|-------------|
| Register            | Create a new member profile with login details |
| Set Health Metrics  | Log current weight, blood pressure, body fat, etc. |
| Set Fitness Goal    | Define weight or body fat % target with a time frame |
| Dashboard           | View personal details, goal progress, recent biometrics |
| Book Session        | Book a personal training slot from available times |

---

### Trainer Functions

| Function         | Description |
|------------------|-------------|
| Set Availability | Define session timing; prevents time overlaps |
| View Schedule    | View booked sessions with members |
| Search Members   | Look up members and view their stats and goals |

---

### Admin Functions

| Function             | Description |
|----------------------|-------------|
| Assign Rooms         | Assign rooms to booked sessions using availability data |
| Billing              | Create bills and add services |
| Payments             | Mark line items as paid |
| Billing Status View  | Uses SQL VIEW to assess unpaid bills |

---

## Running the Program

1. Ensure PostgreSQL is running on your system.
2. In `Main.java`, update the following lines if needed:

```java
String url = "jdbc:postgresql://localhost:5432/postgres";
String user = "postgres";
String password = "your_password";
```

3. Run `Main.java`:
   - Creates all necessary tables
   - Populates the database with sample data
   - Builds Views and Triggers

---

## Using the Program

After launching:

- A GUI login screen will appear.
- Choose your role: Member, Trainer, or Admin.
- Register using the appropriate "New Account" button.
- After logging in, you’ll be shown a personalized dashboard based on your role.

### Use-Flow by Role:

- **Member**:
  - View up-to-date health stats
  - Set and track personal goals (weight/body fat)
  - Book personal training sessions with scheduled trainers

- **Trainer**:
  - Add available time slots
  - View upcoming appointments
  - Look up members by name and view their goals

- **Admin**:
  - Assign rooms to sessions
  - Create and issue bills to members
  - Mark line items as paid and view payment progress

---

## Demo Video

(https://youtu.be/-vwLYiF4MIA)

---

**End of README**
