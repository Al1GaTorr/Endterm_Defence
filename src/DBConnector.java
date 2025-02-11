import java.sql.*;
import java.time.LocalDateTime;

public class DBConnector {

    private static Connection connection;

    private static final LocalDateTime now = LocalDateTime.now();
    private static final Timestamp currentTimestamp = Timestamp.valueOf(now);

    static {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/postgres",
                    "postgres",
                    "875173"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
}
