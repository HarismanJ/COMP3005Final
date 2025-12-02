import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Goal extends JFrame {

    public Goal(Connection c, int memberId, Dashboard d) {
        setTitle("Update Goal");
        setSize(350, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        //Inputs
        JLabel goalTypeLabel = new JLabel("Goal Type:");
        JComboBox<String> goalTypeBox = new JComboBox<>(new String[]{"Select", "Desired Weight", "Desired Body fat %"});
        JLabel targetLabel = new JLabel("Target:");
        JComboBox<String> targetBox = new JComboBox<>();
        JLabel startDateLabel = new JLabel("Start Date:");
        JDateChooser startDateChooser = new JDateChooser();
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        JLabel endDateLabel = new JLabel("End Date:");
        JDateChooser endDateChooser = new JDateChooser();
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        startDateChooser.setMinSelectableDate(new Date());
        endDateChooser.setMinSelectableDate(new Date());
        startDateChooser.getDateEditor().getUiComponent().setEnabled(false);
        endDateChooser.getDateEditor().getUiComponent().setEnabled(false);
        startDateChooser.getDateEditor().addPropertyChangeListener("date", e->{
            if(startDateChooser.getDate()!=null){
                endDateChooser.setMinSelectableDate(startDateChooser.getDate());
            }
        });

        //Goal Type Selector
        goalTypeBox.addActionListener(e -> {
            String choice = (String) goalTypeBox.getSelectedItem();
            targetBox.removeAllItems();
            if (choice.contains("Weight")) {
                for (int i = 40; i <= 180; i += 1) {
                    targetBox.addItem(i + " kg");
                }
            } else if (choice.equals("Desired Body fat %")) {
                for (int i = 0; i <= 60; i++) {
                    targetBox.addItem(i + "%");
                }
            }
        });

        inputPanel.add(goalTypeLabel);
        inputPanel.add(goalTypeBox);
        inputPanel.add(targetLabel);
        inputPanel.add(targetBox);
        inputPanel.add(startDateLabel);
        inputPanel.add(startDateChooser);
        inputPanel.add(endDateLabel);
        inputPanel.add(endDateChooser);

        //Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton submitButton = new JButton("Submit");
        JButton exitButton = new JButton("Exit");

        submitButton.addActionListener(e -> {
            String goal = (String) goalTypeBox.getSelectedItem();
            String target = (String) targetBox.getSelectedItem();
            Date start = startDateChooser.getDate();
            Date end = endDateChooser.getDate();
            if (goal == null || goal.equals("Select") || target == null || start==null || end==null) {
                JOptionPane.showMessageDialog(this,
                        "Please select both goal type and target.",
                        "Incomplete Selection",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                try{
                    int targetInt = Integer.parseInt(target.replaceAll("[^\\d-]", ""));
                    String startDateStr = new SimpleDateFormat("yyyy-MM-dd").format(startDateChooser.getDate());
                    String endDateStr = new SimpleDateFormat("yyyy-MM-dd").format(endDateChooser.getDate());
                    Statement st =c.createStatement();
                    st.executeUpdate("INSERT INTO Goal (memberId, goalType, targetVal, startDate, endDate) " +
                            "VALUES (" + memberId + ", '" + goal + "', " + targetInt + ", '" + startDateStr + "', '" + endDateStr + "') " +
                            "ON CONFLICT (memberId) DO UPDATE SET goalType = EXCLUDED.goalType, targetVal = EXCLUDED.targetVal, " +
                            "startDate = EXCLUDED.startDate, endDate= EXCLUDED.endDate;");

                }catch(Exception ex){ex.printStackTrace();}
                JOptionPane.showMessageDialog(this,
                        "Goal saved: " + goal + " → " + target,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                d.refreshGoal();
                this.dispose();
            }
        });
        exitButton.addActionListener(e -> this.dispose());
        buttonPanel.add(exitButton);
        buttonPanel.add(submitButton);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }
}