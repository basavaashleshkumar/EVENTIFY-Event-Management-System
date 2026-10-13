package db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database connectivity using Oracle JDBC Thin Driver.
 * Reads database connection configuration from config/db.properties.
 * 
 * Demonstrates:
 * - DriverManager.getConnection()
 * - Proper driver initialization
 * - Configuration separation from source code
 */
public class DatabaseConnection {

    private static String dbUrl = "jdbc:oracle:thin:@localhost:1521/xe";
    private static String dbUser = "eventify";
    private static String dbPassword = "eventify123";
    private static String dbDriver = "oracle.jdbc.OracleDriver";
    private static boolean driverLoaded = false;

    static {
        loadConfiguration();
        initializeDriver();
    }

    /**
     * Loads database configuration from config/db.properties file.
     */
    public static void loadConfiguration() {
        Properties props = new Properties();
        InputStream is = null;
        try {
            // 1. Try relative path config/db.properties
            File file = new File("config/db.properties");
            if (!file.exists()) {
                // 2. Try looking inside Eventify/config/db.properties
                file = new File("Eventify/config/db.properties");
            }

            if (file.exists()) {
                is = new FileInputStream(file);
            } else {
                // 3. Fallback to classpath
                is = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties");
                if (is == null) {
                    is = DatabaseConnection.class.getClassLoader().getResourceAsStream("config/db.properties");
                }
            }

            if (is != null) {
                props.load(is);
                dbUrl = props.getProperty("db.url", dbUrl).trim();
                dbUser = props.getProperty("db.username", dbUser).trim();
                dbPassword = props.getProperty("db.password", dbPassword).trim();
                dbDriver = props.getProperty("db.driver", dbDriver).trim();
                System.out.println("[DatabaseConnection] Loaded configuration from db.properties: " + dbUrl + " (" + dbUser + ")");
            } else {
                System.out.println("[DatabaseConnection] db.properties not found, using default settings: " + dbUrl);
            }
        } catch (Exception e) {
            System.err.println("[DatabaseConnection] Warning: Error reading db.properties: " + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Initializes the Oracle JDBC Driver.
     */
    private static void initializeDriver() {
        try {
            Class.forName(dbDriver);
            driverLoaded = true;
            System.out.println("[DatabaseConnection] Oracle JDBC Driver successfully registered: " + dbDriver);
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] Critical Error: Oracle JDBC Driver class not found: " + dbDriver);
            e.printStackTrace();
        }
    }

    /**
     * Opens and returns a new connection to the Oracle database.
     * 
     * @return java.sql.Connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            initializeDriver();
        }
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }

    /**
     * Tests whether the Oracle database is reachable.
     * 
     * @return true if connected successfully, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Test connection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Safely closes one or more JDBC resources without throwing exceptions.
     */
    public static void closeQuietly(AutoCloseable... resources) {
        if (resources != null) {
            for (AutoCloseable res : resources) {
                if (res != null) {
                    try {
                        res.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    // Getters and setters for runtime overrides (e.g. settings dialog)
    public static String getDbUrl() {
        return dbUrl;
    }

    public static void setDbUrl(String dbUrl) {
        DatabaseConnection.dbUrl = dbUrl;
    }

    public static String getDbUser() {
        return dbUser;
    }

    public static void setDbUser(String dbUser) {
        DatabaseConnection.dbUser = dbUser;
    }

    public static void setDbPassword(String dbPassword) {
        DatabaseConnection.dbPassword = dbPassword;
    }
}
