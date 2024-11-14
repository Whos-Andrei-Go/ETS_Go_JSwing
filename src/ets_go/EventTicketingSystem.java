package ets_go;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

public class EventTicketingSystem extends JFrame {

    private JTabbedPane tabbedPane;
    private JPanel eventPanel, userPanel, statsPanel;

    private DefaultTableModel eventTableModel;
    private JTable eventTable;

    private DefaultTableModel userTableModel;
    private JTable userTable;

    // Database connection details for Microsoft SQL Server
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=ETS_DB;user=sa;password=sa;trustServerCertificate=true";
    private static final String DB_USER = "sa";  // Username for your database
    private static final String DB_PASSWORD = "sa"; // Password for your database

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
        statsPanel = new JPanel(); // Placeholder panel

        tabbedPane.addTab("Manage Events", eventPanel);
        tabbedPane.addTab("Manage Users", userPanel);
        tabbedPane.addTab("Statistics", statsPanel);

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
        eventTableModel = new DefaultTableModel(columns, 0);
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
             ResultSet resultSet = statement.executeQuery("SELECT * FROM users")) {

            // Loop through the results and add each row to the table
            while (resultSet.next()) {
                int userId = resultSet.getInt("user_id");
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
             PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM users WHERE user_id = ?")) {

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EventTicketingSystem window = new EventTicketingSystem();
            window.setVisible(true);
        });
    }
}
