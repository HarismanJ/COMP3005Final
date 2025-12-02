import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class AdminDashboard extends JFrame {
    JComboBox<String> lineItemBox = new JComboBox<>();
    Map<String, Integer> lineItemMap = new HashMap<>();
    Map<String, Integer> billMap = new HashMap<>();
    public AdminDashboard(Connection c) {
        setTitle("Admin Dashboard");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top Panle
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, Admin");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setPreferredSize(new Dimension(100, 30));
        logoutBtn.addActionListener(e -> dispose());
        JPanel logoutWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutWrapper.add(logoutBtn);
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutWrapper, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        //Left Panel
        JPanel roomPanel = buildRoomBookingPanel(c);

        //Right Panel
        JPanel billPanel = buildBillingPanel(c);
        JPanel paymentPanel = buildPaymentPanel(c);

        centerPanel.add(roomPanel);
        centerPanel.add(billPanel);
        centerPanel.add(paymentPanel);
        add(centerPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel buildRoomBookingPanel(Connection conn) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Assign Room to PT Session"));

        JComboBox<String> sessionBox = new JComboBox<>();
        JComboBox<String> roomBox = new JComboBox<>();
        Map<String, String> sessionMap = new HashMap<>();
        Map<String, Integer> roomMap = new HashMap<>();

        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT A.trainerID, A.availDate, A.startTime, A.endTime, " +
                            "T.firstname AS tName, M.firstname AS mName " +
                            "FROM Availabilities A " +
                            "JOIN Trainer T ON A.trainerID = T.trainerId " +
                            "JOIN Member M ON A.memberId = M.memberId " +
                            "WHERE A.memberId IS NOT NULL AND A.roomAssigned = FALSE " +
                            "ORDER BY A.availDate, A.startTime"
            );
            while (rs.next()) {
                String label = "Trainer ID: " + rs.getInt("trainerId") + " | " + rs.getString("availDate") +
                        " | " + rs.getString("startTime").substring(0, 5) +
                        " - " + rs.getString("endTime").substring(0, 5) +
                        " | Trainer: " + rs.getString("tName") +
                        ", Member: " + rs.getString("mName");
                sessionBox.addItem(label);
                String key = rs.getInt("trainerId") + "|" +
                        rs.getString("availDate") + "|" +
                        rs.getString("startTime") + "|" +
                        rs.getString("endTime");

                sessionMap.put(label, key);
            }
            rs = st.executeQuery("SELECT roomId, roomName FROM Room ORDER BY roomName");
            while (rs.next()) {
                roomBox.addItem(rs.getString("roomName"));
                roomMap.put(rs.getString("roomName"), rs.getInt("roomId"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JButton assignBtn = new JButton("Assign Room");
        assignBtn.addActionListener(e->{
            try{
                String selectedLabel = (String) sessionBox.getSelectedItem();
                String selectedRoom = (String) roomBox.getSelectedItem();
                if (selectedLabel == null || selectedRoom == null || selectedLabel.startsWith("Error")) {
                    JOptionPane.showMessageDialog(panel, "Please select a valid session and room.");
                    return;
                }
                String key = sessionMap.get(selectedLabel);
                if (key == null) {
                    JOptionPane.showMessageDialog(panel, "Session key is missing or invalid.");
                    return;
                }
                String[] parts = key.split("\\|");
                int trainerId = Integer.parseInt(parts[0]);
                String date = parts[1];
                String startTime = parts[2];
                String endTime = parts[3];

                int roomId = roomMap.get(selectedRoom);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery( "SELECT 1 FROM RoomBooking WHERE " +
                        "roomId = " + roomId +
                        " AND availDate = '" + date + "'" +
                        " AND startTime < '" + endTime + "'" +
                        " AND endTime > '" + startTime + "'");
                if (rs.next()) {
                    JOptionPane.showMessageDialog(panel, "This room is already booked at this time.");
                    return;
                }
                stmt.executeUpdate("INSERT INTO RoomBooking (trainerID, availDate, startTime, endTime, roomId) " +
                        "VALUES (" + trainerId + ", '" + date + "', '" + startTime + "', '" + endTime + "', " + roomId + ")");

                JOptionPane.showMessageDialog(panel, "Room assigned successfully!");
                sessionMap.remove(selectedLabel);
                sessionBox.removeItem(selectedLabel);
            } catch (Exception ex){System.out.println(ex);}
        });
        JLabel sessionLabel = new JLabel("Select Session:");
        sessionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sessionBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel roomLabel = new JLabel("Select Room:");
        roomLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        roomBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        assignBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(sessionLabel);
        panel.add(sessionBox);
        panel.add(Box.createVerticalStrut(10));
        panel.add(roomLabel);
        panel.add(roomBox);
        panel.add(Box.createVerticalStrut(10));
        panel.add(assignBtn);

        return panel;
    }


    private JPanel buildBillingPanel(Connection conn) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Issue New Bill"));

        JComboBox<String> memberBox = new JComboBox<>();
        Map<String, Integer> memberMap = new HashMap<>();

        Map<String, Double> serviceCatalog = new HashMap<>();
        serviceCatalog.put("Personal Training Session", 50.00);
        serviceCatalog.put("Group Yoga Class", 20.00);
        serviceCatalog.put("Monthly Membership Fee", 100.00);
        serviceCatalog.put("Locker Rental Fee", 15.00);
        serviceCatalog.put("Late Cancellation Fee", 25.00);
        serviceCatalog.put("Massage Therapy", 70.00);
        serviceCatalog.put("Swimming Pool Access", 30.00);
        JComboBox<String> serviceBox = new JComboBox<>();
        for (String service : serviceCatalog.keySet()) {
            serviceBox.addItem(service);
        }

        JTextArea billSummaryArea = new JTextArea(5, 25);
        billSummaryArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(billSummaryArea);

        java.util.List<String> lineItems = new java.util.ArrayList<>();
        java.util.List<Double> lineAmounts = new java.util.ArrayList<>();


        JButton addBtn = new JButton("Add Service");
        JButton clearBtn = new JButton("Clear");
        JButton issueBtn = new JButton("Issue Bill");
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT memberId, firstname, lastname FROM Member");
            while (rs.next()) {
                String label = rs.getString("firstname") + " " + rs.getString("lastname");
                int id = rs.getInt("memberId");
                memberBox.addItem(label);
                memberMap.put(label, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        addBtn.addActionListener(e -> {
            String selectedService = (String) serviceBox.getSelectedItem();
            if (selectedService != null && serviceCatalog.containsKey(selectedService)) {
                double amount = serviceCatalog.get(selectedService);
                lineItems.add(selectedService);
                lineAmounts.add(amount);
                billSummaryArea.append(selectedService + " - $" + String.format("%.2f", amount) + "\n");
            }
        });
        clearBtn.addActionListener(e -> {
            billSummaryArea.setText("");
            lineItems.clear();
            lineAmounts.clear();
        });
        issueBtn.addActionListener(e -> {
            try {
                String selected = (String) memberBox.getSelectedItem();
                if (selected == null || !memberMap.containsKey(selected)) {
                    JOptionPane.showMessageDialog(panel, "Please select a member.");
                    return;
                }
                if (lineItems.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "No services added to bill.");
                    return;
                }
                int memberId = memberMap.get(selected);

                //Use view here
                Statement checkStmt = conn.createStatement();
                ResultSet unpaid = checkStmt.executeQuery(
                        "SELECT 1 FROM BillingStatusView WHERE memberId = " + memberId + " AND isFullyPaid = false"
                );

                if (unpaid.next()) {
                    JOptionPane.showMessageDialog(panel, "This member has an unpaid bill. Please settle it before creating a new bill.");
                    return;
                }

                Statement stmt = conn.createStatement();
                stmt.executeUpdate(
                        "INSERT INTO Billing (memberId) " +
                                "VALUES (" + memberId +")",
                        Statement.RETURN_GENERATED_KEYS
                );
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int billId = keys.getInt(1);
                    for (int i = 0; i < lineItems.size(); i++) {
                        String desc = lineItems.get(i);
                        double amt = lineAmounts.get(i);
                        stmt.executeUpdate(
                                "INSERT INTO BillingLineItem (billId, description, amount) VALUES (" +
                                        billId + ", '" + desc.replace("'", "''") + "', " + amt +")"
                        );
                    }
                    JOptionPane.showMessageDialog(panel, "Bill issued successfully.");
                    refreshPaymentDropdown(conn);
                    lineItems.clear();
                    lineAmounts.clear();
                    billSummaryArea.setText("");
                    serviceBox.setSelectedIndex(0);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JLabel memberLabel = new JLabel("Select Member:");
        JLabel serviceLabel = new JLabel("Select Service:");
        JLabel summaryLabel = new JLabel("Services Added:");
        memberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        serviceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        summaryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        memberBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        serviceBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        issueBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(memberLabel);
        panel.add(memberBox);
        panel.add(Box.createVerticalStrut(10));
        panel.add(serviceLabel);
        panel.add(serviceBox);
        panel.add(Box.createVerticalStrut(10));
        panel.add(addBtn);
        panel.add(Box.createVerticalStrut(5));
        panel.add(clearBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(summaryLabel);
        panel.add(scrollPane);
        panel.add(Box.createVerticalStrut(10));
        panel.add(Box.createVerticalStrut(10));
        panel.add(issueBtn);
        return panel;
    }

    private JPanel buildPaymentPanel(Connection conn) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Pay Bill"));
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT L.lineItemId, L.billId, L.description, L.amount, " +
                            "M.firstname, M.lastname " +
                            "FROM BillingLineItem L " +
                            "JOIN Billing B ON L.billId = B.billId " +
                            "JOIN Member M ON B.memberId = M.memberId " +
                            "WHERE L.isPaid = FALSE " +
                            "ORDER BY B.billId, L.lineItemId"
            );
            while (rs.next()) {
                int lineItemId = rs.getInt("lineItemId");
                int billId = rs.getInt("billId");
                String desc = rs.getString("description");
                double amount = rs.getDouble("amount");
                String memberName = rs.getString("firstname") + " " + rs.getString("lastname");
                String label = "Bill #" + billId + " | " + desc + " ($" + String.format("%.2f", amount) + ") - " + memberName;
                lineItemBox.addItem(label);
                lineItemMap.put(label, lineItemId);
                billMap.put(label, billId);
            }
            if (lineItemBox.getItemCount() == 0) {
                lineItemBox.addItem("No unpaid items available");
                lineItemBox.setEnabled(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JButton payBtn = new JButton("Mark as Paid");
        payBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        payBtn.addActionListener(e -> {
            String selected = (String) lineItemBox.getSelectedItem();
            if (selected == null || !lineItemMap.containsKey(selected)) {
                JOptionPane.showMessageDialog(panel, "Please select a valid unpaid line item.");
                return;
            }
            int lineItemId = lineItemMap.get(selected);

            try {
                Statement stmt = conn.createStatement();
                int affected = stmt.executeUpdate("UPDATE BillingLineItem SET isPaid = TRUE WHERE lineItemId = " + lineItemId);
                if (affected > 0) {
                    JOptionPane.showMessageDialog(panel, "Line item paid successfully!");
                    lineItemMap.remove(selected);
                    billMap.remove(selected);
                    lineItemBox.removeItem(selected);
                    if (lineItemBox.getItemCount() == 0) {
                        lineItemBox.addItem("No unpaid items available");
                        lineItemBox.setEnabled(false);
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, "Update failed.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Error updating payment.");
            }
        });

        JLabel lineItemLabel = new JLabel("Select Unpaid Line Item:");
        lineItemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lineItemBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lineItemLabel);
        panel.add(lineItemBox);
        panel.add(Box.createVerticalStrut(10));
        panel.add(payBtn);

        return panel;
    }


    private void refreshPaymentDropdown(Connection conn) {
        lineItemBox.removeAllItems();
        lineItemMap.clear();
        billMap.clear();
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(
                    "SELECT L.lineItemId, L.billId, L.description, L.amount, " +
                            "M.firstname, M.lastname " +
                            "FROM BillingLineItem L " +
                            "JOIN Billing B ON L.billId = B.billId " +
                            "JOIN Member M ON B.memberId = M.memberId " +
                            "WHERE L.isPaid = FALSE " +
                            "ORDER BY B.billId, L.lineItemId"
            );
            int count = 0;
            while (rs.next()) {
                int lineItemId = rs.getInt("lineItemId");
                int billId = rs.getInt("billId");
                String desc = rs.getString("description");
                double amount = rs.getDouble("amount");
                String memberName = rs.getString("firstname") + " " + rs.getString("lastname");

                String label = "[" + memberName + "] Bill #" + billId + " | " + desc + " - $" + String.format("%.2f", amount);
                lineItemBox.addItem(label);
                lineItemMap.put(label, lineItemId);
                billMap.put(label, billId);
                count++;
            }
            if (count == 0) {
                lineItemBox.addItem("No unpaid items available");
                lineItemBox.setEnabled(false);
            } else {
                lineItemBox.setEnabled(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}