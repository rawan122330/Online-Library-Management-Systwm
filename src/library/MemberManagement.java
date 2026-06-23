package library;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;





//واجهه ادارة بيانات الميمبرز

public class MemberManagement extends JFrame {
    private JTable memberTable;
    private JButton refreshButton;
    private JButton addMemberButton;
    private JButton editMemberButton;
    private JButton deleteMemberButton;

public MemberManagement() {
    setTitle("Member Management");
    setSize(800, 450);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLayout(null);

    memberTable = new JTable();
    JScrollPane scrollPane = new JScrollPane(memberTable);
    scrollPane.setBounds(20, 20, 740, 300);
    add(scrollPane);

    refreshButton = new JButton("Refresh Members");// تحديث الميمبر
    refreshButton.setBounds(50, 350, 150, 30);
    add(refreshButton);
    refreshButton.addActionListener(new RefreshMembersAction());

    addMemberButton = new JButton("Add Member");// اضافة ميمبر
    addMemberButton.setBounds(220, 350, 150, 30);
    add(addMemberButton);
    addMemberButton.addActionListener(new AddMemberAction());

    editMemberButton = new JButton("Edit Member");//تعديل على بيانات الميمبر
    editMemberButton.setBounds(390, 350, 150, 30);
    add(editMemberButton);
    editMemberButton.addActionListener(new EditMemberAction());

    deleteMemberButton = new JButton("Delete Member");// حذف الميمبر
    deleteMemberButton.setBounds(560, 350, 150, 30);
    add(deleteMemberButton);
    deleteMemberButton.addActionListener(new DeleteMemberAction());
    loadMembers();
    setLocationRelativeTo(null);
    setVisible(true);
}

//تحميل بيانات الميمبرز من الداتابيس
    private void loadMembers() {
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Username", "Email"}, 0);//

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, username, email FROM users")) {
            
             //اضافة البيانات اللي جابها من الداتابيس بالجدول
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading members: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);//اذا صار فيه ايرور اثناء جلب البيانات
        }
        memberTable.setModel(tableModel);
    }

    private class RefreshMembersAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loadMembers();
        }
    }

    private class AddMemberAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            
            // اضافة بيانات ممبر جديد
            String username = JOptionPane.showInputDialog(MemberManagement.this, "Enter Member Username:");
            String email = JOptionPane.showInputDialog(MemberManagement.this, "Enter Member Email:");
            String password = JOptionPane.showInputDialog(MemberManagement.this, "Enter Member Password:");

            if (username != null && email != null && password != null) {
                if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                    JOptionPane.showMessageDialog(MemberManagement.this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (password.length() < 8) {
                    JOptionPane.showMessageDialog(MemberManagement.this, "Password must be at least 8 characters long!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                    //يتصل بالداتابيس عشان يضيف بيانات الممبر الجديد هذا
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, email, password) VALUES (?, ?, ?)")) {

                    stmt.setString(1, username);
                    stmt.setString(2, email);
                    stmt.setString(3, password);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(MemberManagement.this, "Member added successfully!");
                    loadMembers();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(MemberManagement.this, "Error adding member: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class EditMemberAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            //يحدد اول شيء الممبر اللي بنعدل عليه
            int selectedRow = memberTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(MemberManagement.this, "Please select a member to edit.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
                          // يطلع البيانات الحاليه للممبر من الجدول
            int memberId = (int) memberTable.getValueAt(selectedRow, 0);
                            //يطلب ادخال البيانات الجديدة 
            String newUsername = JOptionPane.showInputDialog(MemberManagement.this, "Enter New Username:", memberTable.getValueAt(selectedRow, 1));
            String newEmail = JOptionPane.showInputDialog(MemberManagement.this, "Enter New Email:", memberTable.getValueAt(selectedRow, 2));
            String newPassword = JOptionPane.showInputDialog(MemberManagement.this, "Enter New Password:");

            if (!newEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                JOptionPane.showMessageDialog(MemberManagement.this, "Invalid email format!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(MemberManagement.this, "Password must be at least 6 characters long!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
                     //يتصل بالداتابيس عشان يحدث البيانات
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE users SET username = ?, email = ?, password = ? WHERE id = ?")) {

                stmt.setString(1, newUsername);
                stmt.setString(2, newEmail);
                stmt.setString(3, newPassword);
                stmt.setInt(4, memberId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(MemberManagement.this, "Member updated successfully!");
                loadMembers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(MemberManagement.this, "Error updating member: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class DeleteMemberAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = memberTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(MemberManagement.this, "Please select a member to delete.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int memberId = (int) memberTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(MemberManagement.this, "Are you sure you want to delete this member?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) { //اذا حط يس راح ينحذف الميمبر من الداتابيس
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {

                    stmt.setInt(1, memberId);
                    stmt.executeUpdate();

                    JOptionPane.showMessageDialog(MemberManagement.this, "Member deleted successfully!");
                    loadMembers();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(MemberManagement.this, "Error deleting member: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}


