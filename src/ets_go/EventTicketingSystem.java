/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

    // Database connection details for Microsoft SQL Server
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=eventdb;user=sa;password=password;";
    private static final String DB_USER = "sa";  // Username for your database
    private static final String DB_PASSWORD = "password"; // Password for your database

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
        userPanel = new JPanel(); // Placeholder panel
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

    // Helper method to create styled buttons
    private JButton createButton(String text, String iconPath) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 102, 204));
        button.setIcon(new ImageIcon(getClass().getResource(iconPath)));
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

        // Add header panel to the top of the frame (this is optional and depends on your design)
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(headerPanel, BorderLayout.NORTH);
        containerPanel.add(tabbedPane, BorderLayout.CENTER);
        add(containerPanel);

        // Add a label to the header
        JLabel headerLabel = new JLabel("Event Ticketing System", JLabel.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
    }

    public static void main(String[] args) {
        // Run the GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            EventTicketingSystem window = new EventTicketingSystem();
            window.setVisible(true);
        });
    }
}
            
