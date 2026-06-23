package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.sql.*;

public class Books extends JFrame {
    private JTable bookTable, borrowHistoryTable;
    private JTextField titleField, authorField;
    private JComboBox<String> genreComboBox, authorComboBox, yearComboBox;
    private JButton searchButton, borrowButton, clearNotificationsButton, filterButton,refreshNotificationsButton;
    private JTextArea notificationArea;
    private String username;

    public Books(String username) {
        this.username = username;
        setTitle("Library Book Browser");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setBounds(20, 20, 100, 25);
        add(titleLabel);

        titleField = new JTextField();
        titleField.setBounds(100, 20, 150, 25);
        add(titleField);

        JLabel authorLabel = new JLabel("Author:");
        authorLabel.setBounds(270, 20, 100, 25);
        add(authorLabel);

        authorField = new JTextField();
        authorField.setBounds(350, 20, 150, 25);
        add(authorField);

        searchButton = new JButton("Search");
        searchButton.setBounds(520, 20, 100, 25);
        add(searchButton);

        borrowButton = new JButton("Borrow Book");
        borrowButton.setBounds(630, 20, 120, 25);
        add(borrowButton);

        JLabel genreLabel = new JLabel("Genre:");
        genreLabel.setBounds(20, 60, 100, 25);
        add(genreLabel);

        genreComboBox = new JComboBox<>();
        genreComboBox.setBounds(100, 60, 150, 25);
        add(genreComboBox);

        JLabel authorFilterLabel = new JLabel("Author:");
        authorFilterLabel.setBounds(270, 60, 100, 25);
        add(authorFilterLabel);

        authorComboBox = new JComboBox<>();
        authorComboBox.setBounds(350, 60, 150, 25);
        add(authorComboBox);

        JLabel yearLabel = new JLabel("Publication Year:");
        yearLabel.setBounds(520, 60, 120, 25);
        add(yearLabel);

        yearComboBox = new JComboBox<>();
        yearComboBox.setBounds(650, 60, 100, 25);
        add(yearComboBox);

        filterButton = new JButton("Filter");
        filterButton.setBounds(760, 60, 100, 25);
        add(filterButton);

        bookTable = new JTable();
        JScrollPane bookScrollPane = new JScrollPane(bookTable);
        bookScrollPane.setBounds(20, 100, 840, 200);
        add(bookScrollPane);
        bookTable.setDefaultEditor(Object.class, null); 

        JLabel borrowHistoryLabel = new JLabel("Borrow History");
        borrowHistoryLabel.setBounds(20, 320, 200, 25);
        add(borrowHistoryLabel);

        borrowHistoryTable = new JTable();
        borrowHistoryTable.setDefaultEditor(Object.class, null);
        JScrollPane borrowHistoryScrollPane = new JScrollPane(borrowHistoryTable);
        borrowHistoryScrollPane.setBounds(20, 350, 840, 150);
        add(borrowHistoryScrollPane);

notificationArea = new JTextArea();
notificationArea.setEditable(false);
JScrollPane notificationScrollPane = new JScrollPane(notificationArea);
notificationScrollPane.setBounds(20, 520, 700, 100);
add(notificationScrollPane);

clearNotificationsButton = new JButton("Clear Notifications");
clearNotificationsButton.setBounds(750, 520, 120, 25);
add(clearNotificationsButton);

JButton refreshNotificationsButton = new JButton("Refresh Notifications");
refreshNotificationsButton.setBounds(750, 550, 120, 25);
add(refreshNotificationsButton);

clearNotificationsButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        notificationArea.setText("");
    }
});
refreshNotificationsButton.addActionListener(new RefreshNotificationsAction());

        loadComboBoxData();
        loadBooks("", "");
        loadBorrowHistory();
        loadNotifications();
        searchButton.addActionListener(new SearchAction());
        borrowButton.addActionListener(new BorrowAction());
        filterButton.addActionListener(new FilterAction());
        clearNotificationsButton.addActionListener(new ClearNotificationsAction());

        setLocationRelativeTo(null); 
        setVisible(true);
    }

    private void loadBooks(String title, String author) {
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"ID", "Title", "Author", "Genre", "Available Copies", "Availability"}, 0);

        String query = "SELECT id, title, author, genre, num_of_books, availability FROM books WHERE title LIKE ? AND author LIKE ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + title + "%");
            stmt.setString(2, "%" + author + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("genre"),
                            rs.getInt("num_of_books"),
                            rs.getInt("availability") == 1 ? "Yes" : "No"
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading books: " + e.getMessage());
        }

        bookTable.setModel(tableModel);
    }

    private void loadBorrowHistory() {
        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"ID", "Book Title", "Author", "Borrow Date", "Return Date", "Status", "Overdue Days"}, 0
        );

        String query = "SELECT br.id, b.title, b.author, br.borrow_date, br.return_date, br.status, " +
                       "DATEDIFF(CURDATE(), br.return_date) AS overdue_days " +
                       "FROM borrowings br " +
                       "JOIN books b ON br.book_id = b.id " +
                       "WHERE br.user_id = (SELECT id FROM users WHERE username = ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getDate("borrow_date"),
                            rs.getDate("return_date"),
                            rs.getString("status"),
                            rs.getInt("overdue_days") > 0 ? rs.getInt("overdue_days") : 0
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading borrow history: " + e.getMessage());
        }

        borrowHistoryTable.setModel(tableModel);
    }

    private void loadNotifications() {
        notificationArea.setText("");

        addBorrowedBookNotifications();
        addOverdueNotifications();
    }

    private void addBorrowedBookNotifications() {
        String query = "SELECT b.title, br.return_date FROM borrowings br JOIN books b ON br.book_id = b.id " +
                       "WHERE br.user_id = (SELECT id FROM users WHERE username = ?) AND br.status = 'borrowed'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                notificationArea.append("Borrowed Books:\n");
                while (rs.next()) {
                    notificationArea.append("Title: " + rs.getString("title") + "\n");
                    notificationArea.append("Due Date: " + rs.getDate("return_date") + "\n\n");
                }
            }
        } catch (SQLException e) {
            notificationArea.append("Error fetching borrowed books: " + e.getMessage() + "\n");
        }
    }

    private void addOverdueNotifications() {
        String query = "SELECT b.title, u.username, br.return_date FROM borrowings br " +
                       "JOIN books b ON br.book_id = b.id " +
                       "JOIN users u ON br.user_id = u.id " +
                       "WHERE br.status = 'overdue'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            notificationArea.append("Overdue Books:\n");
            while (rs.next()) {
                notificationArea.append("Title: " + rs.getString("title") + "\n");
                notificationArea.append("Username: " + rs.getString("username") + "\n");
                notificationArea.append("Due Date: " + rs.getDate("return_date") + "\n\n");
            }
        } catch (SQLException e) {
            notificationArea.append("Error fetching overdue books: " + e.getMessage() + "\n");
        }
    }

    private class ClearNotificationsAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            notificationArea.setText("");
        }
    }

    private class SearchAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            loadBooks(title, author);
        }
    }

    private class BorrowAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow != -1) {
            int bookId = (int) bookTable.getValueAt(selectedRow, 0);
            String bookTitle = (String) bookTable.getValueAt(selectedRow, 1);
            String bookAuthor = (String) bookTable.getValueAt(selectedRow, 2);
            String availability = (String) bookTable.getValueAt(selectedRow, 5); // الحصول على حالة التوفر

            if (availability.equals("No")) { // إذا كان الكتاب غير متوفر
                JOptionPane.showMessageDialog(Books.this, "This book is currently unavailable for borrowing.");
            } else {
                new BorrowManager(username, bookId, bookTitle, bookAuthor).setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(Books.this, "Please select a book to borrow.");
        }
    }
}


  private class FilterAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        String genre = genreComboBox.getSelectedItem().toString();
        String author = authorComboBox.getSelectedItem().toString();
        String year = yearComboBox.getSelectedItem().toString();

        DefaultTableModel tableModel = new DefaultTableModel(
                new String[]{"ID", "Title", "Author", "Genre", "Available Copies", "Availability"}, 0
        );

        String query = "SELECT id, title, author, genre, num_of_books, availability FROM books WHERE 1=1";
        
        if (!genre.equals("All")) {
            query += " AND genre = ?";
        }
        if (!author.equals("All")) {
            query += " AND author = ?";
        }
        if (!year.equals("All")) {
            query += " AND YEAR(publication_date) = ?";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            int parameterIndex = 1;

            if (!genre.equals("All")) {
                stmt.setString(parameterIndex++, genre);
            }
            if (!author.equals("All")) {
                stmt.setString(parameterIndex++, author);
            }
            if (!year.equals("All")) {
                stmt.setString(parameterIndex++, year);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getString("genre"),
                            rs.getInt("num_of_books"),
                            rs.getInt("availability") == 1 ? "Yes" : "No"
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(Books.this, "Error filtering books: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        bookTable.setModel(tableModel);
    }
}

    private void loadComboBoxData() {
    try (Connection conn = DatabaseManager.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT DISTINCT genre FROM books")) {

        genreComboBox.addItem("All"); 
        while (rs.next()) {
            genreComboBox.addItem(rs.getString("genre")); 
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading genres: " + e.getMessage());
    }

    try (Connection conn = DatabaseManager.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT DISTINCT author FROM books")) {

        authorComboBox.addItem("All"); 
        while (rs.next()) {
            authorComboBox.addItem(rs.getString("author"));
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading authors: " + e.getMessage());
    }
    try (Connection conn = DatabaseManager.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT DISTINCT YEAR(publication_date) AS year FROM books")) {

        yearComboBox.addItem("All"); 
        while (rs.next()) {
            yearComboBox.addItem(rs.getString("year"));
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error loading publication years: " + e.getMessage());
    }
    
    
}
    private class RefreshNotificationsAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        loadNotifications();
    }
    }
}




