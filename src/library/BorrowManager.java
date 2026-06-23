package library;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

// كلاس BorrowManager لإدارة عملية استعارة كتاب باستخدام واجهة رسومية Swing
public class BorrowManager extends JFrame {
    
    private JTextField bookTitleField, bookAuthorField, dueDateField; 
    private JButton borrowButton; 
    private String username; 
    private int bookId; 

    // المنشئ (Constructor) لتهيئة الواجهة الرسومية مع القيم الضرورية
    public BorrowManager(String username, int bookId, String bookTitle, String bookAuthor) {
        this.username = username; 
        this.bookId = bookId; 

        // إعداد خصائص نافذة الاستعارة
        setTitle("Borrow Book"); 
        setSize(400, 250); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // إغلاق النافذة فقط عند النقر على زر الإغلاق
        setLayout(new GridLayout(4, 2, 10, 10)); // تنظيم العناصر بترتيب شبكي GridLayout

        // إعداد عناصر الواجهة الرسومية (Labels, TextFields, Buttons)
        JLabel titleLabel = new JLabel("Book Title:"); // عنوان النص لعناوين الحقول
        bookTitleField = new JTextField(bookTitle); // حقل لعرض عنوان الكتاب
        bookTitleField.setEditable(false); // جعله غير قابل للتعديل
        add(bookTitleField); // إضافته للواجهة

        JLabel authorLabel = new JLabel("Book Author:"); // عنوان النص للمؤلف
        bookAuthorField = new JTextField(bookAuthor); // حقل لعرض اسم المؤلف
        bookAuthorField.setEditable(false); // جعله غير قابل للتعديل

        JLabel dueDateLabel = new JLabel("Due Date (YYYY-MM-DD):"); // حقل إدخال لتاريخ الاستحقاق
        dueDateField = new JTextField(10); // إدخال تاريخ الإرجاع

        borrowButton = new JButton("Borrow Book"); // زر لإتمام عملية الاستعارة
        borrowButton.addActionListener(new BorrowBookAction()); // إضافة الحدث الذي يتم استدعاؤه عند الضغط على الزر

        // ترتيب العناصر داخل النافذة
        add(titleLabel);
        add(bookTitleField);
        add(authorLabel);
        add(bookAuthorField);
        add(dueDateLabel);
        add(dueDateField);
        add(new JLabel()); // عنصر فارغ للتنسيق
        add(borrowButton);

        setLocationRelativeTo(null); // جعل النافذة تظهر في منتصف الشاشة
        setVisible(true); // جعل النافذة مرئية
    }

    // كلاس داخلي لمعالجة حدث الضغط على زر "Borrow Book"
    private class BorrowBookAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // التحقق من إدخال تاريخ الإرجاع
            String dueDateText = dueDateField.getText().trim(); // الحصول على النص المدخل في الحقل
            if (dueDateText.isEmpty()) { // التحقق إذا كان الحقل فارغًا
                JOptionPane.showMessageDialog(BorrowManager.this, "Please enter a due date!", "Error", JOptionPane.ERROR_MESSAGE);
                return; // الخروج من الحدث
            }

