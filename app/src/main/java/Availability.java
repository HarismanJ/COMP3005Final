import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import com.toedter.calendar.JDateChooser;

public class Availability extends JDialog {

    public Availability(JFrame parent, int trainerId, Connection c, TrainerDashboard d) {
        super(parent, "Add Availability", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        //Date things
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Select Date:"), gbc);
        gbc.gridx = 1;
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.getDateEditor().getUiComponent().setEnabled(false);
        add(dateChooser, gbc);

        //Start Time things
        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("Start Time:"), gbc);
        gbc.gridx = 1;
        SpinnerDateModel startModel = new SpinnerDateModel();
        JSpinner startSpinner = new JSpinner(startModel);
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "HH:mm"));
        add(startSpinner, gbc);

        //End Time things
        gbc.gridy++;
        gbc.gridx = 0;
        add(new JLabel("End Time:"), gbc);
        gbc.gridx = 1;
        SpinnerDateModel endModel = new SpinnerDateModel();
        JSpinner endSpinner = new JSpinner(endModel);
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "HH:mm"));
        add(endSpinner, gbc);

        //Confirm
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton confirmBtn = new JButton("Add Availability");
        add(confirmBtn, gbc);
        confirmBtn.addActionListener(e -> {
            Date selectedDate = dateChooser.getDate();
            Date startTime = (Date) startSpinner.getValue();
            Date endTime = (Date) endSpinner.getValue();
            if (selectedDate == null || startTime == null || endTime == null) {
                JOptionPane.showMessageDialog(this, "Please select a date and time.", "Missing Date or Time", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Calendar dateCal = Calendar.getInstance();
            dateCal.setTime(selectedDate);
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startTime);
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endTime);
            LocalDate date = LocalDate.of(
                    dateCal.get(Calendar.YEAR),
                    dateCal.get(Calendar.MONTH) + 1,
                    dateCal.get(Calendar.DAY_OF_MONTH)
            );
            LocalTime start = LocalTime.of(
                    startCal.get(Calendar.HOUR_OF_DAY),
                    startCal.get(Calendar.MINUTE)
            );
            LocalTime end = LocalTime.of(
                    endCal.get(Calendar.HOUR_OF_DAY),
                    endCal.get(Calendar.MINUTE)
            );
            LocalDateTime startDateTime = LocalDateTime.of(date, start);
            LocalDateTime endDateTime = LocalDateTime.of(date, end);

            if (!endDateTime.isAfter(startDateTime)) {
                JOptionPane.showMessageDialog(this, "End time must be after start time.", "Invalid Time", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try{
                String dateString = date.toString();
                String startString = start.toString();
                String endString = end.toString();
                Statement st = c.createStatement();

                ResultSet rs = st.executeQuery("SELECT * FROM Availabilities " +
                        "WHERE trainerID = " + trainerId +
                        " AND availDate = '" + dateString + "'" +
                        " AND (startTime < '" + endString + "' AND endTime > '" + startString + "')");

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this,
                            "You already have an availability that overlaps with this time.",
                            "Overlap Detected",
                            JOptionPane.ERROR_MESSAGE);
                    rs.close();
                    st.close();
                    return;
                }
                rs.close();
                st.executeUpdate("INSERT INTO Availabilities (trainerID, availDate, startTime, endTime, roomAssigned, memberId) " +
                        "VALUES (" + trainerId + ", '" + date + "', '" + start + "', '" + end + "', FALSE, NULL);");

            } catch (Exception ex){System.out.println(ex);}

            JOptionPane.showMessageDialog(this,
                    "Availability added successfully:\n" +
                            "From: " + startDateTime + "\nTo:    " + endDateTime,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            d.refreshSessionPanel(c, trainerId);
            dispose();
        });

        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}