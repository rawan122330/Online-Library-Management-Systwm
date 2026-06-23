

package library;
import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/mysql";  
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void createTables() {
        try (Connection conn = getConnection(); 
             Statement stmt = conn.createStatement()) {
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "username VARCHAR(255) UNIQUE NOT NULL, "
                    + "email VARCHAR(255) UNIQUE NOT NULL, "
                    + "password VARCHAR(255) NOT NULL)";
            stmt.executeUpdate(createUsersTable);

            String createBooksTable = "CREATE TABLE IF NOT EXISTS books ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "title VARCHAR(255) NOT NULL, "
                    + "author VARCHAR(255) NOT NULL, "
                    + "genre VARCHAR(255), "
                    + "publication_date DATE, "
                    + "num_of_books INT NOT NULL, "
                    + "availability ENUM('Yes', 'No') NOT NULL DEFAULT 'Yes')";
            stmt.executeUpdate(createBooksTable);

            String createBorrowingsTable = "CREATE TABLE IF NOT EXISTS borrowings ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "user_id INT NOT NULL, "
                    + "book_id INT NOT NULL, "
                    + "borrow_date DATE NOT NULL, "
                    + "return_date DATE NOT NULL, "
                    + "status ENUM('borrowed', 'returned') NOT NULL, "
                    + "FOREIGN KEY (user_id) REFERENCES users(id), "
                    + "FOREIGN KEY (book_id) REFERENCES books(id))";
            stmt.executeUpdate(createBorrowingsTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}