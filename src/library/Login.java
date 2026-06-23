

package library;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

public class Login extends JFrame {
    private JLabel usernameL, passL, welL; 
    private JTextField usernameF; 
    private JPasswordField passF;
    private JButton loginB, signUpB, resetB; 
    
    public Login() {
        setTitle("Login or Sign up");
        setSize(400, 210);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        setResizable(false);
        setLocation(400, 300); 
        setLayout(null);
        setVisible(true); 
 
        usernameL = new JLabel("Username:");
        add(usernameL);
        usernameL.setBounds(50, 10, 150, 25);

        usernameF = new JTextField(20);
        add(usernameF);
        usernameF.setBounds(170, 10, 150, 25);

        passL = new JLabel("Password:");
        add(passL);
        passL.setBounds(50, 60, 150, 25);

        passF = new JPasswordField(20);
        add(passF);
        passF.setBounds(170, 60, 150, 25);

        welL = new JLabel("");
        add(welL);
        welL.setBounds(150, 140, 120, 25);

        loginB = new JButton("Login");
        add(loginB);
        loginB.setBounds(20, 110, 120, 25);
        loginB.addActionListener(new LoginAction());

        signUpB = new JButton("Sign up");
        add(signUpB);
        signUpB.setBounds(150, 110, 120, 25);
        signUpB.addActionListener(new SignUpAction());
                
        resetB = new JButton("Reset");
        add(resetB);
        resetB.setBounds(280, 110, 80, 25);
        resetB.addActionListener(new ResetAction());
    }
    
 private class LoginAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        String username = usernameF.getText().trim();  
        String pass = new String(passF.getPassword()).trim();

        if (username.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (username.equalsIgnoreCase("rawan")) {
            if (!pass.equals("11111111")) {
                welL.setText("Incorrect password.");
                return;
            }
            new Adminboard().setVisible(true);
            dispose();
            return;
        }
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (pass.equals(rs.getString("password"))) {
                        new Books(username).setVisible(true);
                        dispose();
                    } else {
                  welL.setText("Incorrect password.");
                    }
                } else {
                    int choice = JOptionPane.showConfirmDialog(null, "Username not found. Do you want to create a new account?", "Account Not Found", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        new SignUp();
                        dispose();
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
  private class SignUpAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
         new SignUp();  
         dispose();  
    }

}
    private class ResetAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            usernameF.setText("");
            passF.setText("");
        }
    }}