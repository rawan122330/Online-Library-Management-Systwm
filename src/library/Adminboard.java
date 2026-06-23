package library;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.io.FileWriter;
import java.io.IOException;

public class Adminboard extends JFrame{
    private JTable bookTable, borrowTable;
    private JTextArea notificationArea;
    private JButton  addBookButton, editBookButton, deleteBookButton, toggleAvailabilityButton, manageUserButton, clearnotfication;
    private JButton generateReportButton, generateReceiptButton, markAsReturnedButton,refreshNotificationsButton;

public Adminboard(){
    setTitle("Library Admin Dashboard");
    setSize(1200, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
    setLayout(null);
    setVisible(true);
    repaint();

    JPanel bookPanel = new JPanel(null);
    bookPanel.setBounds(10, 10, 600, 370);
    JLabel bookLabel = new JLabel("Book Inventory", JLabel.CENTER);
    bookLabel.setBounds(10, 10, 550, 30);
    bookPanel.add(bookLabel);
    
    bookTable = new JTable();//جدول الكتب
    JScrollPane bookScrollPane = new JScrollPane(bookTable);
    bookScrollPane.setBounds(10, 50, 550, 200);
    bookPanel.add(bookScrollPane);
    bookTable.setDefaultEditor(Object.class, null);//لجعل الجدول غير قابل للتعديل مباشرة
    //ازرار إدارة الكتب
    addBookButton = new JButton("Add Book");
    addBookButton.setBounds(60, 270, 130, 30);
    bookPanel.add(addBookButton);
    addBookButton.addActionListener(new AddBookAction());

    editBookButton = new JButton("Edit Book");
    editBookButton.setBounds(220, 270, 130, 30);
    bookPanel.add(editBookButton);
    editBookButton.addActionListener(new EditBookAction());

    deleteBookButton = new JButton("Delete Book");
    deleteBookButton.setBounds(370, 270, 130, 30);
    bookPanel.add(deleteBookButton);
    deleteBookButton.addActionListener(new DeleteBookAction());

    toggleAvailabilityButton = new JButton("Toggle Availability");
    toggleAvailabilityButton.setBounds(210, 320, 150, 30);
    bookPanel.add(toggleAvailabilityButton);
    toggleAvailabilityButton.addActionListener(new ToggleAvailabilityAction());
    add(bookPanel);
    //إدارة الاقتراضات
    JPanel borrowPanel = new JPanel(null);
    borrowPanel.setBounds(600, 10, 570, 370);

    JLabel borrowLabel = new JLabel("Borrowing History", JLabel.CENTER);
    borrowLabel.setBounds(10, 10, 550, 30);
    borrowPanel.add(borrowLabel);
    bookTable.setDefaultEditor(Object.class, null); 

    borrowTable = new JTable();
    JScrollPane borrowScrollPane = new JScrollPane(borrowTable);
    borrowScrollPane.setBounds(10, 50, 550, 200);
    borrowPanel.add(borrowScrollPane);
    //أزرار إدارة السجلات
    generateReportButton = new JButton("Generate Report");
    generateReportButton.setBounds(210, 270, 150, 30);
    borrowPanel.add(generateReportButton);
    generateReportButton.addActionListener(new GenerateReportAction());

    generateReceiptButton = new JButton("Generate Receipt");
    generateReceiptButton.setBounds(380, 270, 150, 30);
    borrowPanel.add(generateReceiptButton);
    generateReceiptButton.addActionListener(new GenerateReceiptAction());

    manageUserButton = new JButton("Manage User Info");
    manageUserButton.setBounds(40, 270, 150, 30);
    borrowPanel.add(manageUserButton);
    manageUserButton.addActionListener(new ManageUserAction());

    markAsReturnedButton = new JButton("Mark as Returned");
    markAsReturnedButton.setBounds(210, 320, 150, 30);
    borrowPanel.add(markAsReturnedButton);
    markAsReturnedButton.addActionListener(new MarkAsReturnedAction());
    add(borrowPanel);
    //الإشعارات
    JPanel notificationPanel = new JPanel(null);
    notificationPanel.setBounds(10, 360, 1160, 200);

    JLabel notificationLabel = new JLabel("Notifications", JLabel.CENTER);
    notificationLabel.setBounds(10, 10, 1140, 30);
    notificationPanel.add(notificationLabel);

    notificationArea = new JTextArea();
    notificationArea.setEditable(false);//جعل النص للقراءة فقط
    JScrollPane notificationScrollPane = new JScrollPane(notificationArea);
    notificationScrollPane.setBounds(10, 50, 1140, 100);
    notificationPanel.add(notificationScrollPane);
   
    clearnotfication = new JButton("Clear Notifications");//زر مسح الاشعارات
    clearnotfication.setBounds(520, 160, 150, 30);
    notificationPanel.add(clearnotfication);
    clearnotfication.addActionListener(new clearnotfication());
    add(notificationPanel);
    JButton refreshNotificationsButton; 

   refreshNotificationsButton = new JButton("Refresh Notifications");//زر تحديث الاشعارات
   refreshNotificationsButton.setBounds(690, 160, 150, 30);
   notificationPanel.add(refreshNotificationsButton);

    loadBooks();//تحميل بيانات الكتب
    loadBorrows();//تحميل بيانات الاستعارات
    loadNotifications();//تحميل الاشعارات
    updateOverdueStatus();//تحديث حالة الكتب المتأخرة
}
//تحميل بيانات الكتب من قاعدة البيانات إلى جدول الكتب
private void loadBooks() {
    DefaultTableModel bookModel = new DefaultTableModel(
    new String[]{"ID", "Title", "Author", "Genre", "Copies", "Availability", "Publication Date"}, 0);
    try (Connection conn = DatabaseManager.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM books")) {
        while (rs.next()) {
            bookModel.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("genre"),
                rs.getInt("num_of_books"),
                rs.getInt("availability") == 1 ? "Yes" : "No",
                rs.getDate("publication_date") });}
    }catch(SQLException e){
        JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
    bookTable.setModel(bookModel);//تحديث جدول الكتب
}
//تحميل بيانات الاستعارات من قاعدة البيانات الى جدول الاستعارات 
private void loadBorrows() {
        updateOverdueStatus();
        DefaultTableModel borrowModel = new DefaultTableModel(new String[]{"ID", "User ID", "Book ID", "Borrow Date", "Return Date", "Status"}, 0);
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM borrowings")) {
             while (rs.next()) {
                borrowModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("book_id"),
                    rs.getDate("borrow_date"),
                    rs.getDate("return_date"),
                    rs.getString("status")});}
        }catch(SQLException e){
            JOptionPane.showMessageDialog(this, "Error loading rentals: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        borrowTable.setModel(borrowModel);//تحديث جدول الاستعارات
} 
//تحميل الاشعارات من قاعدة البيانات و عرضها 
private void loadNotifications() {
    String overdueQuery = "SELECT u.email, b.title, br.return_date " +"FROM borrowings br " +"JOIN books b ON br.book_id = b.id " +"JOIN users u ON br.user_id = u.id " +"WHERE br.status = 'Overdue'";

    String newBorrowQuery = "SELECT u.email, b.title, br.return_date " +"FROM borrowings br " +"JOIN books b ON br.book_id = b.id " +"JOIN users u ON br.user_id = u.id "+"WHERE br.borrow_date = CURDATE()";

    try (Connection conn = DatabaseManager.getConnection()) {
        try (PreparedStatement stmt = conn.prepareStatement(overdueQuery);
             ResultSet rs = stmt.executeQuery()) {
            notificationArea.append("Overdue Books:\n");
            while (rs.next()) {
                notificationArea.append("User Email: " + rs.getString("email") +", Title: " + rs.getString("title") +", Due Date: " + rs.getDate("return_date") + "\n");} }
        //تحميل الاستعارات الجديدة
        try (PreparedStatement stmt = conn.prepareStatement(newBorrowQuery);
             ResultSet rs = stmt.executeQuery()) {
            notificationArea.append("\nNew Borrowings:\n");
            while (rs.next()) {
                notificationArea.append("User Email: " + rs.getString("email") +", Title: " + rs.getString("title") +", Due Date: " + rs.getDate("return_date") + "\n");
            }}
    }catch (SQLException e){
        JOptionPane.showMessageDialog(this, "Error loading notifications: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);}} 
//تحديث حالة الاستعارات اذا تجاوزت تاريخ الارجاع
private void updateOverdueStatus() {
    String query = "UPDATE borrowings SET status = 'overdue' WHERE status = 'borrowed' AND return_date < CURDATE()";
    try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        }catch(SQLException e) {
            JOptionPane.showMessageDialog(this, "Error updating overdue status: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);}
    }

private class AddBookAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        String title = JOptionPane.showInputDialog("Enter Book Title:");
        if (title == null) return; 
        String author = JOptionPane.showInputDialog("Enter Book Author:");
        if (author == null) return;
        String genre = JOptionPane.showInputDialog("Enter Book Genre:");
        if (genre == null) return;
        String copies = JOptionPane.showInputDialog("Enter Number of Copies:");
        if (copies == null) return;
        String publicationDate = JOptionPane.showInputDialog("Enter Publication Date (yyyy-mm-dd):");
        if (publicationDate == null) return;
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO books (title, author, genre, num_of_books, availability, publication_date) VALUES (?, ?, ?, ?, ?, ?)")) {
            //الاضافة الى قاعدة البيانات
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, genre);
            stmt.setInt(4, Integer.parseInt(copies));
            stmt.setInt(5, Integer.parseInt(copies) > 0 ? 1 : 0);//لتحديد التوفر
            stmt.setDate(6, Date.valueOf(publicationDate));  
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Book added successfully!");//لاعلام المستخدم بالنجاح و تحديث العرض
            loadBooks();
        }catch(SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error adding book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);}}}

