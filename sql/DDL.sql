-- DDL

DROP TABLE IF EXISTS RoomBooking CASCADE;
DROP TABLE IF EXISTS BillingLineItem CASCADE;
DROP TABLE IF EXISTS Billing CASCADE;
DROP TABLE IF EXISTS Room CASCADE;
DROP TABLE IF EXISTS Availabilities CASCADE;
DROP TABLE IF EXISTS Goal CASCADE;
DROP TABLE IF EXISTS Biometrics CASCADE;
DROP TABLE IF EXISTS Trainer CASCADE;
DROP TABLE IF EXISTS Member CASCADE;
DROP TABLE IF EXISTS Admin CASCADE;


CREATE TABLE IF NOT EXISTS Member(
  memberId SERIAL PRIMARY KEY,
  password TEXT NOT NULL,
  firstname TEXT NOT NULL,
  lastname TEXT NOT NULL,
  DOB DATE NOT NULL,
  gender TEXT NOT NULL,
  primPhone TEXT NOT NULL,
  secPhone TEXT
);

CREATE TABLE IF NOT EXISTS Biometrics(
  date DATE,
  memberId INTEGER references Member (memberId),
  PRIMARY KEY (memberId, date),
  height INTEGER NOT NULL,
  weight INTEGER NOT NULL,
  heartRate INTEGER NOT NULL,
  bodyFat INTEGER NOT NULL,
  systolic INTEGER,
  diastolic INTEGER
  
);

CREATE TABLE IF NOT EXISTS Goal(
  memberId INTEGER PRIMARY KEY REFERENCES Member(memberId),
  goalType TEXT NOT null,
  targetVal INTEGER NOT NULL,
  startDate DATE NOT NULL,
  endDate DATE
  
);

CREATE TABLE IF NOT EXISTS Trainer(
    trainerId SERIAL PRIMARY KEY,
    password TEXT NOT NULL,
    firstname TEXT NOT NULL,
    lastname TEXT NOT NULL,
    bio TEXT,
    phone TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS Availabilities(
	trainerID INTEGER REFERENCES Trainer(trainerId),
    availDate DATE NOT NULL,
    startTime TIME NOT NULL,
    endTime TIME NOT NULL,
    roomAssigned BOOLEAN,
    memberId INTEGER REFERENCES Member(memberId),
    PRIMARY KEY(trainerID, availDate, startTime, endTime)
);

CREATE TABLE IF NOT EXISTS Admin(
    adminId SERIAL PRIMARY KEY,
    password TEXT NOT NULL,
    firstname TEXT NOT NULL,
    lastname TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS Room (
    roomId SERIAL PRIMARY KEY,
    roomName TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS RoomBooking (
    trainerID INTEGER NOT NULL,
    availDate DATE NOT NULL,
    startTime TIME NOT NULL,
    endTime TIME NOT NULL,
    roomId INTEGER NOT NULL,
    
    PRIMARY KEY (trainerID, availDate, startTime, endTime),
    
    FOREIGN KEY (trainerID, availDate, startTime, endTime)
        REFERENCES Availabilities(trainerID, availDate, startTime, endTime)
        ON DELETE CASCADE,
        
    FOREIGN KEY (roomId) REFERENCES Room(roomId)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Billing (
    billId SERIAL PRIMARY KEY,
    memberId INTEGER NOT NULL REFERENCES Member(memberId),
    issueDate DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS BillingLineItem (
    lineItemId SERIAL PRIMARY KEY,
    billId INTEGER NOT NULL REFERENCES Billing(billId) ON DELETE CASCADE,
    description TEXT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    isPaid BOOLEAN NOT NULL DEFAULT FALSE
);

-- View: Checks Member's Unpaid Bills
CREATE OR REPLACE VIEW BillingStatusView AS
SELECT
    B.billId,
    B.memberId,
    B.issueDate,
    COUNT(L.lineItemId) FILTER (WHERE L.isPaid = false) AS unpaidItems,
    COUNT(L.lineItemId) FILTER (WHERE L.isPaid = false) = 0 AS isFullyPaid
FROM Billing B
JOIN BillingLineItem L ON B.billId = L.billId
GROUP BY B.billId, B.memberId, B.issueDate;

--Trigger: Sets room assigned to true when app. booked
CREATE OR REPLACE FUNCTION set_room_assigned_true()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE Availabilities
    SET roomAssigned = TRUE
    WHERE trainerID = NEW.trainerID
      AND availDate = NEW.availDate
      AND startTime = NEW.startTime
      AND endTime = NEW.endTime;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
CREATE TRIGGER trg_room_assigned
AFTER INSERT ON RoomBooking
FOR EACH ROW
EXECUTE FUNCTION set_room_assigned_true();

--Index: Speeds up queries that search for available appointment slots
CREATE INDEX IF NOT EXISTS idx_availabilities_open_slots 
ON Availabilities(availDate) 
WHERE memberId IS NULL;


