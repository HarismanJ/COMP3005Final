import java.math.BigInteger;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

//Comment your code!!! Hard to keep track without comments

//https://app.eraser.io/workspace/Fw65GU8sym01b6HvblNM?origin=share&elements=48bnJs5ncLGeoqilgsNSTg

//Break Testing/Code Adjusting
//Main -- DONE
//MainMenu -- DONE
//NewUser -- DONE
//Biometrics -- DONE
//Goal -- DONE
//TrainerMenu -- DONE
//NewTrainer -- DONE
//TrainerDashboard -- DONE
//Availability -- DONE
//AdminMenu -- DONE
//NewAdmin -- DONE
//AdminDashboard -- DONE
//Dashboard -- DONE

//Functionality
//Sign-Up (User, Trainer, Admin) -- GOOD
//Member Functionality -- (User Goal, Biometrics Update, Book Appointments) --




/*
● Set Availability: Define time windows when available for sessions or classes. Prevent overlap.
● Schedule View: See assigned PT sessions and classes.
● Member Lookup: Search by name (case-insensitive) and view current goal and last metric. No
editing rights.

 */


public class Main {
    public static void main(String[] args) {

        String url = "jdbc:postgresql://localhost:5432/postgres";
        String user = "postgres";
        String password = "harisman";

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            createTables(connection);
            new MainMenu(connection).setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createTables(Connection c) {
        try (Statement statement = c.createStatement()) {

            // Dropping Tables
            statement.executeUpdate("DROP TABLE IF EXISTS RoomBooking CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS BillingLineItem CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS Billing CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS Room CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS Availabilities CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS Goal CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS Biometrics CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS Trainer CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS Member CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS Admin CASCADE");

            // Creating Tables
            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Member (" +
                            "memberId SERIAL PRIMARY KEY, " +
                            "password TEXT NOT NULL, " +
                            "firstname TEXT NOT NULL, " +
                            "lastname TEXT NOT NULL, " +
                            "DOB DATE NOT NULL, " +
                            "gender TEXT NOT NULL, " +
                            "primPhone TEXT NOT NULL, " +
                            "secPhone TEXT)"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Biometrics (" +
                            "date DATE, " +
                            "memberId INTEGER REFERENCES Member(memberId), " +
                            "PRIMARY KEY (memberId, date), " +
                            "height INTEGER NOT NULL, " +
                            "weight INTEGER NOT NULL, " +
                            "heartRate INTEGER NOT NULL, " +
                            "bodyFat INTEGER NOT NULL, " +
                            "systolic INTEGER, " +
                            "diastolic INTEGER)"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Goal (" +
                            "memberId INTEGER PRIMARY KEY REFERENCES Member(memberId), " +
                            "goalType TEXT NOT NULL, " +
                            "targetVal INTEGER NOT NULL, " +
                            "startDate DATE NOT NULL, " +
                            "endDate DATE)"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Trainer (" +
                            "trainerId SERIAL PRIMARY KEY, " +
                            "password TEXT NOT NULL, " +
                            "firstname TEXT NOT NULL, " +
                            "lastname TEXT NOT NULL, " +
                            "bio TEXT, " +
                            "phone TEXT NOT NULL, " +
                            "email TEXT NOT NULL UNIQUE)"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Availabilities (" +
                            "trainerID INTEGER REFERENCES Trainer(trainerId), " +
                            "availDate DATE NOT NULL, " +
                            "startTime TIME NOT NULL, " +
                            "endTime TIME NOT NULL, " +
                            "roomAssigned BOOLEAN, " +
                            "memberId INTEGER REFERENCES Member(memberId), " +
                            "PRIMARY KEY (trainerID, availDate, startTime, endTime))"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Admin (" +
                            "adminId SERIAL PRIMARY KEY, " +
                            "password TEXT NOT NULL, " +
                            "firstname TEXT NOT NULL, " +
                            "lastname TEXT NOT NULL)"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Room (" +
                            "roomId SERIAL PRIMARY KEY, " +
                            "roomName TEXT NOT NULL UNIQUE)"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS RoomBooking (" +
                            "trainerID INTEGER NOT NULL, " +
                            "availDate DATE NOT NULL, " +
                            "startTime TIME NOT NULL, " +
                            "endTime TIME NOT NULL, " +
                            "roomId INTEGER NOT NULL, " +
                            "PRIMARY KEY (trainerID, availDate, startTime, endTime), " +
                            "FOREIGN KEY (trainerID, availDate, startTime, endTime) " +
                            "REFERENCES Availabilities(trainerID, availDate, startTime, endTime) ON DELETE CASCADE, " +
                            "FOREIGN KEY (roomId) REFERENCES Room(roomId) ON DELETE CASCADE)"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Billing (" +
                            "billId SERIAL PRIMARY KEY, " +
                            "memberId INTEGER NOT NULL REFERENCES Member(memberId), " +
                            "issueDate DATE NOT NULL DEFAULT CURRENT_DATE)"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS BillingLineItem (" +
                            "lineItemId SERIAL PRIMARY KEY, " +
                            "billId INTEGER NOT NULL REFERENCES Billing(billId) ON DELETE CASCADE, " +
                            "description TEXT NOT NULL, " +
                            "amount DECIMAL(10,2) NOT NULL, " +
                            "isPaid BOOLEAN NOT NULL DEFAULT FALSE)"
            );

            // View
            statement.executeUpdate(
                    "CREATE OR REPLACE VIEW BillingStatusView AS " +
                            "SELECT B.billId, B.memberId, B.issueDate, " +
                            "COUNT(L.lineItemId) FILTER (WHERE L.isPaid = false) AS unpaidItems, " +
                            "COUNT(L.lineItemId) FILTER (WHERE L.isPaid = false) = 0 AS isFullyPaid " +
                            "FROM Billing B " +
                            "JOIN BillingLineItem L ON B.billId = L.billId " +
                            "GROUP BY B.billId, B.memberId, B.issueDate"
            );

            // Trigger Function
            statement.executeUpdate(
                    "CREATE OR REPLACE FUNCTION set_room_assigned_true() RETURNS TRIGGER AS $$ " +
                            "BEGIN " +
                            "UPDATE Availabilities SET roomAssigned = TRUE " +
                            "WHERE trainerID = NEW.trainerID " +
                            "AND availDate = NEW.availDate " +
                            "AND startTime = NEW.startTime " +
                            "AND endTime = NEW.endTime; " +
                            "RETURN NEW; " +
                            "END; $$ LANGUAGE plpgsql"
            );

            // Trigger
            statement.executeUpdate(
                    "CREATE TRIGGER trg_room_assigned " +
                            "AFTER INSERT ON RoomBooking " +
                            "FOR EACH ROW EXECUTE FUNCTION set_room_assigned_true()"
            );

            // Index
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_availabilities_open_slots " +
                            "ON Availabilities(availDate) WHERE memberId IS NULL"
            );

            // Populating Member table
            statement.executeUpdate("INSERT INTO Member (password, firstname, lastname, DOB, gender, primPhone, secPhone) VALUES " +
                    "('12345', 'Alice', 'Smith', '1990-05-15', 'Female', '1234567890', '0987654321'), " +
                    "('password', 'Bob', 'Jones', '1985-08-20', 'Male', '1112223333', '2223334444'), " +
                    "('abcde', 'Charlie', 'Brown', '1995-02-10', 'Male', '5556667777', '8889990000'), " +
                    "('123456', 'Diana', 'Miller', '1988-12-01', 'Female', '4445556666', '7778889999')");

            // Populating Trainer table
            statement.executeUpdate("INSERT INTO Trainer (password, firstname, lastname, bio, phone, email) VALUES " +
                    "('trainer', 'John', 'Doe', 'Specializes in weight training and cardio.', '1231231234', 'john.doe@gym.com'), " +
                    "('12345', 'Jane', 'Roe', 'Certified Yoga and Pilates instructor.', '3213214321', 'jane.roe@gym.com'), " +
                    "('pass123', 'Mike', 'Tyson', 'Boxing and high intensity interval training.', '9876543210', 'mike.t@gym.com')");

            // Populating Admin table
            statement.executeUpdate("INSERT INTO Admin (password, firstname, lastname) VALUES " +
                    "('admin', 'Sarah', 'Connor'), " +
                    "('12345', 'Kyle', 'Reese'), " +
                    "('password', 'Tony', 'Stark')");

            // Populating Room table
            statement.executeUpdate("INSERT INTO Room (roomName) VALUES " +
                    "('Studio A'), ('Studio B'), ('Studio C'), ('Yoga Room'), " +
                    "('Cardio Room'), ('Weights Room'), ('Pool Area')");

            // Populating Biometrics table
            statement.executeUpdate("INSERT INTO Biometrics (date, memberId, height, weight, heartRate, bodyFat, systolic, diastolic) VALUES " +
                    "('2023-10-01', 1, 165, 60, 70, 25, 120, 80), " +
                    "('2023-10-05', 2, 180, 90, 65, 20, 118, 78), " +
                    "('2023-10-10', 3, 175, 80, 75, 18, 125, 82), " +
                    "('2023-11-01', 1, 165, 58, 68, 24, 119, 79)");

            // Populating Goal table
            statement.executeUpdate("INSERT INTO Goal (memberId, goalType, targetVal, startDate, endDate) VALUES " +
                    "(1, 'Desired Weight', 55, '2023-01-01', '2023-06-01'), " +
                    "(2, 'Desired Body Fat %', 15, '2023-02-01', '2023-12-31'), " +
                    "(3, 'Desired Weight', 75, '2023-03-01', '2023-09-01')");

            // Populating Availabilities table
            statement.executeUpdate("INSERT INTO Availabilities (trainerID, availDate, startTime, endTime, roomAssigned, memberId) VALUES " +
                    "(1, '2024-01-10', '09:00:00', '10:00:00', FALSE, 1), " +
                    "(1, '2024-01-10', '10:00:00', '11:00:00', FALSE, NULL), " +
                    "(2, '2024-01-10', '14:00:00', '15:00:00', FALSE, 2), " +
                    "(3, '2024-01-11', '08:00:00', '09:00:00', FALSE, NULL), " +
                    "(2, '2024-01-12', '16:00:00', '17:00:00', FALSE, 3)");

            // Populating RoomBooking table
            statement.executeUpdate("INSERT INTO RoomBooking (trainerID, availDate, startTime, endTime, roomId) VALUES " +
                    "(1, '2024-01-10', '09:00:00', '10:00:00', 6), " +
                    "(2, '2024-01-10', '14:00:00', '15:00:00', 4), " +
                    "(2, '2024-01-12', '16:00:00', '17:00:00', 4)");

            // Populating Billing table
            statement.executeUpdate("INSERT INTO Billing (memberId, issueDate) VALUES " +
                    "(1, '2023-12-01'), " +
                    "(2, '2023-12-05'), " +
                    "(3, '2023-12-10')");

            // Populating BillingLineItem table
            statement.executeUpdate("INSERT INTO BillingLineItem (billId, description, amount, isPaid) VALUES " +
                    "(1, 'Monthly Membership Fee', 100.00, TRUE), " +
                    "(1, 'Personal Training Session', 50.00, FALSE), " +
                    "(2, 'Monthly Membership Fee', 100.00, TRUE), " +
                    "(2, 'Locker Rental Fee', 15.00, TRUE), " +
                    "(3, 'Monthly Membership Fee', 100.00, FALSE), " +
                    "(3, 'Late Cancellation Fee', 25.00, FALSE)");

            System.out.println("Database reset and populated successfully.");

        } catch (Exception e) {
            System.out.println("Error creating tables:");
            e.printStackTrace();
        }
    }
}