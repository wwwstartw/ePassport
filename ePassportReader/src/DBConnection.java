import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static String driverClass = "com.mysql.cj.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/epassport";
    private static String username = "root";
    private static String password = "root";
    private static Connection conn = null;
    static {
        try {
            Class.forName(driverClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static Connection getConnection() throws SQLException {
        conn = DriverManager.getConnection(url, username, password);
        return conn;
    }
}