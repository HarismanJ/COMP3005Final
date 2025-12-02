import javax.swing.*;
import java.awt.*;
import com.toedter.calendar.JDateChooser;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class NewTrainer extends JFrame {
    public NewTrainer(Connection c) {
        setTitle("New Trainer Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 450);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 10));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 30, 8, 30);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        //Member ID Label
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Trainer ID:"), gbc);
        gbc.gridx = 1;
        int trainerId = 1;
        try {
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery("SELECT trainerid FROM trainer ORDER BY trainerid DESC LIMIT 1");

            if (rs.next()) {
                trainerId = rs.getInt("trainerid") + 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        JLabel trainerIdLabel = new JLabel(Integer.toString(trainerId));
        panel.add(trainerIdLabel, gbc);

        //Password Input
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);


        //First Name Input
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JTextField firstNameField = new JTextField(20);
        panel.add(firstNameField, gbc);

        //Last Name Input
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        JTextField lastNameField = new JTextField(20);
        panel.add(lastNameField, gbc);


        //Primary Phone Input
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        JTextField primaryPhoneField = new JTextField(20);
        panel.add(primaryPhoneField, gbc);

        //Email Input
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        JTextField email = new JTextField(20);
        panel.add(email, gbc);

        //Biography Input
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Bio:"), gbc);
        gbc.gridx = 1;
        JTextField bio= new JTextField(20);
        panel.add(bio, gbc);



        //Buttons
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> {
            if (new String(passwordField.getPassword()).isBlank() || firstNameField.getText().isBlank() || lastNameField.getText().isBlank()
                    || bio.getText().isBlank() || !isValidNum(primaryPhoneField.getText())
                    || email.getText().isBlank() || !email.getText().contains("@")) {
                JOptionPane.showMessageDialog(
                        null,
                        "Incomplete or Inaccurate Information",
                        "Registration Error",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                try {
                    Statement statement = c.createStatement();
                    String sqlInputString = "'"+(new String(passwordField.getPassword()))+"', '"+ firstNameField.getText()+"', '" +
                            lastNameField.getText() + "', '" + bio.getText() + "', '" +
                            primaryPhoneField.getText() + "', '" +
                            email.getText() + "'";
                    statement.executeUpdate("INSERT INTO trainer (password, firstname, lastname, bio, phone, email)" +
                            "VALUES ("+sqlInputString+");");
                    this.dispose();
                    new TrainerMenu(c).setVisible(true);
                    JOptionPane.showMessageDialog(
                            null,
                            "Welcome to Harisman's Gym!",
                            "Trainer Registered",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception e1) {
                    System.out.println(e1);
                }
            }
        });
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            new TrainerMenu(c).setVisible(true);
            this.dispose();
        });
        buttonPanel.add(submitButton);
        buttonPanel.add(exitButton);
        panel.add(buttonPanel, gbc);
        add(panel);
    }


    public static boolean isValidNum(String str) { //Checks if phone number is 10 digits
        if(str.length()!=10){return false;}
        try {
            BigInteger bigint = new BigInteger(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}
