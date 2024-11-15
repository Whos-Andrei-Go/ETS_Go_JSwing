package ets_go;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class EventTicketingSystem extends JFrame {

    private JTabbedPane tabbedPane;
    private JPanel eventPanel, userPanel, offersPanel;;

    private DefaultTableModel eventTableModel;
    private JTable eventTable;

    private DefaultTableModel userTableModel;
    private JTable userTable;
    
    private DefaultTableModel offersTableModel;
    private JTable offersTable;

    // Database connection details for Microsoft SQL Server
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=ETS_DB;user=admin;password=admin;trustServerCertificate=true";
    private static final String DB_USER = "admin";  // Username for your database
    private static final String DB_PASSWORD = "admin"; // Password for your database

    public EventTicketingSystem() {
        setTitle("Event Ticketing System - Admin Dashboard");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set up the layout and tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.PLAIN, 16));

        // Create panels
        eventPanel = createEventManagementPanel();
        userPanel = createUserManagementPanel();
        offersPanel = createOffersManagementPanel(); 

        tabbedPane.addTab("Manage Events", eventPanel);
        tabbedPane.addTab("Manage Users", userPanel);
        tabbedPane.addTab("Manage Offers", offersPanel);  // Add new tab for Offers

        // Set background color and layout
        getContentPane().setBackground(new Color(245, 245, 245));
        add(tabbedPane);

        // Set UI enhancements
        applyUIEnhancements();
    }

    // --- Event Management Panel ---
    private JPanel createEventManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Create a table model for the events
        String[] columns = {"Event ID", "Event Name", "Event Date"};
        eventTableModel = new DefaultTableModel(columns, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        eventTable = new JTable(eventTableModel);

        // Load event data from the database
        loadEventsFromDatabase();

        // Add the table with a scroll pane
        JScrollPane scrollPane = new JScrollPane(eventTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add buttons for managing events
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton addEventButton = createButton("Add Event", "/icons/add.png");
        JButton deleteEventButton = createButton("Delete Event", "/icons/delete.png");
        
        buttonPanel.add(addEventButton);
        buttonPanel.add(deleteEventButton);

        addEventButton.addActionListener(e -> {
            String eventName = JOptionPane.showInputDialog(this, "Enter Event Name:");
            String eventDate = JOptionPane.showInputDialog(this, "Enter Event Date (YYYY-MM-DD):");
            if (eventName != null && !eventName.trim().isEmpty() && eventDate != null && !eventDate.trim().isEmpty()) {
                addEventToDatabase(eventName, eventDate);
                loadEventsFromDatabase(); // Reload data
            }
        });

        deleteEventButton.addActionListener(e -> {
            int selectedRow = eventTable.getSelectedRow();
            if (selectedRow != -1) {
                int eventId = (int) eventTable.getValueAt(selectedRow, 0);
                deleteEventFromDatabase(eventId);
                loadEventsFromDatabase(); // Reload data
            } else {
                JOptionPane.showMessageDialog(this, "Please select an event to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        eventTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // Double-click to open analytics view
                    int selectedRow = eventTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int eventId = (int) eventTable.getValueAt(selectedRow, 0);
                        String eventName = (String) eventTable.getValueAt(selectedRow, 1);
                        openEventAnalyticsView(eventId, eventName);
                    }
                }
            }
        });


        return panel;
    }

    // --- User Management Panel ---
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Create a table model for the users
        String[] userColumns = {"User ID", "Username", "Email", "Role"};
        userTableModel = new DefaultTableModel(userColumns, 0);
        userTable = new JTable(userTableModel);

        // Load user data from the database
        loadUsersFromDatabase();

        // Add the table with a scroll pane
        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add buttons for managing users
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton addUserButton = createButton("Add User", "/icons/add.png");
        JButton deleteUserButton = createButton("Delete User", "/icons/delete.png");

        buttonPanel.add(addUserButton);
        buttonPanel.add(deleteUserButton);

        addUserButton.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this, "Enter Username:");
            String email = JOptionPane.showInputDialog(this, "Enter Email:");
            String password = JOptionPane.showInputDialog(this, "Enter Password:");
            String role = JOptionPane.showInputDialog(this, "Enter Role (Admin, Organizer, Attendee):");

            if (username != null && !username.trim().isEmpty() && email != null && !email.trim().isEmpty() && password != null && !password.trim().isEmpty() && role != null && !role.trim().isEmpty()) {
                addUserToDatabase(username, email, password, role);
                loadUsersFromDatabase(); // Reload data
            }
        });

        deleteUserButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow != -1) {
                int userId = (int) userTable.getValueAt(selectedRow, 0);
                deleteUserFromDatabase(userId);
                loadUsersFromDatabase(); // Reload data
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }
    
    // --- Offers Management Panel --- (New Panel)
    private JPanel createOffersManagementPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Create a table model for the offers
        String[] offerColumns = {"Offer ID", "Event Name", "Discount Percentage", "Start Date", "End Date"};
        offersTableModel = new DefaultTableModel(offerColumns, 0);
        offersTable = new JTable(offersTableModel);

        // Load offers data from the database
        loadOffersFromDatabase();

        // Add the table with a scroll pane
        JScrollPane scrollPane = new JScrollPane(offersTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Add buttons for managing offers
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));

        JButton addOfferButton = createButton("Add Offer", "/icons/add.png");
        JButton deleteOfferButton = createButton("Delete Offer", "/icons/delete.png");

        buttonPanel.add(addOfferButton);
        buttonPanel.add(deleteOfferButton);

        addOfferButton.addActionListener(e -> {
            String eventId = JOptionPane.showInputDialog(this, "Enter Event ID for the offer:");
            String discountPercentage = JOptionPane.showInputDialog(this, "Enter Discount Percentage:");
            String startDate = JOptionPane.showInputDialog(this, "Enter Offer Start Date (YYYY-MM-DD):");
            String endDate = JOptionPane.showInputDialog(this, "Enter Offer End Date (YYYY-MM-DD):");

            if (eventId != null && !eventId.trim().isEmpty() &&
                discountPercentage != null && !discountPercentage.trim().isEmpty() &&
                startDate != null && !startDate.trim().isEmpty() &&
                endDate != null && !endDate.trim().isEmpty()) {
                addOfferToDatabase(eventId, discountPercentage, startDate, endDate);
                loadOffersFromDatabase(); // Reload data
            }
        });

        deleteOfferButton.addActionListener(e -> {
            int selectedRow = offersTable.getSelectedRow();
            if (selectedRow != -1) {
                int offerId = (int) offersTable.getValueAt(selectedRow, 0);
                deleteOfferFromDatabase(offerId);
                loadOffersFromDatabase(); // Reload data
            } else {
                JOptionPane.showMessageDialog(this, "Please select an offer to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    // --- Load Data Methods ---
    private void loadEventsFromDatabase() {
        // Clear the current data in the table
        eventTableModel.setRowCount(0);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM events")) {

            // Loop through the results and add each row to the table
            while (resultSet.next()) {
                int eventId = resultSet.getInt("id");
                String eventName = resultSet.getString("name");
                String eventDate = resultSet.getString("date");

                // Add a row to the table
                eventTableModel.addRow(new Object[]{eventId, eventName, eventDate});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading events from the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadUsersFromDatabase() {
        // Clear the current data in the table
        userTableModel.setRowCount(0);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM Users")) {

            // Loop through the results and add each row to the table
            while (resultSet.next()) {
                int userId = resultSet.getInt("id");
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                String role = resultSet.getString("role");

                // Add a row to the table
                userTableModel.addRow(new Object[]{userId, username, email, role});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading users from the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadOffersFromDatabase() {
        offersTableModel.setRowCount(0);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT o.id, e.name, o.discount_percentage, o.start_date, o.end_date FROM offers o JOIN events e ON o.event_id = e.id")) {

            while (resultSet.next()) {
                int offerId = resultSet.getInt("id");
                String eventName = resultSet.getString("name");
                double discountPercentage = resultSet.getDouble("discount_percentage");
                String startDate = resultSet.getString("start_date");
                String endDate = resultSet.getString("end_date");

                offersTableModel.addRow(new Object[]{offerId, eventName, discountPercentage, startDate, endDate});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading offers from the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Add/Remove Data Methods ---
    private void addEventToDatabase(String name, String date) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO events (name, date) VALUES (?, ?)")) {

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, date);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Event added successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add event.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding event to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEventFromDatabase(int eventId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM events WHERE id = ?")) {

            preparedStatement.setInt(1, eventId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Event deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete event.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting event from the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addUserToDatabase(String username, String email, String password, String role) {
        if (password != null && !password.trim().isEmpty()) {
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO users (username, email, role, password) VALUES (?, ?, ?, ?)")) {

                preparedStatement.setString(1, username);
                preparedStatement.setString(2, email);
                preparedStatement.setString(3, role);
                preparedStatement.setString(4, password);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "User added successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add user.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding user to the database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUserFromDatabase(int userId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {

            preparedStatement.setInt(1, userId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "User deleted successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting user from the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addOfferToDatabase(String eventId, String discountPercentage, String startDate, String endDate) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO offers (event_id, discount_percentage, start_date, end_date) VALUES (?, ?, ?, ?)")) {

            preparedStatement.setInt(1, Integer.parseInt(eventId));
            preparedStatement.setDouble(2, Double.parseDouble(discountPercentage));
            preparedStatement.setString(3, startDate);
            preparedStatement.setString(4, endDate);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding offer to the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteOfferFromDatabase(int offerId) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM offers WHERE id = ?")) {

            preparedStatement.setInt(1, offerId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting offer from the database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Helper Methods ---
    private JButton createButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 102, 204));
        //button.setIcon(new ImageIcon(getClass().getResource(iconPath)));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        button.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 85, 179));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 102, 204));
            }
        });

        return button;
    }

    private void applyUIEnhancements() {
        // Apply a gradient background for the header panel
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(0, 0, new Color(0, 102, 204), 0, getHeight(), new Color(0, 51, 153));
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(getWidth(), 60));
        headerPanel.setBackground(new Color(0, 102, 204));

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(headerPanel, BorderLayout.NORTH);
        containerPanel.add(tabbedPane, BorderLayout.CENTER);
        add(containerPanel);

        JLabel headerLabel = new JLabel("Event Ticketing System", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
    }
    
    private void openEventAnalyticsView(int eventId, String eventName) {
        JFrame analyticsFrame = new JFrame("Analytics for Event: " + eventName);
        analyticsFrame.setSize(800, 600);  // Adjust the size to accommodate both tables
        analyticsFrame.setLocationRelativeTo(this);

        // Table for event analytics (metrics)
        String[] analyticsColumns = {"Metric", "Value"};
        DefaultTableModel analyticsTableModel = new DefaultTableModel(analyticsColumns, 0);
        JTable analyticsTable = new JTable(analyticsTableModel);
        loadEventAnalytics(eventId, analyticsTableModel);

        // Table for tickets sold
        String[] ticketsColumns = {"Ticket ID", "User ID", "Ticket Type", "Price", "Status", "Offer ID"};
        DefaultTableModel ticketsTableModel = new DefaultTableModel(ticketsColumns, 0);
        JTable ticketsTable = new JTable(ticketsTableModel);
        loadTicketsSold(eventId, ticketsTableModel);

        // Create a tabbed pane to switch between analytics and tickets
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Event Analytics", new JScrollPane(analyticsTable));
        tabbedPane.addTab("Tickets Sold", new JScrollPane(ticketsTable));

        // Add components to the analytics frame
        analyticsFrame.setLayout(new BorderLayout());
        analyticsFrame.add(tabbedPane, BorderLayout.CENTER);

        // Close button
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> analyticsFrame.dispose());
        analyticsFrame.add(closeButton, BorderLayout.SOUTH);

        analyticsFrame.setVisible(true);
    }

// Method to load event analytics data (e.g., number of tickets sold)
    private void loadEventAnalytics(int eventId, DefaultTableModel analyticsTableModel) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement statement = connection.prepareStatement(
                "SELECT COUNT(*) AS tickets_sold, SUM(price) AS total_revenue "
                + "FROM Tickets WHERE event_id = ?")) {

            statement.setInt(1, eventId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int ticketsSold = resultSet.getInt("tickets_sold");
                double totalRevenue = resultSet.getDouble("total_revenue");

                // Add metrics to the analytics table
                analyticsTableModel.addRow(new Object[]{"Tickets Sold", ticketsSold});
                analyticsTableModel.addRow(new Object[]{"Total Revenue", totalRevenue});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading event analytics.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

// Method to load tickets sold for the event
    private void loadTicketsSold(int eventId, DefaultTableModel ticketsTableModel) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement statement = connection.prepareStatement(
                "SELECT id, user_id, ticket_type, price, status, offer_id "
                + "FROM Tickets WHERE event_id = ?")) {

            statement.setInt(1, eventId);
            ResultSet resultSet = statement.executeQuery();

            // Add each ticket to the tickets table
            while (resultSet.next()) {
                int ticketId = resultSet.getInt("id");
                int userId = resultSet.getInt("user_id");
                String ticketType = resultSet.getString("ticket_type");
                double ticketPrice = resultSet.getDouble("price");
                String ticketStatus = resultSet.getString("status");
                String offerId = resultSet.getString("offer_id");

                ticketsTableModel.addRow(new Object[]{ticketId, userId, ticketType, ticketPrice, ticketStatus, offerId});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading tickets sold.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }




    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EventTicketingSystem window = new EventTicketingSystem();
            window.setVisible(true);
        });
    }
}
