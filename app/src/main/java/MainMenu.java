import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class MainMenu extends JFrame{
    public MainMenu(Connection c){
        setTitle("Fitness Club - Member Login");
        setSize(300, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); //Padding around border
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); //Padding between items
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Member ID things
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Member ID:"), gbc);
        gbc.gridx = 1;
        ArrayList<Integer> memberIds = new ArrayList<>();
        try{
            Statement s = c.createStatement();
            ResultSet r = s.executeQuery("SELECT memberId FROM Member");
            while(r.next()){
                memberIds.add(r.getInt("memberId"));
            }
            r.close();
        }catch (Exception ex){ex.printStackTrace();}

        JComboBox<Integer> memberIdField = new JComboBox<>(memberIds.toArray(new Integer[0]));
        panel.add(memberIdField, gbc);

        //Password Things
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        // Sign In things
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
                ResultSet rs = signin.executeQuery("SELECT * FROM Member WHERE memberId="+memberIdField.getSelectedItem()+" AND " +
                        "password = '"+ new String(passwordField.getPassword())+"';");
                if(rs.next()){
                    new Dashboard(c, rs.getInt("memberId"), rs.getString("firstname"), rs.getString("lastname"),
                            rs.getDate("dob"), rs.getString("gender"), rs.getString("primphone"), rs.getString("secphone"));
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

        // Register things
        gbc.gridy = 3;
        JButton registerBtn = new JButton("New User");
        panel.add(registerBtn, gbc);
        registerBtn.addActionListener(e -> {
            new NewUser(c).setVisible(true);
            this.dispose();
        });

        //Trainer option
        gbc.gridy = 4;
        JButton trainer = new JButton("I'm a Trainer");
        trainer.addActionListener(e->{
            new TrainerMenu(c).setVisible(true);
            this.dispose();
        });
        panel.add(trainer, gbc);

        //Admin option
        gbc.gridy = 5;
        JButton admin = new JButton("I'm an Admin");
        admin.addActionListener(e->{
            new AdminMenu(c).setVisible(true);
            this.dispose();
        });
        panel.add(admin, gbc);

        add(panel);
    }

}
