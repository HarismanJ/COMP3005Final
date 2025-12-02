import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class NewAdmin extends JFrame {
    public NewAdmin(Connection c) {
        setTitle("New Admin Registration");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 300);
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

        //Member ID Things
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Admin ID:"), gbc);
        gbc.gridx = 1;
        int adminId = 1;
        try {
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery("SELECT adminId FROM Admin ORDER BY adminId DESC LIMIT 1");

            if (rs.next()) {
                adminId = rs.getInt("adminId") + 1;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        JLabel adminIdLabel = new JLabel(Integer.toString(adminId));
        panel.add(adminIdLabel, gbc);

        //Password
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


        //First Name
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

        //Last Name
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Last Name:"), gbc);

        gbc.gridx = 1;
        JTextField lastNameField = new JTextField(20);
        panel.add(lastNameField, gbc);


        //Buttons
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton submitButton = new JButton("Submit");
        int finalMemberId = adminId;
        submitButton.addActionListener(e -> {
            if (new String(passwordField.getPassword()).isBlank() || firstNameField.getText().isBlank() || lastNameField.getText().isBlank()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Incomplete or Inaccurate Information",
                        "Registration Error",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                try {
                    Statement statement = c.createStatement();
                    String sqlInputString = "'" + (new String(passwordField.getPassword())) + "', '" + firstNameField.getText() + "', '" +
                            lastNameField.getText() + "'";
                    statement.executeUpdate("INSERT INTO Admin (password, firstname, lastname)" +
                            "VALUES (" + sqlInputString + ");");
                    this.dispose();
                    new AdminMenu(c).setVisible(true);
                    JOptionPane.showMessageDialog(
                            null,
                            "Welcome to Harisman's Gym!",
                            "Admin Registered",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception e1) {
                    System.out.println(e1);
                }
            }
        });
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            new AdminMenu(c).setVisible(true);
            this.dispose();
        });
        buttonPanel.add(submitButton);
        buttonPanel.add(exitButton);

        panel.add(buttonPanel, gbc);

        add(panel);
    }
}