private class EditBookAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = bookTable.getSelectedRow();//الحصول على الصف المحدد في جدول الكتب
        if (selectedRow == -1) {
JOptionPane.showMessageDialog(null, "Please select a book.", "Warning", JOptionPane.INFORMATION_MESSAGE);
            return;}
        int bookId = (int) bookTable.getValueAt(selectedRow, 0);//للكتاب من الصف المحددIDاستخراج
        String title = JOptionPane.showInputDialog("Enter New Title:", bookTable.getValueAt(selectedRow, 1));
        String author = JOptionPane.showInputDialog("Enter New Author:", bookTable.getValueAt(selectedRow, 2));
        String genre = JOptionPane.showInputDialog("Enter New Genre:", bookTable.getValueAt(selectedRow, 3));
        String copies = JOptionPane.showInputDialog("Enter New Number of Copies:", bookTable.getValueAt(selectedRow, 4));
        String publicationDate = JOptionPane.showInputDialog("Enter New Publication Date (yyyy-mm-dd):", bookTable.getValueAt(selectedRow, 6));
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE books SET title = ?, author = ?, genre = ?, num_of_books = ?, availability = ?, publication_date = ? WHERE id = ?")) {

            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, genre);
            stmt.setInt(4, Integer.parseInt(copies));//لتحويل عدد النسخ المدخل الى عدد صحيح
            stmt.setInt(5, Integer.parseInt(copies) > 0 ? 1 : 0);//لتحديد توفر الكتاب
            stmt.setDate(6, Date.valueOf(publicationDate));//Dateلتحويل تاريخ النشر الى نوع 
            stmt.setInt(7, bookId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Book updated successfully!");
            loadBooks();
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Error updating book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }}}
private class DeleteBookAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
JOptionPane.showMessageDialog(null, "Please select a book.", "Warning", JOptionPane.INFORMATION_MESSAGE);
            return;}
        int bookId = (int) bookTable.getValueAt(selectedRow, 0);
        try(Connection conn = DatabaseManager.getConnection();//لفتح الاتصال مع قاعدة البيانات
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM books WHERE id = ?")){//يحضر استعلام لحذف الكتاب
            stmt.setInt(1, bookId);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Book deleted successfully!");
            loadBooks();
        }catch(SQLException ex) {
            JOptionPane.showMessageDialog(null, "Error deleting book: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }}}

private class ToggleAvailabilityAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = bookTable.getSelectedRow();
            if (selectedRow == -1) {
JOptionPane.showMessageDialog(null, "Please select a book.", "Warning", JOptionPane.INFORMATION_MESSAGE);
                return;}
            int bookId = (int) bookTable.getValueAt(selectedRow, 0);
            try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement("UPDATE books SET availability = NOT availability WHERE id = ?")) {//يحضر استعلام لتبديل التوفر
                stmt.setInt(1, bookId);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(null, "Book availability toggled successfully!");
                loadBooks();
            }catch(SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error toggling availability: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }}}
