import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Date;

import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;

public class Dashboard extends JFrame {
    private JPanel rightPanel;
    private JPanel leftPanel;
    private JPanel biometricsContainer;
    private Connection conn;
    private int memberId;
    private JPanel goalContainer;
    public Dashboard(Connection c, int memberId, String firstname, String lastname,
                     Date dob, String gender, String primPhone, String secPhone) {

        this.conn = c;
        this.memberId = memberId;

        setTitle("Dashboard - " + firstname + " " + lastname);
        setSize(700, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        setContentPane(content);

        //Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + firstname + " " + lastname);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutButton.setMargin(new Insets(1, 8, 1, 8));
        logoutButton.setPreferredSize(new Dimension(70, 25));
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> {
            this.dispose();
        });
        JPanel logoutWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        logoutWrapper.setOpaque(false);
        logoutWrapper.add(logoutButton);
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutWrapper, BorderLayout.EAST);
        content.add(topPanel, BorderLayout.NORTH);
        content.add(topPanel, BorderLayout.NORTH);

        //Left Panel
        leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JPanel personalInfoPanel = new JPanel(new GridBagLayout());
        personalInfoPanel.setBorder(BorderFactory.createTitledBorder("Personal Information"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        String[][] info = {
                {"Member ID:", String.valueOf(memberId)},
                {"First Name:", firstname},
                {"Last Name:", lastname},
                {"Date of Birth:", dob.toString()},
                {"Gender:", gender},
                {"Primary Phone:", primPhone},
                {"Secondary Phone:", secPhone}
        };
        for (int i = 0; i < info.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            personalInfoPanel.add(new JLabel(info[i][0]), gbc);
            gbc.gridx = 1;
            personalInfoPanel.add(new JLabel(info[i][1]), gbc);
        }
        leftPanel.add(personalInfoPanel, BorderLayout.NORTH);
        goalContainer = buildGoalPanel();
        leftPanel.add(goalContainer, BorderLayout.CENTER);
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        exitButton.addActionListener(e -> this.dispose());
        JButton updateGoalButton = new JButton("Update Goal");
        updateGoalButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateGoalButton.addActionListener(e -> {
            new Goal(c,memberId,this).setVisible(true);
        });
        leftPanel.add(updateGoalButton, BorderLayout.SOUTH);
        content.add(leftPanel, BorderLayout.WEST);

        //Right Panel
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //Book Appointment things

        JPanel appointmentPanel = new JPanel(new BorderLayout());
        appointmentPanel.setBorder(BorderFactory.createTitledBorder("Upcoming Appointments"));
        JTextArea appointmentTextArea = new JTextArea(8, 20);
        appointmentTextArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        appointmentTextArea.setLineWrap(true);
        appointmentTextArea.setWrapStyleWord(true);
        appointmentTextArea.setEditable(false);
        refreshAppointments(appointmentTextArea);
        JScrollPane appointmentScrollPane = new JScrollPane(appointmentTextArea);
        appointmentScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        appointmentPanel.add(appointmentScrollPane, BorderLayout.CENTER);
        JButton bookAppointmentBtn = new JButton("Book Appointment");
        bookAppointmentBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        bookAppointmentBtn.setMaximumSize(new Dimension(200, 30));
        bookAppointmentBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        bookAppointmentBtn.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Book an Appointment", true);
            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setSize(380, 300);
            dialog.setLocationRelativeTo(this);
            JPanel dialogContent = new JPanel(new BorderLayout(10, 10));
            dialogContent.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            datePanel.add(new JLabel("Select Date:"));
            JDateChooser dateChooser = new JDateChooser();
            dateChooser.setDateFormatString("yyyy-MM-dd");
            datePanel.add(dateChooser);
            dateChooser.getDateEditor().getUiComponent().setEnabled(false);
            dialogContent.add(datePanel, BorderLayout.NORTH);
            HashSet<LocalDate> availableDates = new HashSet<>();
            try {
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery("SELECT DISTINCT availDate FROM Availabilities WHERE memberID IS NULL");
                while (rs.next()) {
                    LocalDate date = rs.getDate("availDate").toLocalDate();
                    availableDates.add(date);
                }
            } catch (Exception exception) {
                System.out.println(exception);
            }
            JCalendar calendar = dateChooser.getJCalendar();
            repaintDays(availableDates, dateChooser);
            calendar.getMonthChooser().addPropertyChangeListener(evt -> repaintDays(availableDates, dateChooser));
            calendar.getYearChooser().addPropertyChangeListener(evt -> repaintDays(availableDates, dateChooser));
            DefaultListModel<String> listModel = new DefaultListModel<>();
            JList<String> appointmentList = new JList<>(listModel);
            JScrollPane scrollPane = new JScrollPane(appointmentList);
            dialogContent.add(scrollPane, BorderLayout.CENTER);
            dateChooser.addPropertyChangeListener("date", evt -> {
                listModel.clear();
                Date selectedUtilDate = dateChooser.getDate();
                if (selectedUtilDate == null) return;
                LocalDate selectedLocalDate = selectedUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                String dateStr = selectedLocalDate.toString();
                try {
                    Statement statement = c.createStatement();
                    ResultSet rs = statement.executeQuery("SELECT A.trainerID, T.firstname, T.lastname, A.startTime, A.endTime " +
                            "FROM Availabilities A " +
                            "JOIN Trainer T ON A.trainerID = T.trainerId " +
                            "WHERE A.availDate = '" + dateStr + "' AND A.memberID IS NULL");
                    boolean appAvail = false;
                    while (rs.next()) {
                        appAvail = true;
                        int trainerId = rs.getInt("trainerId");
                        String fname = rs.getString("firstname");
                        String lname = rs.getString("lastname");
                        String start = rs.getString("startTime");
                        String end = rs.getString("endTime");
                        listModel.addElement("Trainer: " + fname + " " + lname + " (ID: " + trainerId + ")" + "  | " + start + " - " + end);
                    }
                    if (!appAvail) {
                        listModel.addElement("No available appointments on this day.");
                    }


                } catch (Exception ex) {
                    System.out.println(ex);
                }
            });

            JButton confirmBtn = new JButton("Book Selected Slot");
            confirmBtn.addActionListener(ev -> {
                String selected = appointmentList.getSelectedValue();
                Date selectedUtilDate = dateChooser.getDate();

                if (selected == null || selectedUtilDate == null || selected.contains("No available")) {
                    JOptionPane.showMessageDialog(dialog, "Please select a slot to book.");
                    return;
                }
                try {
                    String[] parts = selected.split("\\|");
                    String trainerInfo = parts[0].trim();
                    String timeRange = parts[1].trim();
                    int idStart = trainerInfo.indexOf("(ID:") + 4;
                    int idEnd = trainerInfo.indexOf(")", idStart);
                    int trainerId = Integer.parseInt(trainerInfo.substring(idStart, idEnd).trim());
                    String[] times = timeRange.split(" - ");
                    String start = times[0];
                    String end = times[1];

                    LocalDate date = selectedUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    Statement stmt = conn.createStatement();
                    stmt.executeUpdate("UPDATE Availabilities SET memberId = " + memberId +
                            " WHERE trainerID = " + trainerId +
                            " AND availDate = '" + date + "'" +
                            " AND startTime = '" + start + "'" +
                            " AND endTime = '" + end + "';");


                    JOptionPane.showMessageDialog(dialog, "Appointment booked!");

                    refreshAppointments(appointmentTextArea);

                    dialog.dispose();

                } catch (Exception exc) {
                    System.out.println(exc);
                }
                dialog.dispose();
            });
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(confirmBtn);
            dialog.add(dialogContent, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });
        biometricsContainer = buildBiometrics(conn, memberId);
        rightPanel.add(appointmentPanel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(bookAppointmentBtn);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(biometricsContainer);
        content.add(rightPanel, BorderLayout.EAST);

        setVisible(true);
    }


    private void refreshAppointments(JTextArea appointmentTextArea) {
        appointmentTextArea.setText("");
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT A.availDate, A.startTime, A.endTime, T.firstname, T.lastname " +
                            "FROM Availabilities A " +
                            "JOIN Trainer T ON A.trainerID = T.trainerId " +
                            "WHERE A.memberId = " + memberId + " AND A.memberID IS NOT NULL " +
                            "ORDER BY A.availDate, A.startTime"
            );
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                String aDate = rs.getDate("availDate").toString();
                String aStart = rs.getTime("startTime").toString().substring(0, 5);
                String aEnd = rs.getTime("endTime").toString().substring(0, 5);
                String tName = rs.getString("firstname") + " " + rs.getString("lastname");

                appointmentTextArea.append("With " + tName + " | " + aDate + " | " + aStart + " - " + aEnd + "\n");
            }
            if (!hasRows) {
                appointmentTextArea.setText("No upcoming appointments.");
            }

        } catch (Exception ex) {
            appointmentTextArea.setText("Error loading appointments.");
            ex.printStackTrace();
        }
    }



    private JPanel buildBiometrics(Connection c, int memberId) {
        JPanel biometricsPanel = new JPanel(new GridLayout(7, 2, 8, 8));
        biometricsPanel.setBorder(BorderFactory.createTitledBorder("Current Biometrics"));

        try {
            Statement state = c.createStatement();
            ResultSet rs = state.executeQuery("SELECT * FROM Biometrics WHERE memberID=" + memberId + " ORDER BY date DESC LIMIT 1");
            if (rs.next()) {
                String[][] biometrics = {
                        {"Date Updated:", rs.getDate("date").toString()},
                        {"Height:", rs.getInt("height") + " cm"},
                        {"Weight:", rs.getInt("weight") + " kg"},
                        {"Heart Rate:", rs.getInt("heartrate") + " bpm"},
                        {"Body fat:", rs.getInt("bodyfat") + "%"},
                        {"BMI:", Double.toString(bmiCalc(rs.getInt("height"),rs.getInt("weight")))},
                        {"Blood Pressure:", rs.getInt("systolic") + "/" + rs.getInt("diastolic") + " mmHg"},
                };
                for (String[] row : biometrics) {
                    biometricsPanel.add(new JLabel(row[0]));
                    biometricsPanel.add(new JLabel(row[1]));
                }
            } else {
                biometricsPanel.add(new JLabel("No data available"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JButton updateBiometricsButton = new JButton("Update Biometrics");
        updateBiometricsButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        updateBiometricsButton.addActionListener(e -> {
            Biometrics x = new Biometrics(c, memberId, false, () -> {
                refreshBiometrics();
                refreshGoal();
            });
            x.setVisible(true);
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(biometricsPanel, BorderLayout.CENTER);
        panel.add(updateBiometricsButton, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshBiometrics() {
        rightPanel.remove(biometricsContainer);
        biometricsContainer = buildBiometrics(conn, memberId);
        rightPanel.add(biometricsContainer);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void repaintDays(HashSet<LocalDate> availableDates, JDateChooser dateChooser) {
        JCalendar calendar = dateChooser.getJCalendar();

        int year = calendar.getYearChooser().getYear();
        int month = calendar.getMonthChooser().getMonth();
        LocalDate currentMonth = LocalDate.of(year, month + 1, 1);

        Component[] dayButtons = calendar.getDayChooser().getDayPanel().getComponents();

        for (Component comp : dayButtons) {
            if (comp instanceof JButton dayButton) {
                try {
                    int day = Integer.parseInt(dayButton.getText());
                    LocalDate date = currentMonth.withDayOfMonth(day);

                    dayButton.setText(String.valueOf(day));

                    if (availableDates.contains(date)) {
                        dayButton.setForeground(Color.GREEN);
                        dayButton.setFont(dayButton.getFont().deriveFont(Font.BOLD));
                        dayButton.setToolTipText("Appointments available");
                    } else {
                        dayButton.setForeground(Color.BLACK);
                        dayButton.setFont(dayButton.getFont().deriveFont(Font.PLAIN));
                        dayButton.setToolTipText(null);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    protected JPanel buildGoalPanel() {
        JPanel goalPanel = new JPanel(new BorderLayout(10, 10));
        goalPanel.setBorder(BorderFactory.createTitledBorder("User Goal"));
        try {
            Statement s = conn.createStatement();
            ResultSet rs = s.executeQuery("SELECT * FROM Goal WHERE memberId = " + memberId + ";");
            Statement s1 = conn.createStatement();
            ResultSet rs1 = s1.executeQuery("SELECT weight, bodyfat, date FROM Biometrics WHERE memberId = " + memberId + " ORDER BY date DESC LIMIT 1");


            if (rs.next() && rs1.next()) {
                Statement s2 = conn.createStatement();
                ResultSet rs2 = s2.executeQuery("SELECT weight, bodyfat FROM Biometrics " +
                        "WHERE memberId = " + memberId + " AND date <= '" + rs.getDate("startDate") +
                        "' ORDER BY date DESC LIMIT 1");
                if(rs2.next()){
                    JPanel goalInfoPanel = new JPanel(new GridLayout(6, 2, 20, 5));
                    goalInfoPanel.add(new JLabel("Current Goal:"));
                    String goal = rs.getString("goaltype");
                    goalInfoPanel.add(new JLabel(goal));
                    int curWeight = rs1.getInt("weight");
                    int curBf = rs1.getInt("bodyfat");
                    int target = rs.getInt("targetval");
                    int initialBf = rs2.getInt("bodyfat");
                    int initialWeight = rs2.getInt("weight");
                    Date startDate = rs.getDate("startDate");
                    Date endDate = rs.getDate("endDate");
                    int progress = 0;
                    boolean goalFailed=false;

                    if (goal.contains("Weight")) {
                        progress=calculateProgress(initialWeight, curWeight, target);
                        goalInfoPanel.add(new JLabel("Current Weight:"));
                        goalInfoPanel.add(new JLabel(Integer.toString(curWeight)));
                        goalInfoPanel.add(new JLabel("Goal Weight:"));
                        goalInfoPanel.add(new JLabel(Integer.toString(target)));
                    }

                    if (goal.equals("Desired Body fat %")) {
                        progress = calculateProgress(initialBf, curBf, target);
                        goalInfoPanel.add(new JLabel("Current Body Fat:"));
                        goalInfoPanel.add(new JLabel(Integer.toString(curBf)));

                        goalInfoPanel.add(new JLabel("Goal Body Fat:"));
                        goalInfoPanel.add(new JLabel(Integer.toString(target)));
                    }
                    Date latestBioDate = rs1.getDate("date");
                    if ((new Date().after(endDate) || latestBioDate.after(endDate)) && progress < 100) {
                        progress = 0;
                        goalFailed = true;
                    }
                    goalInfoPanel.add(new JLabel("Start Date:"));
                    goalInfoPanel.add(new JLabel(startDate.toString()));

                    goalInfoPanel.add(new JLabel("End Date:"));
                    goalInfoPanel.add(new JLabel(endDate.toString()));


                    goalPanel.add(goalInfoPanel, BorderLayout.NORTH);

                    JProgressBar goalProgress = new JProgressBar(0, 100);
                    goalProgress.setValue(progress);
                    goalProgress.setString(progress+"% Complete");
                    goalProgress.setFont(new Font("SansSerif", Font.BOLD, 16));
                    goalProgress.setStringPainted(true);
                    if (goalFailed) {
                        goalProgress.setString("Goal Failed!");
                        goalProgress.setValue(0);
                    }

                    JPanel progressContainer = new JPanel(new BorderLayout(5, 5));
                    progressContainer.add(goalProgress, BorderLayout.SOUTH);

                    goalPanel.add(progressContainer, BorderLayout.CENTER);
                }
            }
            else{
                goalPanel.add(new JLabel("No goal yet.."), BorderLayout.CENTER);
            }
        } catch (Exception er) {
            er.printStackTrace();
        }

        return goalPanel;
    }
    public static int calculateProgress(int initialVal, int currentVal, int targetVal) {
        if (initialVal == targetVal) return 100;

        double totalDistance = Math.abs(targetVal - initialVal);
        double progressDistance = Math.abs(currentVal - initialVal);

        double progressRaw = progressDistance / totalDistance;

        int percent = (int) (progressRaw * 100);
        return Math.max(0, Math.min(percent, 100));
    }

    protected void refreshGoal() {
        leftPanel = (JPanel) getContentPane().getComponent(1);
        leftPanel.remove(goalContainer);
        goalContainer = buildGoalPanel();
        leftPanel.add(goalContainer, BorderLayout.CENTER);
        leftPanel.revalidate();
        leftPanel.repaint();
    }

    public static double bmiCalc(int height, int weight) {
        double dheight = height / 100.0;
        double bmi = weight / (dheight * dheight);
        return Math.round(bmi * 10.0) / 10.0;
    }
}