            // التحقق من تنسيق التاريخ (YYYY-MM-DD)
            if (!dueDateText.matches("\\d{4}-\\d{2}-\\d{2}")) { // استخدام التعبيرات النمطية
                JOptionPane.showMessageDialog(BorrowManager.this, "Invalid due date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // التحقق من أن التاريخ المدخل ليس تاريخًا ماضيًا
            try {
                java.time.LocalDate dueDate = java.time.LocalDate.parse(dueDateText); // تحويل النص إلى كائن تاريخ
                if (dueDate.isBefore(java.time.LocalDate.now())) { // التحقق إذا كان التاريخ في الماضي
                    JOptionPane.showMessageDialog(BorrowManager.this, "Due date must be in the future!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(BorrowManager.this, "Invalid date entered.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // محاولة الاتصال بقاعدة البيانات للتحقق من البيانات وإتمام عملية الاستعارة
            try (Connection conn = DatabaseManager.getConnection()) {
                // التحقق من وجود المستخدم في قاعدة البيانات
                String checkUserQuery = "SELECT id FROM users WHERE username = ?";
                int userId;

                try (PreparedStatement userStmt = conn.prepareStatement(checkUserQuery)) {
                    userStmt.setString(1, username); // تعيين اسم المستخدم في الاستعلام
                    try (ResultSet userRs = userStmt.executeQuery()) {
                        if (userRs.next()) { // إذا كان المستخدم موجودًا
                            userId = userRs.getInt("id"); // الحصول على معرف المستخدم
                        } else { // إذا لم يتم العثور على المستخدم
                            JOptionPane.showMessageDialog(BorrowManager.this, "Username not found. Please log in again.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }

                // التحقق من توفر الكتاب في قاعدة البيانات
                String checkAvailabilityQuery = "SELECT num_of_books FROM books WHERE id = ?";
                try (PreparedStatement bookStmt = conn.prepareStatement(checkAvailabilityQuery)) {
                    bookStmt.setInt(1, bookId); // تعيين معرف الكتاب في الاستعلام

                    try (ResultSet bookRs = bookStmt.executeQuery()) {
                        if (bookRs.next()) { // إذا تم العثور على الكتاب
                            int availableCopies = bookRs.getInt("num_of_books"); // الحصول على عدد النسخ المتوفرة
                            if (availableCopies <= 0) { // إذا لم تكن هناك نسخ متوفرة
                                JOptionPane.showMessageDialog(BorrowManager.this, "Sorry, this book is not available right now!", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            // استدعاء وظيفة لإتمام عملية الاستعارة
                            borrowBook(conn, userId, dueDateText);
                        } else { // إذا لم يتم العثور على الكتاب
                            JOptionPane.showMessageDialog(BorrowManager.this, "Book not found in the database!", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            } catch (SQLException ex) { // معالجة أخطاء قاعدة البيانات
                JOptionPane.showMessageDialog(BorrowManager.this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // وظيفة خاصة لإتمام عملية الاستعارة
        private void borrowBook(Connection conn, int userId, String dueDateText) throws SQLException {
            // إضافة سجل جديد للاستعارة في قاعدة البيانات
            String borrowQuery = "INSERT INTO borrowings (user_id, book_id, borrow_date, return_date, status) VALUES (?, ?, CURDATE(), ?, 'borrowed')";
            try (PreparedStatement borrowStmt = conn.prepareStatement(borrowQuery)) {
                borrowStmt.setInt(1, userId); // تعيين معرف المستخدم
                borrowStmt.setInt(2, bookId); // تعيين معرف الكتاب
                borrowStmt.setString(3, dueDateText); // تعيين تاريخ الإرجاع
                borrowStmt.executeUpdate(); // تنفيذ الاستعلام
            }

            // تقليل عدد النسخ المتوفرة للكتاب
            String decrementCopiesQuery = "UPDATE books SET num_of_books = num_of_books - 1 WHERE id = ? AND num_of_books > 0";
            try (PreparedStatement decrementStmt = conn.prepareStatement(decrementCopiesQuery)) {
                decrementStmt.setInt(1, bookId); // تعيين معرف الكتاب
                decrementStmt.executeUpdate(); // تنفيذ الاستعلام
            }

            // تحديث حالة توفر الكتاب بناءً على عدد النسخ المتبقية
            String updateAvailabilityQuery = "UPDATE books SET availability = IF(num_of_books > 0, 1, 0) WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateAvailabilityQuery)) {
                updateStmt.setInt(1, bookId); // تعيين معرف الكتاب
                updateStmt.executeUpdate(); // تنفيذ الاستعلام
            }

            // عرض رسالة نجاح
            JOptionPane.showMessageDialog(BorrowManager.this, "Book borrowed successfully! Please return it by " + dueDateText);
            dispose(); // إغلاق النافذة
        }
    }
}