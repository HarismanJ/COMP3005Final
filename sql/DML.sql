-- Populating Member table
INSERT INTO Member (password, firstname, lastname, DOB, gender, primPhone, secPhone) VALUES 
('12345', 'Alice', 'Smith', '1990-05-15', 'Female', '1234567890', '0987654321'),
('password', 'Bob', 'Jones', '1985-08-20', 'Male', '1112223333', '2223334444'),
('abcde', 'Charlie', 'Brown', '1995-02-10', 'Male', '5556667777', '8889990000'),
('123456', 'Diana', 'Miller', '1988-12-01', 'Female', '4445556666', '7778889999');

-- Populating Trainer table
INSERT INTO Trainer (password, firstname, lastname, bio, phone, email) VALUES 
('trainer', 'John', 'Doe', 'Specializes in weight training and cardio.', '1231231234', 'john.doe@gym.com'),
('12345', 'Jane', 'Roe', 'Certified Yoga and Pilates instructor.', '3213214321', 'jane.roe@gym.com'),
('pass123', 'Mike', 'Tyson', 'Boxing and high intensity interval training.', '9876543210', 'mike.t@gym.com');

-- Populating Admin table
INSERT INTO Admin (password, firstname, lastname) VALUES 
('admin', 'Sarah', 'Connor'),
('12345', 'Kyle', 'Reese'),
('password', 'Tony', 'Stark');

-- Populating Room table
INSERT INTO Room (roomName) VALUES 
('Studio A'),
('Studio B'),
('Studio C'),
('Yoga Room'),
('Cardio Room'),
('Weights Room'),
('Pool Area');

-- Populating Biometrics table
INSERT INTO Biometrics (date, memberId, height, weight, heartRate, bodyFat, systolic, diastolic) VALUES 
('2023-10-01', 1, 165, 60, 70, 25, 120, 80),
('2023-10-05', 2, 180, 90, 65, 20, 118, 78),
('2023-10-10', 3, 175, 80, 75, 18, 125, 82),
('2023-11-01', 1, 165, 58, 68, 24, 119, 79);

-- Populating Goal table
INSERT INTO Goal (memberId, goalType, targetVal, startDate, endDate) VALUES 
(1, 'Desired Weight', 55, '2023-01-01', '2023-06-01'),
(2, 'Desired Body Fat %', 15, '2023-02-01', '2023-12-31'),
(3, 'Desired Weight', 75, '2023-03-01', '2023-09-01');

-- Populating Availabilities table
INSERT INTO Availabilities (trainerID, availDate, startTime, endTime, roomAssigned, memberId) VALUES 
(1, '2024-01-10', '09:00:00', '10:00:00', FALSE, 1),
(1, '2024-01-10', '10:00:00', '11:00:00', FALSE, NULL),
(2, '2024-01-10', '14:00:00', '15:00:00', FALSE, 2),
(3, '2024-01-11', '08:00:00', '09:00:00', FALSE, NULL),
(2, '2024-01-12', '16:00:00', '17:00:00', FALSE, 3);

-- Populating RoomBooking table
INSERT INTO RoomBooking (trainerID, availDate, startTime, endTime, roomId) VALUES 
(1, '2024-01-10', '09:00:00', '10:00:00', 6),
(2, '2024-01-10', '14:00:00', '15:00:00', 4),
(2, '2024-01-12', '16:00:00', '17:00:00', 4);

-- Populating Billing table
INSERT INTO Billing (memberId, issueDate) VALUES 
(1, '2023-12-01'),
(2, '2023-12-05'),
(3, '2023-12-10');

-- Populating BillingLineItem table
INSERT INTO BillingLineItem (billId, description, amount, isPaid) VALUES 
(1, 'Monthly Membership Fee', 100.00, TRUE),
(1, 'Personal Training Session', 50.00, FALSE),
(2, 'Monthly Membership Fee', 100.00, TRUE),
(2, 'Locker Rental Fee', 15.00, TRUE),
(3, 'Monthly Membership Fee', 100.00, FALSE),
(3, 'Late Cancellation Fee', 25.00, FALSE);