private class GenerateReportAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        String reportContent = "Library Report\n\n";
        try (Connection conn = DatabaseManager.getConnection();
            Statement stmt = conn.createStatement()){
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total_borrowings FROM borrowings")){//استعلام لحساب اجمالي عمليات الاستعارة
                if (rs.next()) {
                    reportContent += "Total Borrowings: " + rs.getInt("total_borrowings") + "\n";}}
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total_books FROM books")) {//استعلام لحساب لجمالي الكتب
                if (rs.next()) {
                    reportContent += "Total Books: " + rs.getInt("total_books") + "\n";}}
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS total_users FROM users")){//استعلام لحساب اجمالي المستخدمين
                if (rs.next()) {
                    reportContent += "Total Users: " + rs.getInt("total_users") + "\n";}}
            //استعلام لاستخراج الكتاب الاكثر استعارة
            try (ResultSet rs = stmt.executeQuery("SELECT b.title, COUNT(br.book_id) AS borrow_count " +"FROM borrowings br " +"JOIN books b ON br.book_id = b.id " +"GROUP BY br.book_id " +"ORDER BY borrow_count DESC " +"LIMIT 1")) {
                if(rs.next()){
                    reportContent += "\nMost Borrowed Book:\n";
                    reportContent += "Title: " + rs.getString("title") + "\n";
                    reportContent += "Borrow Count: " + rs.getInt("borrow_count") + "\n";}}
            try (FileWriter writer = new FileWriter("LibraryReport.txt")){//لإنشاء ملف تقرير و تخزينه
                writer.write(reportContent);
                JOptionPane.showMessageDialog(null, "Report generated successfully as LibraryReport.txt!");
            }catch(IOException ex){
                JOptionPane.showMessageDialog(null, "Error writing report file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);}
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Error generating report: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }}}
    
