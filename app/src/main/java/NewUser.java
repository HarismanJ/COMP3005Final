import javax.swing.*;
import java.awt.*;
import com.toedter.calendar.JDateChooser;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;

public class NewUser extends JFrame {
    public NewUser(Connection c) {
        setTitle("New Member Registration");
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

        //Static Member ID display
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Member ID:"), gbc);
        gbc.gridx = 1;
        int memberId = 1;
        try {
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery("SELECT memberid FROM member ORDER BY memberid DESC LIMIT 1");

            if (rs.next()) {
                memberId = rs.getInt("memberid") + 1;
            }
            rs.close();

        } catch (Exception e) {
            System.out.println(e);
        }
        JLabel memberIdLabel = new JLabel(Integer.toString(memberId));
        panel.add(memberIdLabel, gbc);

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

        //Date of Birth - JDateChooser
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1;
        JDateChooser dobChooser = new JDateChooser();
        dobChooser.getDateEditor().getUiComponent().setEnabled(false);
        panel.add(dobChooser, gbc);

        //Gender Dropdown
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Gender:"), gbc);
        gbc.gridx = 1;
        String[] genderOptions = {"Male", "Female", "Other"};
        JComboBox<String> genderComboBox = new JComboBox<>(genderOptions);
        genderComboBox.setSelectedIndex(0);
        panel.add(genderComboBox, gbc);

        //Primary Phone (Must be 10 digits)
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Primary Phone:"), gbc);
        gbc.gridx = 1;
        JTextField primaryPhoneField = new JTextField(20);
        panel.add(primaryPhoneField, gbc);

        //Secondary Phone
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Secondary Phone:"), gbc);
        gbc.gridx = 1;
        JTextField secondaryPhoneField = new JTextField(20);
        panel.add(secondaryPhoneField, gbc);


        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        //Submit Button
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton submitButton = new JButton("Submit");
        int finalMemberId = memberId;
        submitButton.addActionListener(e -> {
            if (new String(passwordField.getPassword()).isBlank() || firstNameField.getText().isBlank() || lastNameField.getText().isBlank()
                    || dobChooser.getDate()==null || !isValidNum(primaryPhoneField.getText())
                    || !isValidNum(secondaryPhoneField.getText())) {
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
                            lastNameField.getText() + "', '" + new SimpleDateFormat("yyyy-MM-dd").format(dobChooser.getDate()) + "', '" +
                            genderComboBox.getSelectedItem().toString() + "', '" +
                            primaryPhoneField.getText() + "', '" +
                            secondaryPhoneField.getText() + "'";
                    statement.executeUpdate("INSERT INTO member (password, firstname, lastname, dob, gender, primphone, secphone)" +
                            "VALUES ("+sqlInputString+");");
                    new Biometrics(c, finalMemberId, true, ()->{}).setVisible(true);
                    this.dispose();
                    JOptionPane.showMessageDialog(
                            null,
                            "Please Enter Initial Biometrics",
                            "User Registered",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> {
            new MainMenu(c).setVisible(true);
            this.dispose();
        });
        buttonPanel.add(submitButton);
        buttonPanel.add(exitButton);
        panel.add(buttonPanel, gbc);
        add(panel);
    }


    //Phone Number Verifier
    public static boolean isValidNum(String str) {
        if(str.length()!=10){return false;}
        try {
            BigInteger bigint = new BigInteger(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}
