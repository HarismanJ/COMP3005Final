import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;
import java.util.Date;
import java.text.SimpleDateFormat;
public class Biometrics extends JFrame {
    private Runnable onSubmitCallback; //Used to communicate with Dashboard Class

    public Biometrics(Connection c, int memberId, boolean initial, Runnable onSubmitCallback) {
        setTitle("Update Biometrics");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        //Logged Date
        JLabel dateLabel = new JLabel("Date:");
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        JDateChooser dateChooser = new JDateChooser(new Date());
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setMinSelectableDate(new Date());
        dateChooser.getDateEditor().getUiComponent().setEnabled(false);
        datePanel.add(dateChooser);

        //Height
        JLabel heightLabel = new JLabel("Height (cm):");
        JComboBox<Integer> heightBox = new JComboBox<>();
        for (int i = 100; i <= 220; i++) heightBox.addItem(i);
        heightBox.setSelectedItem(180);

        //Weight
        JLabel weightLabel = new JLabel("Weight (kg):");
        JComboBox<Integer> weightBox = new JComboBox<>();
        for (int i = 40; i <= 180; i++) weightBox.addItem(i);
        weightBox.setSelectedItem(80);

        //Heart rate -- Just Tracking Not for goal Setting
        JLabel heartRateLabel = new JLabel("Resting Heart Rate (bpm):");
        JComboBox<Integer> heartRateBox = new JComboBox<>();
        for (int i = 40; i <= 200; i++) heartRateBox.addItem(i);
        heartRateBox.setSelectedItem(60);


        //Body Fat
        JLabel bodyFatLabel = new JLabel("Body Fat (%):");
        JComboBox<Integer> bodyFatBox = new JComboBox<>();
        for (int i = 0; i <= 60; i++) bodyFatBox.addItem(i);
        bodyFatBox.setSelectedItem(20);

        //Blood Pressure (Systolic & Diastolic) -- Just Tracking Not for goal Setting
        JLabel systolicLabel = new JLabel("Blood Pressure - Systolic:");
        JComboBox<Integer> systolicBox = new JComboBox<>();
        for (int i = 80; i <= 200; i++) systolicBox.addItem(i);
        systolicBox.setSelectedItem(120);
        JLabel diastolicLabel = new JLabel("Blood Pressure - Diastolic:");
        JComboBox<Integer> diastolicBox = new JComboBox<>();
        for (int i = 50; i <= 130; i++) diastolicBox.addItem(i);
        diastolicBox.setSelectedItem(80);

        // Submit button
        JButton submitButton = new JButton("Save Biometrics");
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        this.onSubmitCallback = onSubmitCallback;


        submitButton.addActionListener(e -> {
            try {
                Statement statement = c.createStatement();
                Date selectedDate = dateChooser.getDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateSQL = sdf.format(selectedDate);
                int height = (int) heightBox.getSelectedItem();
                int weight = (int) weightBox.getSelectedItem();
                int heartRate = (int) heartRateBox.getSelectedItem();
                int bodyFat = (int) bodyFatBox.getSelectedItem();
                int systolic = (int) systolicBox.getSelectedItem();
                int diastolic = (int) diastolicBox.getSelectedItem();
                statement.executeUpdate("INSERT INTO Biometrics " +
                        "(date, memberId, height, weight, heartRate, bodyFat, systolic, diastolic) VALUES (" +
                        "'" + dateSQL + "', " +
                        memberId + ", " +
                        height + ", " +
                        weight + ", " +
                        heartRate + ", " +
                        bodyFat + ", " +
                        systolic + ", " +
                        diastolic + ");");
                JOptionPane.showMessageDialog(this, "Biometrics saved.");
                if (initial) {
                    new MainMenu(c).setVisible(true);
                }
                if (onSubmitCallback != null) {
                    onSubmitCallback.run();
                }
                this.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Biometrics cannot be updated more than once per day", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        formPanel.add(dateLabel);
        formPanel.add(datePanel);
        formPanel.add(heightLabel);
        formPanel.add(heightBox);
        formPanel.add(weightLabel);
        formPanel.add(weightBox);
        formPanel.add(heartRateLabel);
        formPanel.add(heartRateBox);
        formPanel.add(bodyFatLabel);
        formPanel.add(bodyFatBox);
        formPanel.add(systolicLabel);
        formPanel.add(systolicBox);
        formPanel.add(diastolicLabel);
        formPanel.add(diastolicBox);
        add(formPanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        if (!initial) {
            exitButton.addActionListener(e -> {this.dispose();});
            bottomPanel.add(exitButton);
        }
        bottomPanel.add(submitButton);
        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

}