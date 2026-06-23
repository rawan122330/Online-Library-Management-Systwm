

package library;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class SignUp extends JFrame {
    private JLabel emailLabel, passLabel, usernameLabel;
    private JTextField emailField, usernameField;
    private JPasswordField passField;
    private JButton CreateButton, prevButton;

    public SignUp() {
        setTitle("Sign up");
        setSize(400, 210);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocation(400, 300);
        setLayout(null);
        setVisible(true);

        usernameLabel = new JLabel("Username :");
        add(usernameLabel);
        usernameLabel.setBounds(60, 10, 80, 25);

        usernameField = new JTextField(20);
        add(usernameField);
        usernameField.setBounds(150, 10, 150, 25);

        emailLabel = new JLabel("Email :");
        add(emailLabel);
        emailLabel.setBounds(60, 50, 80, 25);

        emailField = new JTextField(20);
        add(emailField);
        emailField.setBounds(150, 50, 150, 25);

        passLabel = new JLabel("Password :");
        add(passLabel);
        passLabel.setBounds(60, 90, 80, 25);

        passField = new JPasswordField(20);
        add(passField);
        passField.setBounds(150, 90, 150, 25);

        CreateButton = new JButton("Create");
        add(CreateButton);
        CreateButton.setBounds(100, 130, 100, 25);
        CreateButton.addActionListener(new CreateAction());

        prevButton = new JButton("Prev");
        add(prevButton);
        prevButton.setBounds(210, 130, 100, 25);
        prevButton.addActionListener(new PrevAction());
    }

    private class CreateAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passField.getPassword()).trim();

            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(SignUp.this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                JOptionPane.showMessageDialog(SignUp.this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(SignUp.this, "The password must be at least 6 characters long.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection connection = DatabaseManager.getConnection()) {
                String checkUsernameQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
                try (PreparedStatement checkUsernameStmt = connection.prepareStatement(checkUsernameQuery)) {
                    checkUsernameStmt.setString(1, username);
                    ResultSet rs = checkUsernameStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(SignUp.this, "Username is already taken. Please choose another one.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                String checkEmailQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
                try (PreparedStatement checkEmailStmt = connection.prepareStatement(checkEmailQuery)) {
                    checkEmailStmt.setString(1, email);
                    ResultSet rs = checkEmailStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(SignUp.this, "Email is already registered. Please choose another one.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                String insertQuery = "INSERT INTO users (email, username, password) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                    stmt.setString(1, email);
                    stmt.setString(2, username);
                    stmt.setString(3, password);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(SignUp.this, "Account created successfully!", "Success", JOptionPane.PLAIN_MESSAGE);
                    dispose();
                    new Login().setVisible(true);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(SignUp.this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class PrevAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
            new Login().setVisible(true);
        }
    }
}