private class GenerateReceiptAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int selectedRow = borrowTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(null, "Please select a rental to generate a receipt.", "Error", JOptionPane.ERROR_MESSAGE);
                return;}
            int rentalId = (int) borrowTable.getValueAt(selectedRow, 0);
            int userId = (int) borrowTable.getValueAt(selectedRow, 1);
            int bookId = (int) borrowTable.getValueAt(selectedRow, 2);
            String borrowDate = borrowTable.getValueAt(selectedRow, 3).toString();
            String returnDate = borrowTable.getValueAt(selectedRow, 4).toString();
            String status = (String) borrowTable.getValueAt(selectedRow, 5);
            //محتوى الايصال
            String receiptContent = "Library Borrowing Receipt\n"
                    + "--------------------------\n"
                    + "Rental ID: " + rentalId + "\n"
                    + "User ID: " + userId + "\n"
                    + "Book ID: " + bookId + "\n"
                    + "Borrow Date: " + borrowDate + "\n"
                    + "Return Date: " + returnDate + "\n"
                    + "Status: " + status + "\n";
            try (FileWriter writer = new FileWriter("Receipt_" + rentalId + ".txt")){//إنشاء ملف ايصال و تخزينه
                writer.write(receiptContent);
                JOptionPane.showMessageDialog(null, "Receipt saved as Receipt_" + rentalId + ".txt");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error saving receipt: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }}}
 
class ManageUserAction implements ActionListener{
          public void actionPerformed(ActionEvent e){ 
          new MemberManagement(); }}

private class MarkAsReturnedAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = borrowTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Please select a rental to mark as returned.", "Error", JOptionPane.ERROR_MESSAGE);
            return;}
        int borrowId = (int) borrowTable.getValueAt(selectedRow, 0); 
        int bookId = (int) borrowTable.getValueAt(selectedRow, 2);   
        try (Connection conn = DatabaseManager.getConnection()) {
            String updateBorrowQuery = "UPDATE borrowings SET status = 'returned' WHERE id = ?";//استعلام لتحديث حالة الاستعارة
            try (PreparedStatement stmt = conn.prepareStatement(updateBorrowQuery)) {
                stmt.setInt(1, borrowId);
                stmt.executeUpdate();}
            //استعلام لتحديث بيانات الكتاب
            String updateBookQuery = "UPDATE books SET num_of_books = num_of_books + 1, " +"availability = IF(num_of_books + 1 > 0, 1, 0) WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateBookQuery)) {
                stmt.setInt(1, bookId);
                stmt.executeUpdate();}
            JOptionPane.showMessageDialog(null, "Book marked as returned!");
            loadBorrows();
            loadBooks();   
        }catch(SQLException ex){
            JOptionPane.showMessageDialog(null, "Error marking book as returned: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }}}

class clearnotfication implements ActionListener{
          public void actionPerformed(ActionEvent e){
          notificationArea.setText("");
          }}}