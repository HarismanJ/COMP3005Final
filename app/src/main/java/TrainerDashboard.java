import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TrainerDashboard extends JFrame {

    private JPanel sessionPanel;
    private JPanel memberPanel;

    public TrainerDashboard(int trainerId, String firstname, String lastname, String phone, String email, String bio, Connection c) {
        setTitle("Trainer Dashboard - " + firstname + " " + lastname);
        setSize(640, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));
        setContentPane(content);

        //Top Section
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + firstname + " " + lastname);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        logoutButton.setPreferredSize(new Dimension(80, 30));
        logoutButton.addActionListener(e -> {
            this.dispose();
        });
        JPanel logoutPart = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPart.add(logoutButton);
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutPart, BorderLayout.EAST);
        content.add(topPanel, BorderLayout.NORTH);

        //Left Section
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        //Personal Info Panel
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Personal Info"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        String[][] info = {
                {"Trainer ID:", String.valueOf(trainerId)},
                {"First Name:", firstname},
                {"Last Name:", lastname},
                {"Phone:", phone},
                {"Email:", email}
        };
        for (int i = 0; i < info.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            infoPanel.add(new JLabel(info[i][0]), gbc);
            gbc.gridx = 1;
            infoPanel.add(new JLabel(info[i][1]), gbc);
        }

        //Biography Panel
        JPanel bioPanel = new JPanel(new BorderLayout());
        bioPanel.setBorder(BorderFactory.createTitledBorder("About me"));
        JTextArea bioArea = new JTextArea(4, 20);
        bioArea.setLineWrap(true);
        bioArea.setWrapStyleWord(true);
        bioArea.setEditable(false);
        bioArea.setText(bio);
        bioPanel.add(new JScrollPane(bioArea), BorderLayout.CENTER);

        //Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        JButton addAvailabilityBtn = new JButton("Add Availability");
        JButton hostGroupSessionBtn = new JButton("Search for User");
        addAvailabilityBtn.addActionListener(e -> {
            new Availability(this, trainerId,c, TrainerDashboard.this);

        });
        hostGroupSessionBtn.addActionListener(e -> {
            showMember(c);
        });
        buttonPanel.add(addAvailabilityBtn);
        buttonPanel.add(hostGroupSessionBtn);

        Box leftPanelContent = Box.createVerticalBox();
        leftPanelContent.add(infoPanel);
        leftPanelContent.add(Box.createVerticalStrut(15));
        leftPanelContent.add(bioPanel);
        leftPanelContent.add(Box.createVerticalStrut(15));
        leftPanelContent.add(buttonPanel);
        leftPanel.add(leftPanelContent, BorderLayout.NORTH);
        content.add(leftPanel, BorderLayout.WEST);

        //Right Panel
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sessionPanel = buildSessionPanel(c,trainerId);
        memberPanel =  buildMemberPanel();
        rightPanel.add(sessionPanel);
        rightPanel.add(memberPanel);
        content.add(rightPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    //Reloads trainer session after change
    public void refreshSessionPanel(Connection c, int trainerId) {
        if (sessionPanel != null) {
            getContentPane().remove(sessionPanel);
        }
        sessionPanel = buildSessionPanel(c, trainerId);
        ((JPanel) getContentPane().getComponent(2)).remove(0);
        ((JPanel) getContentPane().getComponent(2)).add(sessionPanel, 0);
        revalidate();
        repaint();
    }

    private JPanel buildSessionPanel(Connection c, int trainerId) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Upcoming Appointments"));
        try {
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT availDate, startTime, endTime " +
                            "FROM Availabilities " +
                            "WHERE trainerId = " + trainerId + " AND memberId IS NOT NULL " +
                            "ORDER BY availDate, startTime"
            );

            DefaultListModel<String> sessionListModel = new DefaultListModel<>();
            while (rs.next()) {
                String date = rs.getString("availDate");
                String start = rs.getString("startTime").substring(0, 5);
                String end = rs.getString("endTime").substring(0, 5);
                sessionListModel.addElement("Date: " + date + " | Time: " + start + " - " + end);
            }
            if (sessionListModel.isEmpty()) {
                sessionListModel.addElement("No upcoming appointments");
            }
            JList<String> sessionList = new JList<>(sessionListModel);
            panel.add(new JScrollPane(sessionList), BorderLayout.CENTER);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return panel;
    }

    //Searches for member first or last, returns multiple
    private void showMember(Connection conn) {
        try {
            String searchInput = JOptionPane.showInputDialog(this, "Enter member name (first, last, or full):");
            if (searchInput == null || searchInput.trim().isEmpty()) {
                return;
            }
            String searchTerm = searchInput.trim().toLowerCase();

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT memberId, firstname, lastname FROM Member");
            java.util.List<String> matches = new java.util.ArrayList<>();
            java.util.Map<String, Integer> memberMap = new java.util.HashMap<>();

            while (rs.next()) {
                int id = rs.getInt("memberId");
                String first = rs.getString("firstname");
                String last = rs.getString("lastname");
                String fullName = first + " " + last;
                if (first.toLowerCase().contains(searchTerm)
                        || last.toLowerCase().contains(searchTerm)
                        || fullName.toLowerCase().contains(searchTerm)) {
                    matches.add(fullName);
                    memberMap.put(fullName, id);
                }
            }

            if (matches.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No matching members found.");
                return;
            }

            String selectedName;
            if (matches.size() == 1) {
                selectedName = matches.get(0);
            } else {
                selectedName = (String) JOptionPane.showInputDialog(
                        this,
                        "Multiple matches, select one:",
                        "Matches Found",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        matches.toArray(),
                        matches.get(0)
                );
                if (selectedName == null || !memberMap.containsKey(selectedName)) return;
            }
            int selectedMemberId = memberMap.get(selectedName);


            DefaultListModel<String> listModel = new DefaultListModel<>();
            listModel.addElement("Name: " + selectedName);
            Statement goalSt = conn.createStatement();
            ResultSet goalRs = goalSt.executeQuery(
                    "SELECT goaltype, targetval, startDate, endDate " +
                            "FROM Goal WHERE memberId = " + selectedMemberId + " LIMIT 1"
            );
            if (goalRs.next()) {
                listModel.addElement("Goal: " + goalRs.getString("goaltype") + " → " + goalRs.getInt("targetval"));
                listModel.addElement("Goal Period: " +
                        goalRs.getDate("startDate") + " to " + goalRs.getDate("endDate"));
            } else {
                listModel.addElement("No active goal.");
            }
            Statement bioSt = conn.createStatement();
            ResultSet bioRs = bioSt.executeQuery(
                    "SELECT * FROM Biometrics WHERE memberId = " + selectedMemberId + " " +
                            "ORDER BY date DESC LIMIT 1"
            );
            if (bioRs.next()) {
                listModel.addElement("Latest Biometrics:");
                listModel.addElement("↳ Date: " + bioRs.getDate("date"));
                listModel.addElement("↳ Weight: " + bioRs.getInt("weight") + " kg");
                listModel.addElement("↳ Heart Rate: " + bioRs.getInt("heartrate") + " bpm");
                listModel.addElement("↳ Body Fat: " + bioRs.getInt("bodyfat") + " %");
            } else {
                listModel.addElement("No biometrics found.");
            }

            memberPanel.removeAll();
            memberPanel.setBorder(BorderFactory.createTitledBorder("Selected Member Details"));
            JList<String> resultList = new JList<>(listModel);
            resultList.setEnabled(false);
            memberPanel.add(new JScrollPane(resultList), BorderLayout.CENTER);
            memberPanel.revalidate();
            memberPanel.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private JPanel buildMemberPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Selected User Details"));

        DefaultListModel<String> classListModel = new DefaultListModel<>();
        classListModel.addElement("Please Search for a User");
        JList<String> classList = new JList<>(classListModel);
        panel.add(new JScrollPane(classList), BorderLayout.CENTER);

        return panel;
    }
}