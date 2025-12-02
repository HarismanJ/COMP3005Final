import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class AdminMenu extends JFrame{
    public AdminMenu(Connection c){
        setTitle("Fitness Club - Admin Login");
        setSize(300, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;


        gbc.gridx = 0;
        gbc.gridy = 0;

        //Admin ID Things
        panel.add(new JLabel("Admin ID:"), gbc);
        gbc.gridx = 1;
        ArrayList<Integer> adminIds = new ArrayList<>();
        try{
            Statement s = c.createStatement();
            ResultSet r = s.executeQuery("SELECT adminId FROM Admin");
            while(r.next()){
                adminIds.add(r.getInt("adminId"));
            }
        }catch (Exception ex){System.out.println(ex);}
        JComboBox<Integer> adminIdField = new JComboBox<>(adminIds.toArray(new Integer[0]));
        panel.add(adminIdField, gbc);

        //Password things
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        //Buttons
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton signInBtn = new JButton("Sign In");
        signInBtn.addActionListener(e->{
            try{
                Statement signin = c.createStatement();
                ResultSet rs = signin.executeQuery("SELECT * FROM Admin WHERE adminId="+adminIdField.getSelectedItem()+" AND " +
                        "password = '"+ new String(passwordField.getPassword())+"';");
                if(rs.next()){
                    new AdminDashboard(c).setVisible(true);
                }else{
                    JOptionPane.showMessageDialog(
                            null,
                            "Sign-in Failed",
                            "Sign-in Error",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

            }catch(Exception er){
                System.out.println(er);
            }
        });
        signInBtn.setMaximumSize(new Dimension(120, 30));
        panel.add(signInBtn, gbc);


        gbc.gridy = 3;
        JButton registerBtn = new JButton("New Admin");
        panel.add(registerBtn, gbc);
        registerBtn.addActionListener(e -> {
            new NewAdmin(c).setVisible(true);
            this.dispose();
        });


        gbc.gridy = 4;
        JButton exit = new JButton("Exit");
        panel.add(exit, gbc);
        exit.addActionListener(e -> {
            new MainMenu(c).setVisible(true);
            this.dispose();
        });

        add(panel);
    }

}
