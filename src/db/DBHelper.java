package src.db;
import java.sql.*;

public class DBHelper {
    private Connection conn;

    public DBHelper() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/finance_manager";
        String user = "root"; // use your MySQL username
        String password = "2003";
        conn = DriverManager.getConnection(url, user, password);
    }

    public Connection getConnection() { return conn; }

    public void close() {
        try { if (conn != null) conn.close(); } catch (SQLException e) { }
    }
    
}
